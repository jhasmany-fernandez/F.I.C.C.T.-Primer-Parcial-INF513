package Datos;

import Conexion.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DUsuario {

    public int guardar(String nombre, String email, String contrasena, int idRol) throws SQLException {
        // CU1 agregar: inserta usuario activo y devuelve el id generado.
        String sql = "insert into usuario (nombre, email, contrasena, id_rol, estado) values (?, ?, ?, ?, 'ACTIVO')";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nombre);
            statement.setString(2, email);
            statement.setString(3, contrasena);
            statement.setInt(4, idRol);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public void modificar(int idUsuario, String nombre, String email, String contrasena, int idRol) throws SQLException {
        // CU1 modificar: actualiza nombre, email, contrasena y rol.
        String sql = "update usuario set nombre = ?, email = ?, contrasena = ?, id_rol = ? where id_usuario = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, email);
            statement.setString(3, contrasena);
            statement.setInt(4, idRol);
            statement.setInt(5, idUsuario);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe usuario con id " + idUsuario);
            }
        }
    }

    public void eliminar(int idUsuario) throws SQLException {
        // CU1 eliminar: baja logica para conservar historial del usuario.
        String sql = "update usuario set estado = 'INACTIVO' where id_usuario = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUsuario);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe usuario activo con id " + idUsuario);
            }
        }
    }

    public String[] ver(int idUsuario) throws SQLException {
        String sql = "select u.id_usuario, u.nombre, u.email, r.nombre_rol, u.fecha_registro, u.estado "
                + "from usuario u join rol r on r.id_rol = u.id_rol where u.id_usuario = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUsuario);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return toRow(result);
                }
            }
        }
        throw new SQLException("No existe usuario con id " + idUsuario);
    }

    public List<String[]> listar() throws SQLException {
        String sql = "select u.id_usuario, u.nombre, u.email, r.nombre_rol, u.fecha_registro, u.estado "
                + "from usuario u join rol r on r.id_rol = u.id_rol order by u.id_usuario";
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

    public int getRolId(String nombreRol) throws SQLException {
        String sql = "select id_rol from rol where lower(nombre_rol) = lower(?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombreRol);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id_rol");
                }
            }
        }
        throw new SQLException("No existe el rol " + nombreRol);
    }

    public boolean existeEmail(String email) throws SQLException {
        String sql = "select 1 from usuario where lower(email) = lower(?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existeEmailEnOtroUsuario(String email, int idUsuario) throws SQLException {
        String sql = "select 1 from usuario where lower(email) = lower(?) and id_usuario <> ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            statement.setInt(2, idUsuario);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean estaActivo(int idUsuario) throws SQLException {
        String sql = "select 1 from usuario where id_usuario = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idUsuario);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean esPropietarioPorEmail(String email) throws SQLException {
        // CU1 acceso: confirma que el remitente sea Propietario activo.
        String sql = "select 1 "
                + "from usuario u join rol r on r.id_rol = u.id_rol "
                + "where lower(u.email) = lower(?) "
                + "and u.estado = 'ACTIVO' "
                + "and r.nombre_rol = 'Propietario'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private String[] toRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_usuario")),
            result.getString("nombre"),
            result.getString("email"),
            result.getString("nombre_rol"),
            String.valueOf(result.getDate("fecha_registro")),
            result.getString("estado")
        };
    }
}
