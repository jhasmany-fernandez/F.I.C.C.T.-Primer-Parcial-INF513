package Negocio;

import Datos.DSeguimiento;
import Datos.DSeguimiento.UsuarioAcceso;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class NSeguimiento {

    public static final String ACCESS_DENIED
            = "Acceso denegado. No tiene permisos para gestionar seguimiento.";
    public static final String ACCESS_DENIED_CLIENTE
            = "Acceso denegado. El Cliente solo puede registrar y consultar sus propios seguimientos.";

    private final DSeguimiento dSeguimiento;

    public NSeguimiento() {
        dSeguimiento = new DSeguimiento();
    }

    public void validarAccesoConsulta(String correoRemitente) throws SQLException {
        obtenerUsuarioConsulta(correoRemitente);
    }

    public List<String[]> listar(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioConsulta(correoRemitente);
        if ("Cliente".equals(usuario.getRol())) {
            return dSeguimiento.listarPorCliente(usuario.getIdUsuario());
        }
        return dSeguimiento.listarActivos();
    }

    public List<String[]> mostrarPorRutinaCliente(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioConsulta(correoRemitente);
        requireSize(parametros, 1, "seguimiento mostrar [id_rutina_cliente]");
        int idRutinaCliente = parseId(parametros.get(0), "id_rutina_cliente");
        validarRutinaClienteActiva(idRutinaCliente);
        if ("Cliente".equals(usuario.getRol())
                && !dSeguimiento.rutinaClientePerteneceACliente(idRutinaCliente, usuario.getIdUsuario())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        return dSeguimiento.listarPorRutinaCliente(idRutinaCliente);
    }

    public String[] ver(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioConsulta(correoRemitente);
        requireSize(parametros, 1, "seguimiento ver [id_seguimiento]");
        int idSeguimiento = parseId(parametros.get(0), "id_seguimiento");
        if ("Cliente".equals(usuario.getRol())
                && !dSeguimiento.seguimientoPerteneceACliente(idSeguimiento, usuario.getIdUsuario())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        return dSeguimiento.obtenerPorId(idSeguimiento);
    }

    public int registrar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioConsulta(correoRemitente);
        requireSize(parametros, 5,
                "seguimiento agregar [id_rutina_cliente; fecha_seguimiento; peso; medidas; observacion]");
        int idRutinaCliente = parseId(parametros.get(0), "id_rutina_cliente");
        Date fechaSeguimiento = parseFecha(parametros.get(1), "fecha_seguimiento");
        BigDecimal peso = parsePeso(parametros.get(2));
        String medidas = normalizeOptional(parametros.get(3));
        String observacion = parametros.get(4);

        validarObservacion(observacion);
        validarRutinaClienteActiva(idRutinaCliente);
        if ("Cliente".equals(usuario.getRol())
                && !dSeguimiento.rutinaClientePerteneceACliente(idRutinaCliente, usuario.getIdUsuario())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        if (dSeguimiento.existeSeguimientoActivoDuplicado(idRutinaCliente, fechaSeguimiento)) {
            throw new IllegalArgumentException("Ya existe un seguimiento activo para esa rutina asignada y fecha");
        }
        return dSeguimiento.registrar(idRutinaCliente, fechaSeguimiento, peso, medidas, observacion.trim(), "EN_PROGRESO",
                usuario.getIdUsuario());
    }

    public void modificar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioConsulta(correoRemitente);
        // Formato: [id_seguimiento; peso; medidas; observacion]
        // medidas puede enviarse vacío: "seguimiento modificar [1; 70.5; ; nueva observacion]"
        requireSize(parametros, 4, "seguimiento modificar [id_seguimiento; peso; medidas; observacion]");
        int idSeguimiento = parseId(parametros.get(0), "id_seguimiento");
        BigDecimal peso = parsePeso(parametros.get(1));
        String medidas = normalizeOptional(parametros.get(2));
        String observacion = parametros.get(3);
        validarObservacion(observacion);
        validarSeguimientoActivo(idSeguimiento);
        if ("Cliente".equals(usuario.getRol())
                && !dSeguimiento.seguimientoPerteneceACliente(idSeguimiento, usuario.getIdUsuario())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        dSeguimiento.modificar(idSeguimiento, peso, medidas, observacion.trim(), "EN_PROGRESO", usuario.getIdUsuario());
    }

    public void eliminar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioConsulta(correoRemitente);
        requireSize(parametros, 1, "seguimiento eliminar [id_seguimiento]");
        int idSeguimiento = parseId(parametros.get(0), "id_seguimiento");
        validarSeguimientoActivo(idSeguimiento);
        if ("Cliente".equals(usuario.getRol())
                && !dSeguimiento.seguimientoPerteneceACliente(idSeguimiento, usuario.getIdUsuario())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        dSeguimiento.eliminarLogico(idSeguimiento, usuario.getIdUsuario());
    }

    public void completar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioConsulta(correoRemitente);
        requireSize(parametros, 1, "seguimiento completar [id_seguimiento]");
        int idSeguimiento = parseId(parametros.get(0), "id_seguimiento");
        validarSeguimientoActivo(idSeguimiento);
        if ("Cliente".equals(usuario.getRol())
                && !dSeguimiento.seguimientoPerteneceACliente(idSeguimiento, usuario.getIdUsuario())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        dSeguimiento.completar(idSeguimiento, usuario.getIdUsuario());
    }

    public List<String[]> misSeguimientos(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioCliente(correoRemitente);
        return dSeguimiento.listarPorCliente(usuario.getIdUsuario());
    }

    private UsuarioAcceso obtenerUsuarioConsulta(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario == null) {
            throw new SecurityException(ACCESS_DENIED);
        }
        if ("Propietario".equals(usuario.getRol())
                || "Secretaria".equals(usuario.getRol())
                || "Instructor".equals(usuario.getRol())
                || "Cliente".equals(usuario.getRol())) {
            return usuario;
        }
        throw new SecurityException(ACCESS_DENIED);
    }

    private UsuarioAcceso obtenerUsuarioCliente(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario != null && "Cliente".equals(usuario.getRol())) {
            return usuario;
        }
        throw new SecurityException(ACCESS_DENIED_CLIENTE);
    }

    private UsuarioAcceso obtenerUsuarioActivo(String correoRemitente) throws SQLException {
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            return null;
        }
        return dSeguimiento.obtenerUsuarioActivoPorEmail(correoRemitente.trim());
    }

    private void validarRutinaClienteActiva(int idRutinaCliente) throws SQLException {
        if (!dSeguimiento.existeRutinaClienteActiva(idRutinaCliente)) {
            throw new IllegalArgumentException("id_rutina_cliente debe existir y estar ACTIVO");
        }
    }

    private void validarSeguimientoActivo(int idSeguimiento) throws SQLException {
        if (!dSeguimiento.estaSeguimientoActivo(idSeguimiento)) {
            throw new IllegalArgumentException("No existe seguimiento activo con id " + idSeguimiento);
        }
    }

    private BigDecimal parsePeso(String value) {
        validarObligatorio(value, "peso");
        try {
            BigDecimal peso = new BigDecimal(value.trim());
            if (peso.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("peso debe ser mayor a 0");
            }
            return peso;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("peso debe ser numerico");
        }
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

    private void validarObservacion(String value) {
        validarObligatorio(value, "observacion");
    }

    private String normalizeOptional(String value) {
        return value == null ? "" : value.trim();
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
