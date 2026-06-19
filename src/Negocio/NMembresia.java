package Negocio;

import Datos.DMembresia;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class NMembresia {

    // Capa de datos encargada de ejecutar las consultas SQL del CU2.
    private final DMembresia dMembresia;

    public NMembresia() {
        dMembresia = new DMembresia();
    }

    public void validarAcceso(String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
    }

    public List<String[]> listar(String correoRemitente) throws SQLException {
        // Listar tambien exige permiso, porque expone el catalogo completo de membresias.
        validarPermiso(correoRemitente);
        return dMembresia.listar();
    }

    public String[] obtenerPorId(List<String> parametros, String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
        // El comando ver solo acepta el id_membresia.
        requireSize(parametros, 1, "membresia ver [id_membresia]");
        return dMembresia.obtenerPorId(parseId(parametros.get(0), "id_membresia"));
    }

    public int agregar(List<String> parametros, String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
        // Formato esperado: nombre, descripcion, precio y duracion en dias.
        requireSize(parametros, 4, "membresia agregar [nombre; descripcion; precio; duracion_dias]");
        String nombre = parametros.get(0).trim();
        String descripcion = parametros.get(1).trim();
        BigDecimal precio = parsePrecio(parametros.get(2));
        int duracionDias = parseDuracion(parametros.get(3));

        validarObligatorio(nombre, "nombre");
        // Evita duplicar membresias por nombre, sin distinguir mayusculas/minusculas.
        if (dMembresia.existeNombre(nombre)) {
            throw new IllegalArgumentException("Ya existe una membresia con el nombre " + nombre);
        }

        return dMembresia.agregar(nombre, descripcion, precio, duracionDias);
    }

    public void modificar(List<String> parametros, String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
        // Solo se modifican membresias existentes y activas.
        requireSize(parametros, 5, "membresia modificar [id_membresia; nombre; descripcion; precio; duracion_dias]");
        int idMembresia = parseId(parametros.get(0), "id_membresia");
        String nombre = parametros.get(1).trim();
        String descripcion = parametros.get(2).trim();
        BigDecimal precio = parsePrecio(parametros.get(3));
        int duracionDias = parseDuracion(parametros.get(4));

        validarMembresiaActiva(idMembresia);
        validarObligatorio(nombre, "nombre");
        // Permite mantener el mismo nombre en la misma membresia, pero no repetirlo en otra.
        if (dMembresia.existeNombreEnOtraMembresia(nombre, idMembresia)) {
            throw new IllegalArgumentException("Ya existe otra membresia con el nombre " + nombre);
        }

        dMembresia.modificar(idMembresia, nombre, descripcion, precio, duracionDias);
    }

    public void eliminar(List<String> parametros, String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
        requireSize(parametros, 1, "membresia eliminar [id_membresia]");
        int idMembresia = parseId(parametros.get(0), "id_membresia");
        // Se valida primero para devolver un mensaje claro si no existe o ya esta inactiva.
        validarMembresiaActiva(idMembresia);
        dMembresia.eliminarLogico(idMembresia);
    }

    public boolean renovar(List<String> parametros, String correoRemitente) throws SQLException {
        validarPermiso(correoRemitente);
        requireSize(parametros, 1, "membresia renovar [id_membresia]");
        int idMembresia = parseId(parametros.get(0), "id_membresia");
        // Renovar reactiva una membresia del catalogo, no crea suscripciones ni pagos.
        if (!dMembresia.existe(idMembresia)) {
            throw new IllegalArgumentException("No existe membresia con id " + idMembresia);
        }
        if (dMembresia.estaActiva(idMembresia)) {
            return false;
        }
        dMembresia.renovar(idMembresia);
        return true;
    }

    private void validarPermiso(String correoRemitente) throws SQLException {
        // CU2 solo permite usuarios activos con rol Propietario o Secretaria.
        if (correoRemitente == null || correoRemitente.trim().isEmpty()
                || !dMembresia.tienePermisoGestionMembresia(correoRemitente.trim())) {
            throw new SecurityException("Acceso denegado. Solo el Propietario o la Secretaria pueden gestionar membresías.");
        }
    }

    private void validarMembresiaActiva(int idMembresia) throws SQLException {
        if (!dMembresia.estaActiva(idMembresia)) {
            throw new IllegalArgumentException("No existe membresia activa con id " + idMembresia);
        }
    }

    private void validarObligatorio(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " es obligatorio");
        }
    }

    private BigDecimal parsePrecio(String value) {
        try {
            // BigDecimal evita errores de precision al manejar importes monetarios.
            BigDecimal precio = new BigDecimal(value.trim());
            if (precio.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("precio debe ser mayor o igual a 0");
            }
            return precio;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("precio debe ser numerico");
        }
    }

    private int parseDuracion(String value) {
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
        // Fuerza el formato exacto del comando para evitar datos ambiguos por correo.
        if (parametros.size() != expected) {
            throw new IllegalArgumentException("Parametros invalidos. Uso: " + usage);
        }
    }
}
