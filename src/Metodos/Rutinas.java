package Metodos;

import Negocio.NRutina;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Rutinas {

    private static final String[] RUTINA_HEADERS = {
        "ID", "Nombre", "Descripcion", "Objetivo", "Nivel", "Duracion dias",
        "Fecha registro", "Estado", "Usuario creacion", "Usuario modificacion", "Fecha modificacion"
    };
    private static final String[] ASIGNADO_HEADERS = {
        "ID Asignacion", "ID Rutina", "ID Cliente", "Cliente", "Email", "Fecha inicio",
        "Fecha fin", "Estado", "Usuario creacion", "Usuario modificacion", "Fecha modificacion"
    };
    private static final String[] MIS_RUTINAS_HEADERS = {
        "ID Asignacion", "ID Rutina", "Nombre", "Descripcion", "Objetivo", "Nivel",
        "Duracion dias", "Fecha inicio", "Fecha fin", "Estado"
    };

    private final NRutina nRutina;

    public Rutinas() {
        nRutina = new NRutina();
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
                case "ver":
                    ver(parametros, correo);
                    break;
                case "modificar":
                    modificar(parametros, correo);
                    break;
                case "eliminar":
                    eliminar(parametros, correo);
                    break;
                case "asignar":
                    asignar(parametros, correo);
                    break;
                case "desasignar":
                    desasignar(parametros, correo);
                    break;
                case "mis_rutinas":
                    misRutinas(correo);
                    break;
                default:
                    nRutina.validarAccesoGestion(correo);
                    enviar(correo, HtmlBuilder.generateError("CU5 Rutinas",
                            "Comando no valido para CU5. Use: rutina ayuda"));
                    break;
            }
        } catch (SecurityException ex) {
            enviar(correo, HtmlBuilder.generateError("CU5 Rutinas", ex.getMessage()));
        } catch (SQLException | IllegalArgumentException ex) {
            enviarError(correo, "CU5 Rutinas", ex.getMessage());
        }
    }

    private void ayuda(String correo) throws SQLException {
        nRutina.validarAccesoGestion(correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU5 Rutinas", ""
                + "rutina ayuda<br>"
                + "rutina mostrar<br>"
                + "rutina agregar [nombre; descripcion; objetivo; nivel; duracion_dias]<br>"
                + "rutina ver [id_rutina]<br>"
                + "rutina modificar [id_rutina; nombre; descripcion; objetivo; nivel; duracion_dias]<br>"
                + "rutina eliminar [id_rutina]<br>"
                + "rutina asignar [id_rutina; id_cliente; fecha_inicio; fecha_fin]<br>"
                + "rutina desasignar [id_rutina_cliente]<br>"
                + "rutina mis_rutinas<br>"
                + "Niveles: BASICO, INTERMEDIO, AVANZADO. eliminar realiza baja logica."));
    }

    private void mostrar(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU5 Rutinas",
                "Listado de rutinas activas",
                RUTINA_HEADERS,
                new ArrayList<>(nRutina.listar(correo))
        ));
    }

    private void agregar(List<String> parametros, String correo) throws SQLException {
        int id = nRutina.agregar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU5 Rutinas",
                "Rutina registrada correctamente con ID " + id + "."));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        ArrayList<String[]> rutinaRows = new ArrayList<>();
        rutinaRows.add(nRutina.obtenerPorId(parametros, correo));
        ArrayList<String[]> asignados = new ArrayList<>(nRutina.listarClientesAsignadosActivos(parametros, correo));
        enviar(correo, generarDetalleConAsignados(rutinaRows, asignados));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        nRutina.modificar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU5 Rutinas", "Rutina modificada correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        nRutina.eliminar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU5 Rutinas",
                "Rutina marcada como INACTIVO correctamente."));
    }

    private void asignar(List<String> parametros, String correo) throws SQLException {
        int id = nRutina.asignar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU5 Rutinas",
                "Rutina asignada correctamente con ID " + id + "."));
    }

    private void desasignar(List<String> parametros, String correo) throws SQLException {
        nRutina.desasignar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU5 Rutinas",
                "Asignacion de rutina marcada como INACTIVO correctamente."));
    }

    private void misRutinas(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU5 Rutinas",
                "Mis rutinas asignadas",
                MIS_RUTINAS_HEADERS,
                new ArrayList<>(nRutina.misRutinas(correo))
        ));
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }

    private String generarDetalleConAsignados(ArrayList<String[]> rutinaRows, ArrayList<String[]> asignadosRows) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<style>")
                .append("body{font-family:Arial,sans-serif;color:#111827}")
                .append("table{border-collapse:collapse;width:100%;margin-bottom:24px}")
                .append("th,td{border:1px solid #d1d5db;padding:8px;text-align:left}")
                .append("th{background:#f3f4f6}")
                .append("</style></head><body>")
                .append("<h1>CU5 Rutinas</h1>")
                .append("<p>Detalle de rutina</p>");
        appendTable(html, RUTINA_HEADERS, rutinaRows);
        html.append("<h2>Clientes asignados activos</h2>");
        appendTable(html, ASIGNADO_HEADERS, asignadosRows);
        html.append("</body></html>");
        return html.toString();
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
