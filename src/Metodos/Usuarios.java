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
            // Esta capa traduce un comando de correo a una accion del caso de uso.
            if (!nUsuario.esPropietario(correo)) {
                enviar(correo, ACCESS_DENIED);
                return;
            }

            // Todas las respuestas del CU vuelven al mismo remitente en formato HTML.
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
        // Confirma al usuario administrador el ID generado para el nuevo registro.
        int id = nUsuario.guardar(parametros);
        enviar(correo, HtmlBuilder.generateSuccess("CU1 Usuarios", "Usuario registrado correctamente con ID " + id + "."));
    }

    private void modificar(List<String> parametros, String correo) throws SQLException {
        // La capa Metodos solo coordina: validaciones y persistencia viven mas abajo.
        nUsuario.modificar(parametros);
        enviar(correo, HtmlBuilder.generateSuccess("CU1 Usuarios", "Usuario modificado correctamente."));
    }

    private void eliminar(List<String> parametros, String correo) throws SQLException {
        // La eliminacion visible para el usuario es una baja logica.
        nUsuario.eliminar(parametros);
        enviar(correo, HtmlBuilder.generateSuccess("CU1 Usuarios", "Usuario marcado como INACTIVO correctamente."));
    }

    private void ver(List<String> parametros, String correo) throws SQLException {
        // Empaqueta una sola fila para reutilizar el generador de tablas HTML.
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(nUsuario.ver(parametros));
        enviar(correo, HtmlBuilder.generateTable("CU1 Usuarios", "Detalle de usuario", HEADERS, rows));
    }

    private void mostrar(String correo) throws SQLException {
        // Lista completa del CU para administracion por correo.
        enviar(correo, HtmlBuilder.generateTable("CU1 Usuarios", "Listado de usuarios", HEADERS, new ArrayList<>(nUsuario.listar())));
    }

    private void ayuda(String correo) {
        // Sirve como contrato rapido de uso del comando `usuario`.
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
        // Centraliza el envio para que todas las salidas del CU usen el mismo asunto.
        Email.sendEmail(new Email(correo, Email.SUBJECT, html));
    }
}
