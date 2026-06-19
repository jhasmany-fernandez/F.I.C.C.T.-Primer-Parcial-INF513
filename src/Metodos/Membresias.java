package Metodos;

import Negocio.NMembresia;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Membresias {

    // Encabezados usados para armar las tablas HTML que se envian por correo.
    private static final String[] HEADERS = {"ID", "Nombre", "Descripcion", "Precio", "Duracion dias", "Estado"};
    private final NMembresia nMembresia;

    public Membresias() {
        nMembresia = new NMembresia();
    }

    public void ejecutar(String accion, List<String> parametros, String correo) {
        try {
            // Antes de ejecutar cualquier accion se valida que el remitente tenga permiso para CU2.
            nMembresia.validarAcceso(correo);
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
                    // Si la accion no pertenece a CU2, se responde con una guia breve.
                    enviar(correo, HtmlBuilder.generateError("CU2 Membresias", "Comando no valido para CU2. Use: membresia ayuda"));
                    break;
            }
        } catch (SecurityException ex) {
            // Error de permisos: el remitente no es Propietario ni Secretaria activa.
            enviar(correo, HtmlBuilder.generateError("CU2 Membresias", ex.getMessage()));
        } catch (SQLException | IllegalArgumentException ex) {
            // Errores de base de datos o de parametros invalidos.
            enviarError(correo, "CU2 Membresias", ex.getMessage());
        }
    }

    private void ayuda(String correo) {
        enviar(correo, HtmlBuilder.generateSuccess("CU2 Membresias", ""
                + "membresia ayuda<br>"
                + "membresia mostrar<br>"
                + "membresia agregar [nombre; descripcion; precio; duracion_dias]<br>"
                + "membresia ver [id_membresia]<br>"
                + "membresia modificar [id_membresia; nombre; descripcion; precio; duracion_dias]<br>"
                + "membresia eliminar [id_membresia]<br>"
                + "membresia renovar [id_membresia]<br>"
                + "Nota: eliminar realiza una baja logica cambiando estado a INACTIVO."));
    }

    private void mostrar(String correo) throws SQLException {
        // Consulta las membresias y las devuelve en una tabla HTML.
        enviar(correo, HtmlBuilder.generateTable(
                "CU2 Membresias",
                "Listado de membresias",
                HEADERS,
                new ArrayList<>(nMembresia.listar(correo))
        ));
    }

    private void agregar(List<String> parametros, String correo) throws SQLException {
        // Registra una membresia nueva con estado ACTIVO.
        int id = nMembresia.agregar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU2 Membresias", "Membresia registrada correctamente con ID " + id + "."));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        // Obtiene una sola membresia por ID y la envia como tabla de detalle.
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(nMembresia.obtenerPorId(parametros, correo));
        enviar(correo, HtmlBuilder.generateTable("CU2 Membresias", "Detalle de membresia", HEADERS, rows));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        // Modifica nombre, descripcion, precio y duracion de una membresia activa.
        nMembresia.modificar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU2 Membresias", "Membresia modificada correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        // Baja logica: no elimina la fila, solo cambia estado a INACTIVO.
        nMembresia.eliminar(parametros, correo);
        enviar(correo, HtmlBuilder.generateSuccess("CU2 Membresias", "Membresia marcada como INACTIVO correctamente."));
    }

    private void renovar(List<String> parametros, String correo) throws SQLException {
        // Reactiva una membresia INACTIVA; si ya esta ACTIVA, no realiza cambios.
        boolean renovada = nMembresia.renovar(parametros, correo);
        if (renovada) {
            enviar(correo, HtmlBuilder.generateSuccess("CU2 Membresias", "Membresia renovada correctamente."));
        } else {
            enviar(correo, HtmlBuilder.generateSuccess("CU2 Membresias", "La membresía ya se encuentra activa."));
        }
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }
}
