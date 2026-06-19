package Metodos;

import Negocio.NPago;
import Negocio.NPago.ConsultaQrResultado;
import Negocio.NPago.GenerarQrResultado;
import Datos.DPago.PagoQrInfo;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Pagos {

    private static final String[] HEADERS = {
        "ID", "ID Suscripcion", "ID Cliente", "Cliente", "Tipo pago", "Monto total",
        "Cantidad cuotas", "Cuotas pagadas", "Metodo", "Fecha pago", "Fecha registro",
        "Estado", "Usuario creacion", "Usuario modificacion", "Fecha modificacion",
        "Payment number", "PF Tx ID", "Estado externo"
    };
    private static final String[] CUOTA_HEADERS = {
        "ID Cuota", "ID Pago", "Numero cuota", "Monto", "Fecha pago", "Estado"
    };

    private final NPago nPago;

    public Pagos() {
        nPago = new NPago();
    }

    public void ejecutar(String accion, List<String> parametros, String correo) {
        try {
            switch (accion.toLowerCase()) {
                case "ayuda":
                    ayuda(correo);
                    break;
                case "mostrar":
                    mostrar(correo);
                    break;
                case "agregar":
                    agregar(parametros, correo);
                    break;
                case "registrar_contado":
                    registrarContado(parametros, correo);
                    break;
                case "registrar_credito":
                    registrarCredito(parametros, correo);
                    break;
                case "registrar_cuota":
                    registrarCuota(parametros, correo);
                    break;
                case "generar_qr":
                    generarQr(parametros, correo);
                    break;
                case "consultar_qr":
                    consultarQr(parametros, correo);
                    break;
                case "pagar_cuota":
                    pagarCuota(parametros, correo);
                    break;
                case "ver":
                    ver(parametros, correo);
                    break;
                case "modificar":
                    modificar(parametros, correo);
                    break;
                case "eliminar":
                    eliminar(parametros, correo);
                    break;
                case "mis_pagos":
                    misPagos(correo);
                    break;
                default:
                    nPago.validarAccesoGestion(correo);
                    enviar(correo, HtmlBuilder.generateError("CU7 Pagos",
                            "Comando no valido para CU7. Use: pago ayuda"));
                    break;
            }
        } catch (SecurityException ex) {
            enviar(correo, HtmlBuilder.generateError("CU7 Pagos", ex.getMessage()));
        } catch (SQLException | IllegalArgumentException | IllegalStateException ex) {
            enviarError(correo, "CU7 Pagos", ex.getMessage());
        }
    }

    private void ayuda(String correo) throws SQLException {
        nPago.validarAccesoGestion(correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos", ""
                + "pago ayuda<br>"
                + "pago mostrar<br>"
                + "pago agregar [id_suscripcion; tipo_pago; monto_total; cantidad_cuotas; metodo; fecha_pago]<br>"
                + "pago registrar_contado [id_suscripcion; monto; fecha_pago; metodo]<br>"
                + "pago registrar_credito [id_suscripcion; monto_total; cantidad_cuotas; fecha_inicio; metodo]<br>"
                + "pago registrar_cuota [id_pago; numero_cuota; monto; fecha_pago]<br>"
                + "pago pagar_cuota [id_cuota]<br>"
                + "pago generar_qr [id_suscripcion; monto; fecha_pago; documento_identidad; telefono; concepto]<br>"
                + "pago consultar_qr [id_pago]<br>"
                + "pago ver [id_pago]<br>"
                + "pago modificar [id_pago; monto; metodo]<br>"
                + "pago eliminar [id_pago]<br>"
                + "pago mis_pagos<br>"
                + "Metodos: EFECTIVO, TRANSFERENCIA, QR. eliminar realiza baja logica."));
    }

    private void mostrar(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU7 Pagos",
                "Listado de pagos",
                HEADERS,
                new ArrayList<>(nPago.listar(correo))
        ));
    }

    private void agregar(List<String> parametros, String correo) throws SQLException {
        int id = nPago.agregar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos",
                "Pago registrado correctamente con ID " + id + "."));
    }

    private void registrarContado(List<String> parametros, String correo) throws SQLException {
        int id = nPago.registrarContado(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos",
                "Pago contado registrado correctamente con ID " + id + "."));
    }

    private void registrarCredito(List<String> parametros, String correo) throws SQLException {
        int id = nPago.registrarCredito(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos",
                "Pago credito registrado correctamente con ID " + id + "."));
    }

    private void registrarCuota(List<String> parametros, String correo) throws SQLException {
        nPago.registrarCuota(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos", "Cuota registrada correctamente."));
    }

    private void pagarCuota(List<String> parametros, String correo) throws SQLException {
        nPago.pagarCuota(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos", "Cuota pagada correctamente."));
    }

    private void generarQr(List<String> parametros, String correo) throws SQLException {
        GenerarQrResultado resultado = nPago.generarQr(parametros, correo);
        enviar(correo, generarRespuestaQr(resultado));
    }

    private void consultarQr(List<String> parametros, String correo) throws SQLException {
        ConsultaQrResultado resultado = nPago.consultarQr(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess(
                "CU7 Pagos",
                "Consulta QR realizada. ID pago: " + resultado.getIdPago()
                + ". Estado local: " + resultado.getEstadoLocal()
                + ". Estado externo: " + resultado.getEstadoExterno()
                + (resultado.getMensaje() == null || resultado.getMensaje().trim().isEmpty()
                        ? "."
                        : ". Detalle: " + resultado.getMensaje())
        ));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(nPago.obtenerPorId(parametros, correo));
        ArrayList<String[]> cuotas = new ArrayList<>(nPago.listarCuotasPorPago(parametros, correo));
        PagoQrInfo qrInfo = nPago.obtenerQrInfo(parametros, correo);
        enviar(correo, generarDetalleConCuotas(rows, cuotas, qrInfo));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        nPago.modificar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos", "Pago modificado correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        nPago.eliminar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU7 Pagos", "Pago marcado como INACTIVO correctamente."));
    }

    private void misPagos(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU7 Pagos",
                "Mis pagos",
                HEADERS,
                new ArrayList<>(nPago.misPagos(correo))
        ));
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }

    private String generarDetalleConCuotas(ArrayList<String[]> pagoRows, ArrayList<String[]> cuotaRows,
            PagoQrInfo qrInfo) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;color:#111827}")
                .append("table{border-collapse:collapse;width:100%;margin-bottom:24px}")
                .append("th,td{border:1px solid #d1d5db;padding:8px;text-align:left}")
                .append("th{background:#f3f4f6}")
                .append("</style></head><body>")
                .append("<h1>CU7 Pagos</h1>")
                .append("<p>Detalle de pago</p>");
        appendTable(html, HEADERS, pagoRows);
        appendQrSection(html, qrInfo);
        html.append("<h2>Cuotas</h2>");
        appendTable(html, CUOTA_HEADERS, cuotaRows);
        html.append("</body></html>");
        return html.toString();
    }

    private String generarRespuestaQr(GenerarQrResultado resultado) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;color:#111827}")
                .append(".card{max-width:760px;margin:24px auto;padding:24px;border:1px solid #d1d5db;border-radius:12px}")
                .append(".meta{margin:12px 0}")
                .append(".meta p{margin:6px 0}")
                .append("img{max-width:320px;border:1px solid #d1d5db;border-radius:8px}")
                .append("code{display:block;white-space:pre-wrap;background:#f3f4f6;padding:12px;border-radius:8px}")
                .append("</style></head><body><main class=\"card\">")
                .append("<h1>CU7 Pagos</h1>")
                .append("<p>QR generado correctamente con PagoFacil.</p>")
                .append("<div class=\"meta\">")
                .append("<p><strong>ID pago:</strong> ").append(resultado.getIdPago()).append("</p>")
                .append("<p><strong>Payment number:</strong> ").append(escape(resultado.getPaymentNumber())).append("</p>")
                .append("<p><strong>PF Tx ID:</strong> ")
                .append(resultado.getPagofacilTransactionId() == null ? "" : resultado.getPagofacilTransactionId())
                .append("</p>")
                .append("<p><strong>Callback URL:</strong> ").append(escape(resultado.getCallbackUrl())).append("</p>")
                .append("</div>");
        if (resultado.getQrImagenBase64() != null && !resultado.getQrImagenBase64().trim().isEmpty()) {
            html.append("<p><strong>QR para pago:</strong></p>")
                    .append("<img alt=\"QR PagoFacil\" src=\"data:image/png;base64,")
                    .append(escape(resultado.getQrImagenBase64()))
                    .append("\"/>");
        }
        if (resultado.getQrContenido() != null && !resultado.getQrContenido().trim().isEmpty()) {
            html.append("<p><strong>Contenido QR:</strong></p><code>")
                    .append(escape(resultado.getQrContenido()))
                    .append("</code>");
        }
        html.append("</main></body></html>");
        return html.toString();
    }

    private void appendQrSection(StringBuilder html, PagoQrInfo qrInfo) {
        if (qrInfo == null || !"QR".equals(qrInfo.getMetodo())) {
            return;
        }
        html.append("<h2>Integracion QR</h2>")
                .append("<p><strong>Payment number:</strong> ").append(escape(qrInfo.getPaymentNumber())).append("</p>")
                .append("<p><strong>PF Tx ID:</strong> ")
                .append(qrInfo.getPagofacilTransactionId() == null ? "" : qrInfo.getPagofacilTransactionId())
                .append("</p>")
                .append("<p><strong>Estado externo:</strong> ").append(escape(qrInfo.getEstadoExterno())).append("</p>")
                .append("<p><strong>Callback URL:</strong> ").append(escape(qrInfo.getCallbackUrl())).append("</p>");
        if (qrInfo.getQrImagenBase64() != null && !qrInfo.getQrImagenBase64().trim().isEmpty()) {
            html.append("<p><strong>QR:</strong></p>")
                    .append("<img alt=\"QR PagoFacil\" style=\"max-width:320px;border:1px solid #d1d5db;border-radius:8px\" src=\"data:image/png;base64,")
                    .append(escape(qrInfo.getQrImagenBase64()))
                    .append("\"/>");
        }
        if (qrInfo.getQrContenido() != null && !qrInfo.getQrContenido().trim().isEmpty()) {
            html.append("<p><strong>Contenido QR:</strong></p><pre style=\"white-space:pre-wrap;background:#f3f4f6;padding:12px;border-radius:8px\">")
                    .append(escape(qrInfo.getQrContenido()))
                    .append("</pre>");
        }
    }

    private void appendTable(StringBuilder html, String[] headers, ArrayList<String[]> rows) {
        html.append("<table><thead><tr>");
        for (String header : headers) {
            html.append("<th>").append(escape(header)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (String[] row : rows) {
            html.append("<tr>");
            for (String cell : row) {
                html.append("<td>").append(escape(cell)).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
