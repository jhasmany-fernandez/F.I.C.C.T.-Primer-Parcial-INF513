package Metodos;

import Negocio.NSeguimiento;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Seguimientos {

    private static final String[] HEADERS = {
        "ID", "ID Rutina Cliente", "ID Rutina", "Rutina", "ID Cliente", "Cliente",
        "Email Cliente", "Fecha seguimiento", "Peso", "Medidas", "Observacion", "Estado",
        "Estado logico", "Usuario creacion", "Usuario modificacion", "Fecha modificacion"
    };

    private final NSeguimiento nSeguimiento;

    public Seguimientos() {
        nSeguimiento = new NSeguimiento();
    }

    public void ejecutar(String accion, List<String> parametros, String correo) {
        try {
            switch (accion.toLowerCase()) {
                case "ayuda":
                    ayuda(correo);
                    break;
                case "mostrar":
                    if (parametros.isEmpty()) {
                        mostrar(correo);
                    } else {
                        mostrar(parametros, correo);
                    }
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
                case "registrar":
                    agregar(parametros, correo);
                    break;
                case "completar":
                    completar(parametros, correo);
                    break;
                case "mis_seguimientos":
                    misSeguimientos(correo);
                    break;
                default:
                    nSeguimiento.validarAccesoConsulta(correo);
                    enviar(correo, HtmlBuilder.generateError("CU6 Seguimiento",
                            "Comando no valido para CU6. Use: seguimiento ayuda"));
                    break;
            }
        } catch (SecurityException ex) {
            enviar(correo, HtmlBuilder.generateError("CU6 Seguimiento", ex.getMessage()));
        } catch (SQLException | IllegalArgumentException ex) {
            enviarError(correo, "CU6 Seguimiento", ex.getMessage());
        }
    }

    private void ayuda(String correo) throws SQLException {
        nSeguimiento.validarAccesoConsulta(correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU6 Seguimiento", ""
                + "seguimiento ayuda<br>"
                + "seguimiento mostrar [id_rutina_cliente]<br>"
                + "seguimiento agregar [id_rutina_cliente; fecha_seguimiento; peso; medidas; observacion]<br>"
                + "seguimiento ver [id_seguimiento]<br>"
                + "seguimiento modificar [id_seguimiento; peso; medidas; observacion]<br>"
                + "seguimiento completar [id_seguimiento]<br>"
                + "seguimiento eliminar [id_seguimiento]<br>"
                + "seguimiento mis_seguimientos<br>"
                + "Estados: EN_PROGRESO, COMPLETADO, PAUSADO. eliminar realiza baja logica."));
    }

    private void mostrar(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU6 Seguimiento",
                "Listado de seguimientos activos",
                HEADERS,
                new ArrayList<>(nSeguimiento.listar(correo))
        ));
    }

    private void agregar(List<String> parametros, String correo) throws SQLException {
        int id = nSeguimiento.registrar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU6 Seguimiento",
                "Seguimiento registrado correctamente con ID " + id + "."));
    }

    private void mostrar(List<String> parametros, String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU6 Seguimiento",
                "Seguimientos por rutina_cliente",
                HEADERS,
                new ArrayList<>(nSeguimiento.mostrarPorRutinaCliente(parametros, correo))
        ));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(nSeguimiento.ver(parametros, correo));
        enviar(correo, HtmlBuilder.generateTable("CU6 Seguimiento", "Detalle de seguimiento", HEADERS, rows));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        nSeguimiento.modificar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU6 Seguimiento", "Seguimiento modificado correctamente."));
    }

    private void completar(List<String> parametros, String correo) throws SQLException {
        nSeguimiento.completar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU6 Seguimiento", "Seguimiento completado correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        nSeguimiento.eliminar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU6 Seguimiento",
                "Seguimiento marcado como INACTIVO correctamente."));
    }

    private void misSeguimientos(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU6 Seguimiento",
                "Mis seguimientos",
                HEADERS,
                new ArrayList<>(nSeguimiento.misSeguimientos(correo))
        ));
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }
}
