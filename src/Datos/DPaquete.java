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

public class DPaquete {

    public List<String[]> listar() throws SQLException {
        String sql = "select id_paquete, nombre, descripcion, precio, duracion_dias, fecha_registro, estado "
                + "from paquete order by id_paquete";
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(toListRow(result));
            }
        }
        return rows;
    }

    public String[] obtenerPorId(int idPaquete) throws SQLException {
        String sql = "select id_paquete, nombre, descripcion, precio, duracion_dias, fecha_registro, estado, "
                + "usuario_creacion, usuario_modificacion, fecha_modificacion "
                + "from paquete where id_paquete = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPaquete);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return toDetailRow(result);
                }
            }
        }
        throw new SQLException("No existe paquete con id " + idPaquete);
    }

    public int agregar(String nombre, String descripcion, BigDecimal precio, int duracionDias, int usuarioCreacion)
            throws SQLException {
        String sql = "insert into paquete "
                + "(nombre, descripcion, precio, duracion_dias, estado, usuario_creacion) "
                + "values (?, ?, ?, ?, 'ACTIVO', ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nombre);
            statement.setString(2, descripcion);
            statement.setBigDecimal(3, precio);
            statement.setInt(4, duracionDias);
            statement.setInt(5, usuarioCreacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para el paquete");
        }
    }

    public void modificar(int idPaquete, String nombre, String descripcion, BigDecimal precio, int duracionDias,
            int usuarioModificacion)
            throws SQLException {
        String sql = "update paquete set nombre = ?, descripcion = ?, precio = ?, duracion_dias = ?, "
                + "usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_paquete = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, descripcion);
            statement.setBigDecimal(3, precio);
            statement.setInt(4, duracionDias);
            statement.setInt(5, usuarioModificacion);
            statement.setInt(6, idPaquete);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe paquete activo con id " + idPaquete);
            }
        }
    }

    public void eliminarLogico(int idPaquete, int usuarioModificacion) throws SQLException {
        String sql = "update paquete set estado = 'INACTIVO', "
                + "usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_paquete = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idPaquete);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe paquete activo con id " + idPaquete);
            }
        }
    }

    public boolean existeNombreActivo(String nombre) throws SQLException {
        String sql = "select 1 from paquete where lower(nombre) = lower(?) and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existeNombreActivoEnOtroPaquete(String nombre, int idPaquete) throws SQLException {
        String sql = "select 1 from paquete "
                + "where lower(nombre) = lower(?) and id_paquete <> ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setInt(2, idPaquete);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean estaActivo(int idPaquete) throws SQLException {
        String sql = "select 1 from paquete where id_paquete = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPaquete);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existe(int idPaquete) throws SQLException {
        String sql = "select 1 from paquete where id_paquete = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPaquete);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean renovar(int idPaquete, int usuarioModificacion) throws SQLException {
        String sql = "update paquete set estado = 'ACTIVO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_paquete = ? and estado = 'INACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idPaquete);
            return statement.executeUpdate() > 0;
        }
    }

    public int obtenerIdUsuarioGestionPaquete(String email) throws SQLException {
        String sql = "select u.id_usuario "
                + "from usuario u join rol r on r.id_rol = u.id_rol "
                + "where lower(u.email) = lower(?) "
                + "and u.estado = 'ACTIVO' "
                + "and r.nombre_rol in ('Propietario', 'Secretaria')";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id_usuario");
                }
            }
        }
        return -1;
    }

    private String[] toListRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_paquete")),
            result.getString("nombre"),
            result.getString("descripcion"),
            result.getBigDecimal("precio").toPlainString(),
            String.valueOf(result.getInt("duracion_dias")),
            String.valueOf(result.getDate("fecha_registro")),
            result.getString("estado")
        };
    }

    private String[] toDetailRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_paquete")),
            result.getString("nombre"),
            result.getString("descripcion"),
            result.getBigDecimal("precio").toPlainString(),
            String.valueOf(result.getInt("duracion_dias")),
            String.valueOf(result.getDate("fecha_registro")),
            result.getString("estado"),
            getNullableInt(result, "usuario_creacion"),
            getNullableInt(result, "usuario_modificacion"),
            getNullableTimestamp(result, "fecha_modificacion")
        };
    }

    private String getNullableInt(ResultSet result, String column) throws SQLException {
        int value = result.getInt(column);
        if (result.wasNull()) {
            return "";
        }
        return String.valueOf(value);
    }

    private String getNullableTimestamp(ResultSet result, String column) throws SQLException {
        java.sql.Timestamp value = result.getTimestamp(column);
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }
}
