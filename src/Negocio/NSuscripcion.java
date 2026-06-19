package Negocio;

import Datos.DSuscripcion;
import Datos.DSuscripcion.UsuarioAcceso;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class NSuscripcion {

    public static final String ACCESS_DENIED_GESTION
            = "Acceso denegado. Solo el Propietario o la Secretaria pueden gestionar suscripciones.";
    public static final String ACCESS_DENIED_CLIENTE
            = "Acceso denegado. El Cliente solo puede consultar sus propias suscripciones.";

    private final DSuscripcion dSuscripcion;

    public NSuscripcion() {
        dSuscripcion = new DSuscripcion();
    }

    public void validarAccesoGestion(String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
    }

    public List<String[]> listar(String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        return dSuscripcion.listar();
    }

    public List<String[]> misSuscripciones(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioCliente(correoRemitente);
        return dSuscripcion.listarPorCliente(usuario.getIdUsuario());
    }

    public String[] obtenerPorId(List<String> parametros, String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "suscripcion ver [id_suscripcion]");
        return dSuscripcion.obtenerPorId(parseId(parametros.get(0), "id_suscripcion"));
    }

    public int agregar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 5, "suscripcion agregar [id_cliente; tipo; id_referencia; fecha_inicio; fecha_fin]");

        int idCliente = parseId(parametros.get(0), "id_cliente");
        String tipo = parseTipo(parametros.get(1));
        int idReferencia = parseId(parametros.get(2), "id_referencia");
        Date fechaInicio = parseFecha(parametros.get(3), "fecha_inicio");
        Date fechaFin = parseFecha(parametros.get(4), "fecha_fin");

        validarClienteActivo(idCliente);
        validarReferenciaActiva(tipo, idReferencia);
        validarRangoFechas(fechaInicio, fechaFin);

        return dSuscripcion.agregar(idCliente, tipo, idReferencia, fechaInicio, fechaFin, usuario.getIdUsuario());
    }

    public void modificar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        int idSuscripcion;
        int idCliente;
        String tipo;
        int idReferencia;
        Date fechaInicio;
        Date fechaFin;

        if (parametros.size() == 3) {
            idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
            String[] actual = dSuscripcion.obtenerPorId(idSuscripcion);
            idCliente = parseId(actual[1], "id_cliente");
            tipo = parseTipo(actual[3]);
            idReferencia = parseId(actual[4], "id_referencia");
            fechaInicio = parseFecha(parametros.get(1), "fecha_inicio");
            fechaFin = parseFecha(parametros.get(2), "fecha_fin");
        } else {
            requireSize(parametros, 6,
                    "suscripcion modificar [id_suscripcion; id_cliente; tipo; id_referencia; fecha_inicio; fecha_fin]");
            idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
            idCliente = parseId(parametros.get(1), "id_cliente");
            tipo = parseTipo(parametros.get(2));
            idReferencia = parseId(parametros.get(3), "id_referencia");
            fechaInicio = parseFecha(parametros.get(4), "fecha_inicio");
            fechaFin = parseFecha(parametros.get(5), "fecha_fin");
        }

        validarSuscripcionActiva(idSuscripcion);
        validarClienteActivo(idCliente);
        validarReferenciaActiva(tipo, idReferencia);
        validarRangoFechas(fechaInicio, fechaFin);

        dSuscripcion.modificar(idSuscripcion, idCliente, tipo, idReferencia, fechaInicio, fechaFin,
                usuario.getIdUsuario());
    }

    public void eliminar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "suscripcion eliminar [id_suscripcion]");
        int idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
        validarSuscripcionActiva(idSuscripcion);
        dSuscripcion.eliminarLogico(idSuscripcion, usuario.getIdUsuario());
    }

    public void vencer(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "suscripcion vencer [id_suscripcion]");
        int idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
        validarSuscripcionActiva(idSuscripcion);
        dSuscripcion.vencer(idSuscripcion, usuario.getIdUsuario());
    }

    private UsuarioAcceso obtenerUsuarioGestion(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario == null) {
            throw new SecurityException(ACCESS_DENIED_GESTION);
        }
        if ("Propietario".equals(usuario.getRol()) || "Secretaria".equals(usuario.getRol())) {
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
        throw new SecurityException("Acceso denegado. Solo el Cliente puede consultar sus propias suscripciones.");
    }

    private UsuarioAcceso obtenerUsuarioActivo(String correoRemitente) throws SQLException {
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            return null;
        }
        return dSuscripcion.obtenerUsuarioActivoPorEmail(correoRemitente.trim());
    }

    private void validarClienteActivo(int idCliente) throws SQLException {
        if (!dSuscripcion.existeClienteActivo(idCliente)) {
            throw new IllegalArgumentException("id_cliente debe existir, estar ACTIVO y tener rol Cliente");
        }
    }

    private void validarReferenciaActiva(String tipo, int idReferencia) throws SQLException {
        if (!dSuscripcion.existeReferenciaActiva(tipo, idReferencia)) {
            if ("MEMBRESIA".equals(tipo)) {
                throw new IllegalArgumentException("id_referencia debe corresponder a una membresia activa");
            }
            throw new IllegalArgumentException("id_referencia debe corresponder a un paquete activo");
        }
    }

    private void validarSuscripcionActiva(int idSuscripcion) throws SQLException {
        if (!dSuscripcion.estaActiva(idSuscripcion)) {
            throw new IllegalArgumentException("No existe suscripcion activa con id " + idSuscripcion);
        }
    }

    private void validarRangoFechas(Date fechaInicio, Date fechaFin) {
        if (fechaFin.before(fechaInicio)) {
            throw new IllegalArgumentException("fecha_fin debe ser mayor o igual que fecha_inicio");
        }
    }

    private String parseTipo(String value) {
        validarObligatorio(value, "tipo");
        String tipo = value.trim().toUpperCase();
        if (!"MEMBRESIA".equals(tipo) && !"PAQUETE".equals(tipo)) {
            throw new IllegalArgumentException("tipo debe ser MEMBRESIA o PAQUETE");
        }
        return tipo;
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
