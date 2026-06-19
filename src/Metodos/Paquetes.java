package Metodos;

import Negocio.NPaquete;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Paquetes {

    private static final String[] LIST_HEADERS = {
        "ID", "Nombre", "Descripcion", "Precio", "Duracion dias", "Fecha registro", "Estado"
    };
    private static final String[] DETAIL_HEADERS = {
        "ID", "Nombre", "Descripcion", "Precio", "Duracion dias", "Fecha registro", "Estado",
        "Usuario creacion", "Usuario modificacion", "Fecha modificacion"
    };
    private final NPaquete nPaquete;

    public Paquetes() {
        nPaquete = new NPaquete();
    }

    public void ejecutar(String accion, List<String> parametros, String correo) {
        try {
            nPaquete.validarAcceso(correo);
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
                case "renovar":
                    renovar(parametros, correo);
                    break;
                default:
                    enviar(correo, HtmlBuilder.generateError("CU3 Paquetes", "Comando no valido para CU3. Use: paquete ayuda"));
                    break;
            }
        } catch (SecurityException ex) {
            enviar(correo, HtmlBuilder.generateError("CU3 Paquetes", ex.getMessage()));
        } catch (SQLException | IllegalArgumentException ex) {
            enviarError(correo, "CU3 Paquetes", ex.getMessage());
        }
    }

    private void ayuda(String correo) {
        enviar(correo, HtmlBuilder.generateSuccess("CU3 Paquetes", ""
                + "paquete ayuda<br>"
                + "paquete mostrar<br>"
                + "paquete agregar [nombre; descripcion; precio; duracion_dias]<br>"
                + "paquete ver [id_paquete]<br>"
                + "paquete modificar [id_paquete; nombre; descripcion; precio; duracion_dias]<br>"
                + "paquete eliminar [id_paquete]<br>"
                + "paquete renovar [id_paquete]<br>"
                + "Nota: eliminar realiza una baja logica cambiando estado a INACTIVO."));
    }

    private void mostrar(String correo) throws SQLException {
        enviar(correo, HtmlBuilder.generateTable(
                "CU3 Paquetes",
                "Listado de paquetes",
                LIST_HEADERS,
                new ArrayList<>(nPaquete.listar(correo))
        ));
    }

    private void agregar(List<String> parametros, String correo) throws SQLException {
        int id = nPaquete.agregar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU3 Paquetes", "Paquete registrado correctamente con ID " + id + "."));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(nPaquete.obtenerPorId(parametros, correo));
        enviar(correo, HtmlBuilder.generateTable("CU3 Paquetes", "Detalle de paquete", DETAIL_HEADERS, rows));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        nPaquete.modificar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU3 Paquetes", "Paquete modificado correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        nPaquete.eliminar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU3 Paquetes", "Paquete marcado como INACTIVO correctamente."));
    }

    private void renovar(List<String> parametros, String correo) throws SQLException {
        boolean renovado = nPaquete.renovar(parametros, correo);
        if (renovado) {
            enviar(correo, HtmlBuilder.generateSuccess("CU3 Paquetes", "Paquete renovado correctamente."));
        } else {
            enviar(correo, HtmlBuilder.generateSuccess("CU3 Paquetes", "El paquete ya se encuentra ACTIVO."));
        }
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }
}
