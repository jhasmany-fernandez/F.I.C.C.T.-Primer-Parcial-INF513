package Datos;

import Conexion.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DMembresia {

    public List<String[]> listar() throws SQLException {
        // Devuelve todas las membresias del catalogo, activas e inactivas.
        String sql = "select id_membresia, nombre, descripcion, precio, duracion_dias, estado "
                + "from membresia order by id_membresia";
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(toRow(result));
            }
        }
        return rows;
    }

    public String[] obtenerPorId(int idMembresia) throws SQLException {
        // Busca una membresia puntual para mostrar su detalle por correo.
        String sql = "select id_membresia, nombre, descripcion, precio, duracion_dias, estado "
                + "from membresia where id_membresia = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idMembresia);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return toRow(result);
                }
            }
        }
        throw new SQLException("No existe membresia con id " + idMembresia);
    }

    public int agregar(String nombre, String descripcion, BigDecimal precio, int duracionDias) throws SQLException {
        // Toda membresia nueva entra activa al catalogo.
        String sql = "insert into membresia (nombre, descripcion, precio, duracion_dias, estado) "
                + "values (?, ?, ?, ?, 'ACTIVO')";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nombre);
            statement.setString(2, descripcion);
            statement.setBigDecimal(3, precio);
            statement.setInt(4, duracionDias);
            statement.executeUpdate();
            // Se devuelve el ID generado para incluirlo en la respuesta al usuario.
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void modificar(int idMembresia, String nombre, String descripcion, BigDecimal precio, int duracionDias)
            throws SQLException {
        // Solo actualiza membresias activas; las inactivas deben renovarse primero.
        String sql = "update membresia set nombre = ?, descripcion = ?, precio = ?, duracion_dias = ? "
                + "where id_membresia = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, descripcion);
            statement.setBigDecimal(3, precio);
            statement.setInt(4, duracionDias);
            statement.setInt(5, idMembresia);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe membresia activa con id " + idMembresia);
            }
        }
    }

    public void eliminarLogico(int idMembresia) throws SQLException {
        // Baja logica: conserva el historial y cambia estado a INACTIVO.
        String sql = "update membresia set estado = 'INACTIVO' "
                + "where id_membresia = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idMembresia);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe membresia activa con id " + idMembresia);
            }
        }
    }

    public boolean renovar(int idMembresia) throws SQLException {
        // Reactiva solo si actualmente esta INACTIVO.
        String sql = "update membresia set estado = 'ACTIVO' "
                + "where id_membresia = ? and estado = 'INACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idMembresia);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean existeNombre(String nombre) throws SQLException {
        // Valida nombres duplicados sin distinguir mayusculas/minusculas.
        String sql = "select 1 from membresia where lower(nombre) = lower(?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existeNombreEnOtraMembresia(String nombre, int idMembresia) throws SQLException {
        // Usado al modificar para evitar que dos membresias compartan el mismo nombre.
        String sql = "select 1 from membresia where lower(nombre) = lower(?) and id_membresia <> ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setInt(2, idMembresia);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existe(int idMembresia) throws SQLException {
        // Verifica existencia sin importar el estado.
        String sql = "select 1 from membresia where id_membresia = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idMembresia);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean estaActiva(int idMembresia) throws SQLException {
        // Verifica que la membresia exista y este disponible para modificar o eliminar.
        String sql = "select 1 from membresia where id_membresia = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idMembresia);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean tienePermisoGestionMembresia(String email) throws SQLException {
        // Permiso de CU2: correo registrado, usuario ACTIVO y rol Propietario o Secretaria.
        String sql = "select 1 "
                + "from usuario u join rol r on r.id_rol = u.id_rol "
                + "where lower(u.email) = lower(?) "
                + "and u.estado = 'ACTIVO' "
                + "and r.nombre_rol in ('Propietario', 'Secretaria')";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private String[] toRow(ResultSet result) throws SQLException {
        // Convierte el ResultSet al formato que HtmlBuilder usa para crear tablas.
        return new String[]{
            String.valueOf(result.getInt("id_membresia")),
            result.getString("nombre"),
            result.getString("descripcion"),
            result.getBigDecimal("precio").toPlainString(),
            String.valueOf(result.getInt("duracion_dias")),
            result.getString("estado")
        };
    }
}
