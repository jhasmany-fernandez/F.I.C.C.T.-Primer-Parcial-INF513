package Datos;

import Conexion.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DSeguimiento {

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

    public List<String[]> listarActivos() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where s.estado_logico = 'ACTIVO' order by s.id_seguimiento");
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(toRow(result));
            }
        }
        return rows;
    }

    public List<String[]> listarPorRutinaCliente(int idRutinaCliente) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where s.id_rutina_cliente = ? and s.estado_logico = 'ACTIVO' order by s.fecha_seguimiento")) {
            statement.setInt(1, idRutinaCliente);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toRow(result));
                }
            }
        }
        return rows;
    }

    public List<String[]> listarPorCliente(int idCliente) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where rc.id_cliente = ? and s.estado_logico = 'ACTIVO' "
                        + "order by s.id_seguimiento")) {
            statement.setInt(1, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toRow(result));
                }
            }
        }
        return rows;
    }

    public String[] obtenerPorId(int idSeguimiento) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where s.id_seguimiento = ?")) {
            statement.setInt(1, idSeguimiento);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return toRow(result);
                }
            }
        }
        throw new SQLException("No existe seguimiento con id " + idSeguimiento);
    }

    public boolean existeRutinaClienteActiva(int idRutinaCliente) throws SQLException {
        String sql = "select 1 from rutina_cliente rc "
                + "join rutina r on r.id_rutina = rc.id_rutina "
                + "join usuario u on u.id_usuario = rc.id_cliente "
                + "join rol rol on rol.id_rol = u.id_rol "
                + "where rc.id_rutina_cliente = ? "
                + "and rc.estado = 'ACTIVO' "
                + "and r.estado = 'ACTIVO' "
                + "and u.estado = 'ACTIVO' "
                + "and rol.nombre_rol = 'Cliente'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutinaCliente);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean rutinaClientePerteneceACliente(int idRutinaCliente, int idCliente) throws SQLException {
        String sql = "select 1 from rutina_cliente "
                + "where id_rutina_cliente = ? and id_cliente = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutinaCliente);
            statement.setInt(2, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean seguimientoPerteneceACliente(int idSeguimiento, int idCliente) throws SQLException {
        String sql = "select 1 from seguimiento s "
                + "join rutina_cliente rc on rc.id_rutina_cliente = s.id_rutina_cliente "
                + "where s.id_seguimiento = ? and rc.id_cliente = ? and s.estado_logico = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSeguimiento);
            statement.setInt(2, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existeSeguimientoActivoDuplicado(int idRutinaCliente, Date fechaSeguimiento) throws SQLException {
        String sql = "select 1 from seguimiento "
                + "where id_rutina_cliente = ? and fecha_seguimiento = ? and estado_logico = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutinaCliente);
            statement.setDate(2, fechaSeguimiento);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public int registrar(int idRutinaCliente, Date fechaSeguimiento, BigDecimal peso, String medidas, String observacion,
            String estado, int usuarioCreacion) throws SQLException {
        String sql = "insert into seguimiento "
                + "(id_rutina_cliente, fecha_seguimiento, peso, medidas, observacion, estado, estado_logico, "
                + "usuario_creacion) values (?, ?, ?, ?, ?, ?, 'ACTIVO', ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idRutinaCliente);
            statement.setDate(2, fechaSeguimiento);
            statement.setBigDecimal(3, peso);
            statement.setString(4, medidas);
            statement.setString(5, observacion);
            statement.setString(6, estado);
            statement.setInt(7, usuarioCreacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para el seguimiento");
        }
    }

    public void modificar(int idSeguimiento, BigDecimal peso, String medidas, String observacion, String estado,
            int usuarioModificacion) throws SQLException {
        String sql = "update seguimiento set peso = ?, medidas = ?, observacion = ?, estado = ?, "
                + "usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_seguimiento = ? and estado_logico = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, peso);
            statement.setString(2, medidas);
            statement.setString(3, observacion);
            statement.setString(4, estado);
            statement.setInt(5, usuarioModificacion);
            statement.setInt(6, idSeguimiento);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe seguimiento activo con id " + idSeguimiento);
            }
        }
    }

    public void completar(int idSeguimiento, int usuarioModificacion) throws SQLException {
        String sql = "update seguimiento set estado = 'COMPLETADO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_seguimiento = ? and estado_logico = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idSeguimiento);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe seguimiento activo con id " + idSeguimiento);
            }
        }
    }

    public void eliminarLogico(int idSeguimiento, int usuarioModificacion) throws SQLException {
        String sql = "update seguimiento set estado_logico = 'INACTIVO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_seguimiento = ? and estado_logico = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idSeguimiento);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe seguimiento activo con id " + idSeguimiento);
            }
        }
    }

    public boolean estaSeguimientoActivo(int idSeguimiento) throws SQLException {
        String sql = "select 1 from seguimiento where id_seguimiento = ? and estado_logico = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSeguimiento);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private String baseSelect() {
        return "select s.id_seguimiento, s.id_rutina_cliente, rc.id_rutina, r.nombre as rutina, "
                + "rc.id_cliente, u.nombre as cliente, u.email as cliente_email, s.fecha_seguimiento, "
                + "s.peso, s.medidas, s.observacion, s.estado, s.estado_logico, s.usuario_creacion, "
                + "s.usuario_modificacion, s.fecha_modificacion "
                + "from seguimiento s "
                + "join rutina_cliente rc on rc.id_rutina_cliente = s.id_rutina_cliente "
                + "join rutina r on r.id_rutina = rc.id_rutina "
                + "join usuario u on u.id_usuario = rc.id_cliente";
    }

    private String[] toRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_seguimiento")),
            String.valueOf(result.getInt("id_rutina_cliente")),
            String.valueOf(result.getInt("id_rutina")),
            result.getString("rutina"),
            String.valueOf(result.getInt("id_cliente")),
            result.getString("cliente"),
            result.getString("cliente_email"),
            String.valueOf(result.getDate("fecha_seguimiento")),
            result.getBigDecimal("peso").toPlainString(),
            result.getString("medidas"),
            result.getString("observacion"),
            result.getString("estado"),
            result.getString("estado_logico"),
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
