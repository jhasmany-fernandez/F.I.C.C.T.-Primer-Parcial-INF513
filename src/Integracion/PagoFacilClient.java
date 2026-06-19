package Integracion;

import Utils.AppEnv;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PagoFacilClient {

    public static class QrRequest {

        private final String clientName;
        private final String documentId;
        private final String phoneNumber;
        private final String email;
        private final String paymentNumber;
        private final BigDecimal amount;
        private final String clientCode;
        private final String callbackUrl;
        private final String concept;

        public QrRequest(String clientName, String documentId, String phoneNumber, String email,
                String paymentNumber, BigDecimal amount, String clientCode, String callbackUrl, String concept) {
            this.clientName = clientName;
            this.documentId = documentId;
            this.phoneNumber = phoneNumber;
            this.email = email;
            this.paymentNumber = paymentNumber;
            this.amount = amount;
            this.clientCode = clientCode;
            this.callbackUrl = callbackUrl;
            this.concept = concept;
        }
    }

    public static class QrResponse {

        private final String rawResponse;
        private final Long pagofacilTransactionId;
        private final String qrImageBase64;
        private final String qrContent;
        private final String status;

        public QrResponse(String rawResponse, Long pagofacilTransactionId, String qrImageBase64,
                String qrContent, String status) {
            this.rawResponse = rawResponse;
            this.pagofacilTransactionId = pagofacilTransactionId;
            this.qrImageBase64 = qrImageBase64;
            this.qrContent = qrContent;
            this.status = status;
        }

        public String getRawResponse() {
            return rawResponse;
        }

        public Long getPagofacilTransactionId() {
            return pagofacilTransactionId;
        }

        public String getQrImageBase64() {
            return qrImageBase64;
        }

        public String getQrContent() {
            return qrContent;
        }

        public String getStatus() {
            return status;
        }
    }

    public static class TransactionStatusResponse {

        private final String rawResponse;
        private final String status;
        private final String message;

        public TransactionStatusResponse(String rawResponse, String status, String message) {
            this.rawResponse = rawResponse;
            this.status = status;
            this.message = message;
        }

        public String getRawResponse() {
            return rawResponse;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String tokenService;
    private final String tokenSecret;
    private final String callbackUrl;
    private final int paymentMethod;
    private final int documentType;
    private final int currency;

    public PagoFacilClient() {
        int timeoutMs = parseIntEnv("PROYECTOEMAIL_PAGOFACIL_TIMEOUT_MS", 15000);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
        this.baseUrl = getEnv("PROYECTOEMAIL_PAGOFACIL_BASE_URL",
                "https://masterqr.pagofacil.com.bo/api/services/v2");
        this.tokenService = getEnv("PROYECTOEMAIL_PAGOFACIL_TOKEN_SERVICE", "");
        this.tokenSecret = getEnv("PROYECTOEMAIL_PAGOFACIL_TOKEN_SECRET", "");
        this.callbackUrl = getEnv("PROYECTOEMAIL_PAGOFACIL_CALLBACK_URL", "");
        this.paymentMethod = parseIntEnv("PROYECTOEMAIL_PAGOFACIL_PAYMENT_METHOD", 34);
        this.documentType = parseIntEnv("PROYECTOEMAIL_PAGOFACIL_DOCUMENT_TYPE", 1);
        this.currency = parseIntEnv("PROYECTOEMAIL_PAGOFACIL_CURRENCY", 2);
    }

    public String getConfiguredCallbackUrl() {
        if (callbackUrl == null || callbackUrl.trim().isEmpty()) {
            return "https://example.com/pagofacil-demo-callback";
        }
        return callbackUrl;
    }

    public QrResponse generarQr(QrRequest request) throws IOException, InterruptedException {
        String token = login();
        String body = "{"
                + "\"paymentMethod\":" + paymentMethod + ","
                + "\"clientName\":\"" + escapeJson(request.clientName) + "\","
                + "\"documentType\":" + documentType + ","
                + "\"documentId\":\"" + escapeJson(request.documentId) + "\","
                + "\"phoneNumber\":\"" + escapeJson(request.phoneNumber) + "\","
                + "\"email\":\"" + escapeJson(request.email) + "\","
                + "\"paymentNumber\":\"" + escapeJson(request.paymentNumber) + "\","
                + "\"amount\":" + request.amount.toPlainString() + ","
                + "\"currency\":" + currency + ","
                + "\"clientCode\":\"" + escapeJson(request.clientCode) + "\","
                + "\"callbackUrl\":\"" + escapeJson(request.callbackUrl) + "\","
                + "\"orderDetail\":[{"
                + "\"serial\":1,"
                + "\"product\":\"" + escapeJson(request.concept) + "\","
                + "\"quantity\":1,"
                + "\"price\":" + request.amount.toPlainString() + ","
                + "\"discount\":0,"
                + "\"total\":" + request.amount.toPlainString()
                + "}]"
                + "}";

        String response = postJson("/generate-qr", token, body);
        return new QrResponse(
                response,
                firstLong(response, "pagofacilTransactionId", "transactionId", "transaccionId", "idTransaccion"),
                firstString(response, "qrImageBase64", "qrBase64", "base64Image", "imageBase64", "base64"),
                firstString(response, "qrContent", "qrText", "content", "url", "qr"),
                firstString(response, "status", "estado", "message", "mensaje")
        );
    }

    public TransactionStatusResponse consultarTransaccion(long pagofacilTransactionId)
            throws IOException, InterruptedException {
        String token = login();
        String body = "{\"pagofacilTransactionId\":" + pagofacilTransactionId + "}";
        String response = postJson("/query-transaction", token, body);
        return new TransactionStatusResponse(
                response,
                firstString(response, "status", "estado", "transactionStatus", "transaction_state"),
                firstString(response, "message", "mensaje", "detail", "descripcion")
        );
    }

    private String login() throws IOException, InterruptedException {
        requireConfigured("PROYECTOEMAIL_PAGOFACIL_TOKEN_SERVICE", tokenService);
        requireConfigured("PROYECTOEMAIL_PAGOFACIL_TOKEN_SECRET", tokenSecret);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/login"))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("tcTokenService", tokenService)
                .header("tcTokenSecret", tokenSecret)
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateStatus(response, "/login");
        String token = firstString(response.body(), "token", "access_token", "accessToken");
        if (token == null || token.trim().isEmpty()) {
            throw new IOException("PagoFacil no devolvio un token valido. Respuesta: " + compact(response.body()));
        }
        return token;
    }

    private String postJson(String path, String bearerToken, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(20))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + bearerToken)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        validateStatus(response, path);
        return response.body();
    }

    private void validateStatus(HttpResponse<String> response, String path) throws IOException {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        throw new IOException("PagoFacil respondio " + response.statusCode() + " en " + path
                + ". Detalle: " + compact(response.body()));
    }

    private static void requireConfigured(String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Falta configurar la variable de entorno " + key);
        }
    }

    private static String getEnv(String key, String defaultValue) {
        return AppEnv.get(key, defaultValue);
    }

    private static int parseIntEnv(String key, int defaultValue) {
        return AppEnv.getInt(key, defaultValue);
    }

    private static String firstString(String json, String... keys) {
        for (String key : keys) {
            String value = extractString(json, key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return null;
    }

    private static Long firstLong(String json, String... keys) {
        for (String key : keys) {
            Long value = extractLong(json, key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static String extractString(String json, String key) {
        String token = "\"" + key + "\"";
        int keyIndex = json.indexOf(token);
        if (keyIndex < 0) {
            return null;
        }
        int colonIndex = json.indexOf(':', keyIndex + token.length());
        if (colonIndex < 0) {
            return null;
        }
        int startQuote = json.indexOf('"', colonIndex + 1);
        if (startQuote < 0) {
            return null;
        }
        StringBuilder value = new StringBuilder();
        boolean escaped = false;
        for (int i = startQuote + 1; i < json.length(); i++) {
            char current = json.charAt(i);
            if (escaped) {
                value.append(current);
                escaped = false;
                continue;
            }
            if (current == '\\') {
                escaped = true;
                value.append(current);
                continue;
            }
            if (current == '"') {
                return unescapeJson(value.toString());
            }
            value.append(current);
        }
        return null;
    }

    private static Long extractLong(String json, String key) {
        String token = "\"" + key + "\"";
        int keyIndex = json.indexOf(token);
        if (keyIndex < 0) {
            return null;
        }
        int colonIndex = json.indexOf(':', keyIndex + token.length());
        if (colonIndex < 0) {
            return null;
        }
        int i = colonIndex + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) {
            i++;
        }
        int start = i;
        if (i < json.length() && json.charAt(i) == '-') {
            i++;
        }
        while (i < json.length() && Character.isDigit(json.charAt(i))) {
            i++;
        }
        if (i == start || (i == start + 1 && json.charAt(start) == '-')) {
            return null;
        }
        try {
            return Long.valueOf(json.substring(start, i));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }

    private static String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static String compact(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder compactedBuilder = new StringBuilder(value.length());
        boolean previousWhitespace = false;
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (Character.isWhitespace(current)) {
                if (!previousWhitespace) {
                    compactedBuilder.append(' ');
                    previousWhitespace = true;
                }
            } else {
                compactedBuilder.append(current);
                previousWhitespace = false;
            }
        }
        String compacted = compactedBuilder.toString().trim();
        if (compacted.length() > 400) {
            return compacted.substring(0, 400) + "...";
        }
        return compacted;
    }
}
