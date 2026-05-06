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
        // CU1 agregar: valida formato, duplicados y rol antes de guardar.
        requireSize(parametros, 4, "usuario agregar [nombre; email; contrasena; nombre_rol]");
        String nombre = parametros.get(0);
        String email = parametros.get(1);
        String contrasena = parametros.get(2);
        String nombreRol = parametros.get(3);

        validarObligatorio(nombre, "nombre");
        validarEmail(email);
        validarObligatorio(contrasena, "contrasena");
        validarObligatorio(nombreRol, "nombre_rol");
        if (dUsuario.existeEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con el email " + email);
        }

        int idRol = dUsuario.getRolId(nombreRol);
        return dUsuario.guardar(nombre.trim(), email.trim(), contrasena.trim(), idRol);
    }

    public void modificar(List<String> parametros) throws SQLException {
        // CU1 modificar: solo permite modificar usuarios activos.
        requireSize(parametros, 5, "usuario modificar [id_usuario; nombre; email; contrasena; nombre_rol]");
        int idUsuario = parseId(parametros.get(0), "id_usuario");
        String nombre = parametros.get(1);
        String email = parametros.get(2);
        String contrasena = parametros.get(3);
        String nombreRol = parametros.get(4);

        validarUsuarioActivo(idUsuario);
        validarObligatorio(nombre, "nombre");
        validarEmail(email);
        validarObligatorio(contrasena, "contrasena");
        validarObligatorio(nombreRol, "nombre_rol");
        if (dUsuario.existeEmailEnOtroUsuario(email, idUsuario)) {
            throw new IllegalArgumentException("Ya existe otro usuario con el email " + email);
        }

        int idRol = dUsuario.getRolId(nombreRol);
        dUsuario.modificar(idUsuario, nombre.trim(), email.trim(), contrasena.trim(), idRol);
    }

    public void eliminar(List<String> parametros) throws SQLException {
        // CU1 eliminar: transforma la eliminacion en cambio de estado.
        requireSize(parametros, 1, "usuario eliminar [id_usuario]");
        int idUsuario = parseId(parametros.get(0), "id_usuario");
        validarUsuarioActivo(idUsuario);
        dUsuario.eliminar(idUsuario);
    }

    public String[] ver(List<String> parametros) throws SQLException {
        requireSize(parametros, 1, "usuario ver [id_usuario]");
        return dUsuario.ver(parseId(parametros.get(0), "id_usuario"));
    }

    public List<String[]> listar() throws SQLException {
        return dUsuario.listar();
    }

    public boolean esPropietario(String correoRemitente) throws SQLException {
        // CU1 seguridad: el remitente del correo define si puede administrar usuarios.
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            return false;
        }
        return dUsuario.esPropietarioPorEmail(correoRemitente.trim());
    }

    private void validarEmail(String email) {
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
        if (!dUsuario.estaActivo(idUsuario)) {
            throw new IllegalArgumentException("No existe usuario activo con id " + idUsuario);
        }
    }

    private int parseId(String value, String name) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " debe ser numerico");
        }
    }

    private void requireSize(List<String> parametros, int expected, String usage) {
        if (parametros.size() != expected) {
            throw new IllegalArgumentException("Parametros invalidos. Uso: " + usage);
        }
    }
}
