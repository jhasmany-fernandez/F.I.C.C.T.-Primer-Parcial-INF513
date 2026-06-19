package Negocio;

import Datos.DReporte;
import Datos.DReporte.UsuarioAcceso;
import java.sql.SQLException;
import java.util.List;

public class NReporte {

    public static final String ACCESS_DENIED
            = "Acceso denegado. Solo el Propietario o la Secretaria pueden consultar reportes.";
    public static final String ACCESS_DENIED_GENERAL
            = "Acceso denegado. Solo el Propietario puede consultar el reporte general.";

    private final DReporte dReporte;

    public NReporte() {
        dReporte = new DReporte();
    }

    public List<String[]> usuarios(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reporteUsuarios(), correoRemitente, usuario.getRol());
    }

    public List<String[]> membresias(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reporteMembresias(), correoRemitente, usuario.getRol());
    }

    public List<String[]> paquetes(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reportePaquetes(), correoRemitente, usuario.getRol());
    }

    public List<String[]> suscripciones(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reporteSuscripciones(), correoRemitente, usuario.getRol());
    }

    public List<String[]> pagos(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reportePagos(), correoRemitente, usuario.getRol());
    }

    public List<String[]> rutinas(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reporteRutinas(), correoRemitente, usuario.getRol());
    }

    public List<String[]> seguimientos(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reporteSeguimientos(), correoRemitente, usuario.getRol());
    }

    public List<String[]> estadisticas(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioReportes(correoRemitente);
        return dReporte.withMetadata(dReporte.reporteEstadisticas(), correoRemitente, usuario.getRol());
    }

    public List<String[]> general(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGeneral(correoRemitente);
        return dReporte.withMetadata(dReporte.reporteGeneral(), correoRemitente, usuario.getRol());
    }

    public void validarAccesoReportes(String correoRemitente) throws SQLException {
        obtenerUsuarioReportes(correoRemitente);
    }

    private UsuarioAcceso obtenerUsuarioReportes(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario == null) {
            throw new SecurityException(ACCESS_DENIED);
        }
        if ("Propietario".equals(usuario.getRol()) || "Secretaria".equals(usuario.getRol())) {
            return usuario;
        }
        throw new SecurityException(ACCESS_DENIED);
    }

    private UsuarioAcceso obtenerUsuarioGeneral(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario != null && "Propietario".equals(usuario.getRol())) {
            return usuario;
        }
        throw new SecurityException(ACCESS_DENIED_GENERAL);
    }

    private UsuarioAcceso obtenerUsuarioActivo(String correoRemitente) throws SQLException {
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            return null;
        }
        return dReporte.obtenerUsuarioActivoPorEmail(correoRemitente.trim());
    }
}
