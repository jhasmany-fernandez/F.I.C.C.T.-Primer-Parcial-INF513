package Negocio;

import Datos.DUsuario;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class NUsuario {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private final DUsuario dUsuario;

    public NUsuario() {
        dUsuario = new DUsuario();
    }

    public int guardar(List<String> parametros) throws SQLException {
        // Convierte parametros de correo en datos tipados y valida reglas antes de persistir.
        requireSize(parametros, 4, "usuario agregar [nombre; email; contrasena; nombre_rol]");
        String nombre = parametros.get(0).trim();
        String email = parametros.get(1).trim();
        String contrasena = parametros.get(2).trim();
        String nombreRol = parametros.get(3).trim();

        validarObligatorio(nombre, "nombre");
        validarEmail(email);
        validarObligatorio(contrasena, "contrasena");
        validarObligatorio(nombreRol, "nombre_rol");
        if (dUsuario.existeEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con el email " + email);
        }

        int idRol = dUsuario.getRolId(nombreRol);
        return dUsuario.guardar(nombre, email, contrasena, idRol);
    }

    public void modificar(List<String> parametros) throws SQLException {
        // Solo permite modificar usuarios activos y evita duplicar emails entre usuarios.
        requireSize(parametros, 5, "usuario modificar [id_usuario; nombre; email; contrasena; nombre_rol]");
        int idUsuario = parseId(parametros.get(0), "id_usuario");
        String nombre = parametros.get(1).trim();
        String email = parametros.get(2).trim();
        String contrasena = parametros.get(3).trim();
        String nombreRol = parametros.get(4).trim();

        validarUsuarioActivo(idUsuario);
        validarObligatorio(nombre, "nombre");
        validarEmail(email);
        validarObligatorio(contrasena, "contrasena");
        validarObligatorio(nombreRol, "nombre_rol");
        if (dUsuario.existeEmailEnOtroUsuario(email, idUsuario)) {
            throw new IllegalArgumentException("Ya existe otro usuario con el email " + email);
        }

        int idRol = dUsuario.getRolId(nombreRol);
        dUsuario.modificar(idUsuario, nombre, email, contrasena, idRol);
    }

    public void eliminar(List<String> parametros) throws SQLException {
        // La capa negocio protege contra bajas sobre usuarios inexistentes o ya inactivos.
        requireSize(parametros, 1, "usuario eliminar [id_usuario]");
        int idUsuario = parseId(parametros.get(0), "id_usuario");
        validarUsuarioActivo(idUsuario);
        dUsuario.eliminar(idUsuario);
    }

    public String[] ver(List<String> parametros) throws SQLException {
        // Reutiliza el parseo comun de IDs para mantener mensajes de error consistentes.
        requireSize(parametros, 1, "usuario ver [id_usuario]");
        return dUsuario.ver(parseId(parametros.get(0), "id_usuario"));
    }

    public List<String[]> listar() throws SQLException {
        // En listar no agrega reglas extra; solo expone los datos para renderizado.
        return dUsuario.listar();
    }

    public boolean esPropietario(String correoRemitente) throws SQLException {
        // El permiso de CU1 se basa en el remitente, no en un login interactivo.
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            return false;
        }
        return dUsuario.esPropietarioPorEmail(correoRemitente.trim());
    }

    private void validarEmail(String email) {
        // Se separa para reutilizar la misma regla en alta y modificacion.
        validarObligatorio(email, "email");
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("El email no tiene un formato valido");
        }
    }

    private void validarObligatorio(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " es obligatorio");
        }
    }

    private void validarUsuarioActivo(int idUsuario) throws SQLException {
        // La existencia valida para CU1 incluye estado ACTIVO.
        if (!dUsuario.estaActivo(idUsuario)) {
            throw new IllegalArgumentException("No existe usuario activo con id " + idUsuario);
        }
    }

    private int parseId(String value, String name) {
        // Normaliza el error cuando llega un ID no numerico desde el asunto del correo.
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " debe ser numerico");
        }
    }

    private void requireSize(List<String> parametros, int expected, String usage) {
        // Estandariza el error de formato para todos los comandos del caso de uso.
        if (parametros.size() != expected) {
            throw new IllegalArgumentException("Parametros invalidos. Uso: " + usage);
        }
    }
}
