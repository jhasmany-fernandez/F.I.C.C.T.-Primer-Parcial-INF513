package Datos;

import Conexion.DBConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DSuscripcion {

    public static class UsuarioAcceso {

        private final int idUsuario;
        private final String rol;

        public UsuarioAcceso(int idUsuario, String rol) {
            this.idUsuario = idUsuario;
            this.rol = rol;
        }

        public int getIdUsuario() {
            return idUsuario;
        }

        public String getRol() {
            return rol;
        }
    }

    public UsuarioAcceso obtenerUsuarioActivoPorEmail(String email) throws SQLException {
        String sql = "select u.id_usuario, r.nombre_rol "
                + "from usuario u join rol r on r.id_rol = u.id_rol "
                + "where lower(u.email) = lower(?) and u.estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new UsuarioAcceso(result.getInt("id_usuario"), result.getString("nombre_rol"));
                }
            }
        }
        return null;
    }

    public boolean existeClienteActivo(int idCliente) throws SQLException {
        String sql = "select 1 "
                + "from usuario u join rol r on r.id_rol = u.id_rol "
                + "where u.id_usuario = ? "
                + "and u.estado = 'ACTIVO' "
                + "and r.nombre_rol = 'Cliente'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existeReferenciaActiva(String tipo, int idReferencia) throws SQLException {
        String sql;
        if ("MEMBRESIA".equals(tipo)) {
            sql = "select 1 from membresia where id_membresia = ? and estado = 'ACTIVO'";
        } else {
            sql = "select 1 from paquete where id_paquete = ? and estado = 'ACTIVO'";
        }

        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idReferencia);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public List<String[]> listar() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where s.estado = 'ACTIVO' order by s.id_suscripcion");
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(toRow(result));
            }
        }
        return rows;
    }

    public List<String[]> listarPorCliente(int idCliente) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where s.id_cliente = ? order by s.id_suscripcion")) {
            statement.setInt(1, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toRow(result));
                }
            }
        }
        return rows;
    }

    public String[] obtenerPorId(int idSuscripcion) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where s.id_suscripcion = ?")) {
            statement.setInt(1, idSuscripcion);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return toRow(result);
                }
            }
        }
        throw new SQLException("No existe suscripcion con id " + idSuscripcion);
    }

    public int agregar(int idCliente, String tipo, int idReferencia, Date fechaInicio, Date fechaFin,
            int usuarioCreacion) throws SQLException {
        String sql = "insert into suscripcion "
                + "(id_cliente, tipo, id_referencia, fecha_inicio, fecha_fin, estado, usuario_creacion) "
                + "values (?, ?, ?, ?, ?, 'ACTIVO', ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idCliente);
            statement.setString(2, tipo);
            statement.setInt(3, idReferencia);
            statement.setDate(4, fechaInicio);
            statement.setDate(5, fechaFin);
            statement.setInt(6, usuarioCreacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para la suscripcion");
        }
    }

    public void modificar(int idSuscripcion, int idCliente, String tipo, int idReferencia, Date fechaInicio,
            Date fechaFin, int usuarioModificacion) throws SQLException {
        String sql = "update suscripcion set id_cliente = ?, tipo = ?, id_referencia = ?, fecha_inicio = ?, "
                + "fecha_fin = ?, usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_suscripcion = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCliente);
            statement.setString(2, tipo);
            statement.setInt(3, idReferencia);
            statement.setDate(4, fechaInicio);
            statement.setDate(5, fechaFin);
            statement.setInt(6, usuarioModificacion);
            statement.setInt(7, idSuscripcion);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe suscripcion activa con id " + idSuscripcion);
            }
        }
    }

    public void eliminarLogico(int idSuscripcion, int usuarioModificacion) throws SQLException {
        String sql = "update suscripcion set estado = 'INACTIVO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_suscripcion = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idSuscripcion);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe suscripcion activa con id " + idSuscripcion);
            }
        }
    }

    public void vencer(int idSuscripcion, int usuarioModificacion) throws SQLException {
        String sql = "update suscripcion set estado = 'VENCIDO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_suscripcion = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idSuscripcion);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe suscripcion activa con id " + idSuscripcion);
            }
        }
    }

    public boolean estaActiva(int idSuscripcion) throws SQLException {
        String sql = "select 1 from suscripcion where id_suscripcion = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSuscripcion);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private String baseSelect() {
        return "select s.id_suscripcion, s.id_cliente, u.nombre as cliente, s.tipo, s.id_referencia, "
                + "coalesce(m.nombre, p.nombre, '') as referencia, s.fecha_inicio, s.fecha_fin, "
                + "s.fecha_registro, s.estado, s.usuario_creacion, s.usuario_modificacion, "
                + "s.fecha_modificacion "
                + "from suscripcion s "
                + "join usuario u on u.id_usuario = s.id_cliente "
                + "left join membresia m on s.tipo = 'MEMBRESIA' and m.id_membresia = s.id_referencia "
                + "left join paquete p on s.tipo = 'PAQUETE' and p.id_paquete = s.id_referencia";
    }

    private String[] toRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_suscripcion")),
            String.valueOf(result.getInt("id_cliente")),
            result.getString("cliente"),
            result.getString("tipo"),
            String.valueOf(result.getInt("id_referencia")),
            result.getString("referencia"),
            String.valueOf(result.getDate("fecha_inicio")),
            String.valueOf(result.getDate("fecha_fin")),
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
