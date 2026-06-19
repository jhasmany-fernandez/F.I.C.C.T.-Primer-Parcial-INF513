package Integracion;

import Datos.DPago;
import Utils.AppEnv;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PagoFacilCallbackServer {

    private final int port;
    private final String path;
    private final HttpServer server;
    private final DPago dPago;

    public PagoFacilCallbackServer() throws IOException {
        this.port = parseIntEnv("PROYECTOEMAIL_CALLBACK_PORT", 8085);
        this.path = normalizePath(getEnv("PROYECTOEMAIL_CALLBACK_PATH", "/pagofacil/callback"));
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext(path, new CallbackHandler());
        this.server.setExecutor(Executors.newFixedThreadPool(2));
        this.dPago = new DPago();
    }

    public void start() {
        server.start();
        System.out.println("PagoFacil callback server escuchando en http://0.0.0.0:" + port + path);
    }

    private class CallbackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if (!"POST".equalsIgnoreCase(method) && !"GET".equalsIgnoreCase(method)) {
                writeResponse(exchange, 405, "{\"ok\":false,\"message\":\"Method not allowed\"}");
                return;
            }

            String payload = readBody(exchange.getRequestBody());
            if (payload.trim().isEmpty()) {
                payload = exchange.getRequestURI().getRawQuery();
                if (payload == null) {
                    payload = "";
                }
            }

            try {
                CallbackResult result = processPayload(payload);
                if (result.updatedRows > 0) {
                    writeResponse(exchange, 200, "{\"ok\":true,\"updated\":" + result.updatedRows + "}");
                    return;
                }
                writeResponse(exchange, 202, "{\"ok\":true,\"updated\":0,\"message\":\"No matching payment\"}");
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                writeResponse(exchange, 500, "{\"ok\":false,\"message\":\"" + escapeJson(ex.getMessage()) + "\"}");
            }
        }
    }

    private CallbackResult processPayload(String payload) throws SQLException {
        String paymentNumber = firstString(payload,
                "paymentNumber", "payment_number", "numeroPago", "orderId", "externalReference");
        Long transactionId = firstLong(payload,
                "pagofacilTransactionId", "transactionId", "idTransaccion", "transaccionId");
        String status = normalizeStatus(firstString(payload,
                "status", "estado", "transactionStatus", "transaction_state", "message", "mensaje"));

        String estadoLocal = isPaidStatus(status) ? "PAGADO" : "PENDIENTE";
        int cuotasPagadas = isPaidStatus(status) ? 1 : 0;
        int updated = dPago.actualizarEstadoQrPorReferenciaExterna(
                paymentNumber,
                transactionId,
                estadoLocal,
                cuotasPagadas,
                status,
                payload
        );
        return new CallbackResult(updated);
    }

    private static class CallbackResult {

        private final int updatedRows;

        CallbackResult(int updatedRows) {
            this.updatedRows = updatedRows;
        }
    }

    private void writeResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String readBody(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static String firstString(String source, String... keys) {
        for (String key : keys) {
            String value = extractJsonString(source, key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
            value = extractQueryValue(source, key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static Long firstLong(String source, String... keys) {
        for (String key : keys) {
            Long value = extractJsonLong(source, key);
            if (value != null) {
                return value;
            }
            String query = extractQueryValue(source, key);
            if (query != null) {
                try {
                    return Long.valueOf(query.trim());
                } catch (NumberFormatException ex) {
                    // Sigue con la siguiente alternativa.
                }
            }
        }
        return null;
    }

    private static String extractJsonString(String source, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"");
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static Long extractJsonLong(String source, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String extractQueryValue(String source, String key) {
        Pattern pattern = Pattern.compile("(^|[?&])" + Pattern.quote(key) + "=([^&]+)");
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(2)
                .replace("+", " ")
                .replace("%20", " ");
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "SIN_ESTADO";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isPaidStatus(String status) {
        return "PAGADO".equals(status)
                || "SUCCESS".equals(status)
                || "COMPLETED".equals(status)
                || "APROBADO".equals(status)
                || "APPROVED".equals(status)
                || "PROCESSED".equals(status);
    }

    private static String normalizePath(String rawPath) {
        String value = rawPath == null || rawPath.trim().isEmpty() ? "/pagofacil/callback" : rawPath.trim();
        if (!value.startsWith("/")) {
            return "/" + value;
        }
        return value;
    }

    private static String getEnv(String key, String defaultValue) {
        return AppEnv.get(key, defaultValue);
    }

    private static int parseIntEnv(String key, int defaultValue) {
        return AppEnv.getInt(key, defaultValue);
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
