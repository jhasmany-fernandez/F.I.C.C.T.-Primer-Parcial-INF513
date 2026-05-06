package Metodos;

import Negocio.NUsuario;
import Utils.Email;
import Utils.HtmlBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Usuarios {

    private static final String ACCESS_DENIED = "Acceso denegado. Solo el Propietario puede gestionar usuarios.";
    private static final String[] HEADERS = {"ID", "Nombre", "Email", "Rol", "Fecha registro", "Estado"};
    private final NUsuario nUsuario;

    public Usuarios() {
        nUsuario = new NUsuario();
    }

    public void ejecutar(String accion, List<String> parametros, String correo) {
        try {
            // CU1: solo un usuario activo con rol Propietario puede gestionar usuarios.
            if (!nUsuario.esPropietario(correo)) {
                enviar(correo, ACCESS_DENIED);
                return;
            }

            // CU1: selecciona la accion solicitada en el asunto del correo.
            switch (accion.toLowerCase()) {
                case "agregar":
                    agregar(parametros, correo);
                    break;
                case "modificar":
                    modificar(parametros, correo);
                    break;
                case "eliminar":
                    eliminar(parametros, correo);
                    break;
                case "ver":
                    ver(parametros, correo);
                    break;
                case "mostrar":
                    mostrar(correo);
                    break;
                case "ayuda":
                    ayuda(correo);
                    break;
                default:
                    enviar(correo, HtmlBuilder.generateError("CU1 Usuarios", "Comando no válido para CU1. Use: usuario ayuda"));
                    break;
            }
        } catch (SQLException | IllegalArgumentException ex) {
            enviarError(correo, "CU1 Usuarios", ex.getMessage());
        }
    }

    private void agregar(List<String> parametros, String correo) throws SQLException {
        // CU1 agregar: registra un usuario nuevo con rol existente.
        int id = nUsuario.guardar(parametros);
        enviar(correo, HtmlBuilder.generateSuccess("CU1 Usuarios", "Usuario registrado correctamente con ID " + id + "."));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        // CU1 modificar: actualiza datos principales y rol de un usuario activo.
        nUsuario.modificar(parametros);
        enviar(correo, HtmlBuilder.generateSuccess("CU1 Usuarios", "Usuario modificado correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        // CU1 eliminar: baja logica, no borra fisicamente el registro.
        nUsuario.eliminar(parametros);
        enviar(correo, HtmlBuilder.generateSuccess("CU1 Usuarios", "Usuario marcado como INACTIVO correctamente."));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        // CU1 ver: muestra el detalle de un usuario por id.
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(nUsuario.ver(parametros));
        enviar(correo, HtmlBuilder.generateTable("CU1 Usuarios", "Detalle de usuario", HEADERS, rows));
    }

    private void mostrar(String correo) throws SQLException {
        // CU1 mostrar: lista todos los usuarios registrados.
        enviar(correo, HtmlBuilder.generateTable("CU1 Usuarios", "Listado de usuarios", HEADERS, new ArrayList<>(nUsuario.listar())));
    }

    private void ayuda(String correo) {
        // CU1 ayuda: responde con el formato de comandos soportados.
        enviar(correo, HtmlBuilder.generateSuccess("CU1 Usuarios", ""
                + "usuario agregar [nombre; email; contrasena; nombre_rol]<br>"
                + "usuario modificar [id_usuario; nombre; email; contrasena; nombre_rol]<br>"
                + "usuario eliminar [id_usuario]<br>"
                + "usuario ver [id_usuario]<br>"
                + "usuario mostrar<br>"
                + "Nota: eliminar realiza una baja logica cambiando estado a INACTIVO."));
    }

    private void enviarError(String correo, String titulo, String mensaje) {
        enviar(correo, HtmlBuilder.generateError(titulo, mensaje));
    }

    private void enviar(String correo, String html) {
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }
}
