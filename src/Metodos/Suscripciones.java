package Metodos;

import Negocio.NSuscripcion;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Suscripciones {

    private static final String[] HEADERS = {
        "ID", "ID Cliente", "Cliente", "Tipo", "ID Referencia", "Referencia",
        "Fecha inicio", "Fecha fin", "Fecha registro", "Estado",
        "Usuario creacion", "Usuario modificacion", "Fecha modificacion"
    };

    private final NSuscripcion nSuscripcion;

    public Suscripciones() {
        nSuscripcion = new NSuscripcion();
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
                case "vencer":
                    vencer(parametros, correo);
                    break;
                case "mis_suscripciones":
                    misSuscripciones(correo);
                    break;
                default:
                    nSuscripcion.validarAccesoGestion(correo);
                    enviar(correo, HtmlBuilder.generateError("CU4 Suscripciones",
                            "Comando no valido para CU4. Use: suscripcion ayuda"));
                    break;
            }
        } catch (SecurityException ex) {
            enviar(correo, HtmlBuilder.generateError("CU4 Suscripciones", ex.getMessage()));
        } catch (SQLException | IllegalArgumentException ex) {
            enviarError(correo, "CU4 Suscripciones", ex.getMessage());
        }
    }

    private void ayuda(String correo) throws SQLException {
        nSuscripcion.validarAccesoGestion(correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU4 Suscripciones", ""
                + "suscripcion ayuda<br>"
                + "suscripcion mostrar<br>"
                + "suscripcion agregar [id_cliente; tipo; id_referencia; fecha_inicio; fecha_fin]<br>"
                + "suscripcion ver [id_suscripcion]<br>"
                + "suscripcion modificar [id_suscripcion; fecha_inicio; fecha_fin]<br>"
                + "suscripcion eliminar [id_suscripcion]<br>"
                + "suscripcion vencer [id_suscripcion]<br>"
                + "suscripcion mis_suscripciones<br>"
                + "Nota: tipo puede ser MEMBRESIA o PAQUETE. eliminar realiza baja logica."));
    }

    private void mostrar(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU4 Suscripciones",
                "Listado de suscripciones",
                HEADERS,
                new ArrayList<>(nSuscripcion.listar(correo))
        ));
    }

    private void agregar(List<String> parametros, String correo) throws SQLException {
        int id = nSuscripcion.agregar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU4 Suscripciones",
                "Suscripcion registrada correctamente con ID " + id + "."));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(nSuscripcion.obtenerPorId(parametros, correo));
        enviar(correo, HtmlBuilder.generateTable("CU4 Suscripciones", "Detalle de suscripcion", HEADERS, rows));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        nSuscripcion.modificar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU4 Suscripciones", "Suscripcion modificada correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        nSuscripcion.eliminar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU4 Suscripciones",
                "Suscripcion marcada como INACTIVO correctamente."));
    }

    private void vencer(List<String> parametros, String correo) throws SQLException {
        nSuscripcion.vencer(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU4 Suscripciones", "Suscripcion marcada como VENCIDO correctamente."));
    }

    private void misSuscripciones(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU4 Suscripciones",
                "Mis suscripciones",
                HEADERS,
                new ArrayList<>(nSuscripcion.misSuscripciones(correo))
        ));
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }
}
