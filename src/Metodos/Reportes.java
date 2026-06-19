package Metodos;

import Negocio.NReporte;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Reportes {

    private static final String[] HEADERS = {"Metrica", "Valor"};
    private final NReporte nReporte;

    public Reportes() {
        nReporte = new NReporte();
    }

    public void ejecutar(String accion, List<String> parametros, String correo) {
        try {
            switch (accion.toLowerCase()) {
                case "ayuda":
                    ayuda(correo);
                    break;
                case "usuarios":
                    enviarReporte(correo, "Reporte usuarios", nReporte.usuarios(correo));
                    break;
                case "membresias":
                    enviarReporte(correo, "Reporte membresias", nReporte.membresias(correo));
                    break;
                case "paquetes":
                    enviarReporte(correo, "Reporte paquetes", nReporte.paquetes(correo));
                    break;
                case "suscripciones":
                    enviarReporte(correo, "Reporte suscripciones", nReporte.suscripciones(correo));
                    break;
                case "pagos":
                    enviarReporte(correo, "Reporte pagos", nReporte.pagos(correo));
                    break;
                case "rutinas":
                    enviarReporte(correo, "Reporte rutinas", nReporte.rutinas(correo));
                    break;
                case "seguimientos":
                    enviarReporte(correo, "Reporte seguimientos", nReporte.seguimientos(correo));
                    break;
                case "estadisticas":
                    enviarReporte(correo, "Reporte estadisticas", nReporte.estadisticas(correo));
                    break;
                case "general":
                    enviarReporte(correo, "Reporte general", nReporte.general(correo));
                    break;
                default:
                    nReporte.validarAccesoReportes(correo);
                    enviar(correo, HtmlBuilder.generateError("CU8 Reportes",
                            "Comando no valido para CU8. Use: reporte ayuda"));
                    break;
            }
        } catch (SecurityException ex) {
            enviar(correo, HtmlBuilder.generateError("CU8 Reportes", ex.getMessage()));
        } catch (SQLException | IllegalArgumentException ex) {
            enviarError(correo, "CU8 Reportes", ex.getMessage());
        }
    }

    private void ayuda(String correo) throws SQLException {
        nReporte.validarAccesoReportes(correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU8 Reportes", ""
                + "reporte ayuda<br>"
                + "reporte usuarios<br>"
                + "reporte membresias<br>"
                + "reporte paquetes<br>"
                + "reporte suscripciones<br>"
                + "reporte pagos<br>"
                + "reporte rutinas<br>"
                + "reporte seguimientos<br>"
                + "reporte estadisticas<br>"
                + "reporte general"));
    }

    private void enviarReporte(String correo, String subtitulo, List<String[]> rows) {
        enviar(correo, HtmlBuilder.generateTable(
                "CU8 Reportes",
                subtitulo,
                HEADERS,
                new ArrayList<>(rows)
        ));
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }
}
