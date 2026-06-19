package Negocio;

import Datos.DRutina;
import Datos.DRutina.UsuarioAcceso;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class NRutina {

    public static final String ACCESS_DENIED_GESTION
            = "Acceso denegado. Solo el Propietario, la Secretaria o el Instructor pueden gestionar rutinas.";
    public static final String ACCESS_DENIED_CLIENTE
            = "Acceso denegado. El Cliente solo puede consultar sus rutinas asignadas.";

    private final DRutina dRutina;

    public NRutina() {
        dRutina = new DRutina();
    }

    public void validarAccesoGestion(String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
    }

    public List<String[]> listar(String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        return dRutina.listar();
    }

    public List<String[]> misRutinas(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioCliente(correoRemitente);
        return dRutina.listarPorCliente(usuario.getIdUsuario());
    }

    public String[] obtenerPorId(List<String> parametros, String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "rutina ver [id_rutina]");
        return dRutina.obtenerPorId(parseId(parametros.get(0), "id_rutina"));
    }

    public List<String[]> listarClientesAsignadosActivos(List<String> parametros, String correoRemitente)
            throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "rutina ver [id_rutina]");
        return dRutina.listarClientesAsignadosActivos(parseId(parametros.get(0), "id_rutina"));
    }

    public int agregar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 5, "rutina agregar [nombre; descripcion; objetivo; nivel; duracion_dias]");
        String nombre = parametros.get(0).trim();
        String descripcion = parametros.get(1).trim();
        String objetivo = parametros.get(2).trim();
        String nivel = parseNivel(parametros.get(3));
        int duracionDias = parseDuracion(parametros.get(4));

        validarObligatorio(nombre, "nombre");
        validarObligatorio(objetivo, "objetivo");
        if (dRutina.existeNombreActivo(nombre)) {
            throw new IllegalArgumentException("Ya existe una rutina activa con el nombre " + nombre);
        }
        return dRutina.agregar(nombre, descripcion, objetivo, nivel, duracionDias,
                usuario.getIdUsuario(), usuario.getIdUsuario());
    }

    public void modificar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 6,
                "rutina modificar [id_rutina; nombre; descripcion; objetivo; nivel; duracion_dias]");
        int idRutina = parseId(parametros.get(0), "id_rutina");
        String nombre = parametros.get(1).trim();
        String descripcion = parametros.get(2).trim();
        String objetivo = parametros.get(3).trim();
        String nivel = parseNivel(parametros.get(4));
        int duracionDias = parseDuracion(parametros.get(5));

        validarRutinaActiva(idRutina);
        validarObligatorio(nombre, "nombre");
        validarObligatorio(objetivo, "objetivo");
        if (dRutina.existeNombreActivoEnOtraRutina(nombre, idRutina)) {
            throw new IllegalArgumentException("Ya existe otra rutina activa con el nombre " + nombre);
        }
        dRutina.modificar(idRutina, nombre, descripcion, objetivo, nivel, duracionDias,
                usuario.getIdUsuario());
    }

    public void eliminar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "rutina eliminar [id_rutina]");
        int idRutina = parseId(parametros.get(0), "id_rutina");
        validarRutinaActiva(idRutina);
        dRutina.eliminarLogico(idRutina, usuario.getIdUsuario());
    }

    public int asignar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 4, "rutina asignar [id_rutina; id_cliente; fecha_inicio; fecha_fin]");
        int idRutina = parseId(parametros.get(0), "id_rutina");
        int idCliente = parseId(parametros.get(1), "id_cliente");
        Date fechaInicio = parseFecha(parametros.get(2), "fecha_inicio");
        Date fechaFin = parseFecha(parametros.get(3), "fecha_fin");

        validarRutinaActiva(idRutina);
        validarClienteActivo(idCliente);
        validarRangoFechas(fechaInicio, fechaFin);
        if (dRutina.existeAsignacionActiva(idRutina, idCliente)) {
            throw new IllegalArgumentException("Ya existe una asignacion activa para esa rutina y cliente");
        }
        return dRutina.asignar(idRutina, idCliente, fechaInicio, fechaFin, usuario.getIdUsuario());
    }

    public void desasignar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "rutina desasignar [id_rutina_cliente]");
        int idRutinaCliente = parseId(parametros.get(0), "id_rutina_cliente");
        if (!dRutina.existeAsignacionActivaPorId(idRutinaCliente)) {
            throw new IllegalArgumentException("No existe asignacion activa con id_rutina_cliente " + idRutinaCliente);
        }
        dRutina.desasignar(idRutinaCliente, usuario.getIdUsuario());
    }

    private UsuarioAcceso obtenerUsuarioGestion(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario == null) {
            throw new SecurityException(ACCESS_DENIED_GESTION);
        }
        if ("Propietario".equals(usuario.getRol())
                || "Secretaria".equals(usuario.getRol())
                || "Instructor".equals(usuario.getRol())) {
            return usuario;
        }
        if ("Cliente".equals(usuario.getRol())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        throw new SecurityException(ACCESS_DENIED_GESTION);
    }

    private UsuarioAcceso obtenerUsuarioCliente(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario != null && "Cliente".equals(usuario.getRol())) {
            return usuario;
        }
        throw new SecurityException("Acceso denegado. Solo el Cliente puede consultar sus rutinas asignadas.");
    }

    private UsuarioAcceso obtenerUsuarioActivo(String correoRemitente) throws SQLException {
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            return null;
        }
        return dRutina.obtenerUsuarioActivoPorEmail(correoRemitente.trim());
    }

    private void validarRutinaActiva(int idRutina) throws SQLException {
        if (!dRutina.estaRutinaActiva(idRutina)) {
            throw new IllegalArgumentException("No existe rutina activa con id " + idRutina);
        }
    }

    private void validarClienteActivo(int idCliente) throws SQLException {
        if (!dRutina.existeClienteActivo(idCliente)) {
            throw new IllegalArgumentException("id_cliente debe existir, estar ACTIVO y tener rol Cliente");
        }
    }

    private void validarRangoFechas(Date fechaInicio, Date fechaFin) {
        if (fechaFin.before(fechaInicio)) {
            throw new IllegalArgumentException("fecha_fin debe ser mayor o igual que fecha_inicio");
        }
    }

    private String parseNivel(String value) {
        validarObligatorio(value, "nivel");
        String nivel = value.trim().toUpperCase();
        if (!"BASICO".equals(nivel) && !"INTERMEDIO".equals(nivel) && !"AVANZADO".equals(nivel)) {
            throw new IllegalArgumentException("nivel debe ser BASICO, INTERMEDIO o AVANZADO");
        }
        return nivel;
    }

    private int parseDuracion(String value) {
        int duracion = parseId(value, "duracion_dias");
        if (duracion <= 0) {
            throw new IllegalArgumentException("duracion_dias debe ser mayor a 0");
        }
        return duracion;
    }

    private Date parseFecha(String value, String name) {
        validarObligatorio(value, name);
        try {
            return Date.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(name + " debe tener formato YYYY-MM-DD");
        }
    }

    private int parseId(String value, String name) {
        validarObligatorio(value, name);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " debe ser numerico");
        }
    }

    private void validarObligatorio(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " es obligatorio");
        }
    }

    private void requireSize(List<String> parametros, int expected, String usage) {
        if (parametros.size() != expected) {
            throw new IllegalArgumentException("Parametros invalidos. Uso: " + usage);
        }
    }
}
