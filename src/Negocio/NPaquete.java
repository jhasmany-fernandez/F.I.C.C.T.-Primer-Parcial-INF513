package Negocio;

import Datos.DPaquete;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class NPaquete {

    private final DPaquete dPaquete;

    public NPaquete() {
        dPaquete = new DPaquete();
    }

    public void validarAcceso(String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
    }

    public List<String[]> listar(String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
        return dPaquete.listar();
    }

    public String[] obtenerPorId(List<String> parametros, String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
        requireSize(parametros, 1, "paquete ver [id_paquete]");
        return dPaquete.obtenerPorId(parseId(parametros.get(0), "id_paquete"));
    }

    public int agregar(List<String> parametros, String correoRemitente) throws SQLException {
        int idUsuarioRemitente = validarPermiso(correoRemitente);
        requireSize(parametros, 4, "paquete agregar [nombre; descripcion; precio; duracion_dias]");
        String nombre = parametros.get(0).trim();
        String descripcion = parametros.get(1).trim();
        BigDecimal precio = parsePrecio(parametros.get(2));
        int duracionDias = parseDuracion(parametros.get(3));

        validarObligatorio(nombre, "nombre");
        if (dPaquete.existeNombreActivo(nombre)) {
            throw new IllegalArgumentException("Ya existe un paquete activo con el nombre " + nombre);
        }

        return dPaquete.agregar(nombre, descripcion, precio, duracionDias, idUsuarioRemitente);
    }

    public void modificar(List<String> parametros, String correoRemitente) throws SQLException {
        int idUsuarioRemitente = validarPermiso(correoRemitente);
        requireSize(parametros, 5, "paquete modificar [id_paquete; nombre; descripcion; precio; duracion_dias]");
        int idPaquete = parseId(parametros.get(0), "id_paquete");
        String nombre = parametros.get(1).trim();
        String descripcion = parametros.get(2).trim();
        BigDecimal precio = parsePrecio(parametros.get(3));
        int duracionDias = parseDuracion(parametros.get(4));

        validarPaqueteActivo(idPaquete);
        validarObligatorio(nombre, "nombre");
        if (dPaquete.existeNombreActivoEnOtroPaquete(nombre, idPaquete)) {
            throw new IllegalArgumentException("Ya existe otro paquete activo con el nombre " + nombre);
        }

        dPaquete.modificar(idPaquete, nombre, descripcion, precio, duracionDias, idUsuarioRemitente);
    }

    public void eliminar(List<String> parametros, String correoRemitente) throws SQLException {
        int idUsuarioRemitente = validarPermiso(correoRemitente);
        requireSize(parametros, 1, "paquete eliminar [id_paquete]");
        int idPaquete = parseId(parametros.get(0), "id_paquete");
        validarPaqueteActivo(idPaquete);
        dPaquete.eliminarLogico(idPaquete, idUsuarioRemitente);
    }

    public boolean renovar(List<String> parametros, String correoRemitente) throws SQLException {
        int idUsuarioRemitente = validarPermiso(correoRemitente);
        requireSize(parametros, 1, "paquete renovar [id_paquete]");
        int idPaquete = parseId(parametros.get(0), "id_paquete");
        if (!dPaquete.existe(idPaquete)) {
            throw new IllegalArgumentException("No existe paquete con id " + idPaquete);
        }
        if (dPaquete.estaActivo(idPaquete)) {
            return false;
        }
        return dPaquete.renovar(idPaquete, idUsuarioRemitente);
    }

    private int validarPermiso(String correoRemitente) throws SQLException {
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            throw new SecurityException("Acceso denegado. Solo el Propietario o la Secretaria pueden gestionar paquetes.");
        }
        int idUsuario = dPaquete.obtenerIdUsuarioGestionPaquete(correoRemitente.trim());
        if (idUsuario <= 0) {
            throw new SecurityException("Acceso denegado. Solo el Propietario o la Secretaria pueden gestionar paquetes.");
        }
        return idUsuario;
    }

    private void validarPaqueteActivo(int idPaquete) throws SQLException {
        if (!dPaquete.estaActivo(idPaquete)) {
            throw new IllegalArgumentException("No existe paquete activo con id " + idPaquete);
        }
    }

    private void validarObligatorio(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " es obligatorio");
        }
    }

    private BigDecimal parsePrecio(String value) {
        validarObligatorio(value, "precio");
        try {
            BigDecimal precio = new BigDecimal(value.trim());
            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("precio debe ser mayor a 0");
            }
            return precio;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("precio debe ser numerico");
        }
    }

    private int parseDuracion(String value) {
        validarObligatorio(value, "duracion_dias");
        try {
            int duracion = Integer.parseInt(value.trim());
            if (duracion <= 0) {
                throw new IllegalArgumentException("duracion_dias debe ser mayor a 0");
            }
            return duracion;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("duracion_dias debe ser numerico");
        }
    }

    private int parseId(String value, String name) {
        try {
            return Integer.parseInt(value.trim());
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
