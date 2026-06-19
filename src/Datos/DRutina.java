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

public class DRutina {

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

    public List<String[]> listar() throws SQLException {
        String sql = "select id_rutina, nombre, descripcion, objetivo, nivel, duracion_dias, "
                + "fecha_registro, estado, usuario_creacion, usuario_modificacion, fecha_modificacion "
                + "from rutina where estado = 'ACTIVO' order by id_rutina";
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(toRutinaRow(result));
            }
        }
        return rows;
    }

    public List<String[]> listarPorCliente(int idCliente) throws SQLException {
        String sql = "select rc.id_rutina_cliente, r.id_rutina, r.nombre, r.descripcion, r.objetivo, "
                + "r.nivel, r.duracion_dias, rc.fecha_inicio, rc.fecha_fin, rc.estado "
                + "from rutina_cliente rc "
                + "join rutina r on r.id_rutina = rc.id_rutina "
                + "where rc.id_cliente = ? "
                + "and rc.estado = 'ACTIVO' "
                + "and r.estado = 'ACTIVO' "
                + "order by rc.id_rutina_cliente";
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toAsignacionClienteRow(result));
                }
            }
        }
        return rows;
    }

    public String[] obtenerPorId(int idRutina) throws SQLException {
        String sql = "select id_rutina, nombre, descripcion, objetivo, nivel, duracion_dias, "
                + "fecha_registro, estado, usuario_creacion, usuario_modificacion, fecha_modificacion "
                + "from rutina where id_rutina = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutina);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return toRutinaRow(result);
                }
            }
        }
        throw new SQLException("No existe rutina con id " + idRutina);
    }

    public List<String[]> listarClientesAsignadosActivos(int idRutina) throws SQLException {
        String sql = "select rc.id_rutina_cliente, rc.id_rutina, rc.id_cliente, u.nombre as cliente, "
                + "u.email, rc.fecha_inicio, rc.fecha_fin, rc.estado, rc.usuario_creacion, "
                + "rc.usuario_modificacion, rc.fecha_modificacion "
                + "from rutina_cliente rc "
                + "join usuario u on u.id_usuario = rc.id_cliente "
                + "where rc.id_rutina = ? and rc.estado = 'ACTIVO' "
                + "order by rc.id_rutina_cliente";
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutina);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toAsignadoRow(result));
                }
            }
        }
        return rows;
    }

    public int agregar(String nombre, String descripcion, String objetivo, String nivel, int duracionDias,
            int idInstructor, int usuarioCreacion) throws SQLException {
        String sql = "insert into rutina "
                + "(id_instructor, nombre, descripcion, objetivo, nivel, duracion_dias, estado, usuario_creacion) "
                + "values (?, ?, ?, ?, ?, ?, 'ACTIVO', ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idInstructor);
            statement.setString(2, nombre);
            statement.setString(3, descripcion);
            statement.setString(4, objetivo);
            statement.setString(5, nivel);
            statement.setInt(6, duracionDias);
            statement.setInt(7, usuarioCreacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para la rutina");
        }
    }

    public void modificar(int idRutina, String nombre, String descripcion, String objetivo, String nivel,
            int duracionDias, int usuarioModificacion) throws SQLException {
        String sql = "update rutina set nombre = ?, descripcion = ?, objetivo = ?, nivel = ?, "
                + "duracion_dias = ?, usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_rutina = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setString(2, descripcion);
            statement.setString(3, objetivo);
            statement.setString(4, nivel);
            statement.setInt(5, duracionDias);
            statement.setInt(6, usuarioModificacion);
            statement.setInt(7, idRutina);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe rutina activa con id " + idRutina);
            }
        }
    }

    public void eliminarLogico(int idRutina, int usuarioModificacion) throws SQLException {
        String sql = "update rutina set estado = 'INACTIVO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_rutina = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idRutina);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe rutina activa con id " + idRutina);
            }
        }
    }

    public int asignar(int idRutina, int idCliente, Date fechaInicio, Date fechaFin, int usuarioCreacion)
            throws SQLException {
        String sql = "insert into rutina_cliente "
                + "(id_rutina, id_cliente, fecha_inicio, fecha_fin, estado, usuario_creacion) "
                + "values (?, ?, ?, ?, 'ACTIVO', ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idRutina);
            statement.setInt(2, idCliente);
            statement.setDate(3, fechaInicio);
            statement.setDate(4, fechaFin);
            statement.setInt(5, usuarioCreacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para la asignacion rutina-cliente");
        }
    }

    public boolean existeNombreActivo(String nombre) throws SQLException {
        String sql = "select 1 from rutina where lower(nombre) = lower(?) and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existeNombreActivoEnOtraRutina(String nombre, int idRutina) throws SQLException {
        String sql = "select 1 from rutina "
                + "where lower(nombre) = lower(?) and id_rutina <> ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            statement.setInt(2, idRutina);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean estaRutinaActiva(int idRutina) throws SQLException {
        String sql = "select 1 from rutina where id_rutina = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutina);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
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

    public boolean existeAsignacionActiva(int idRutina, int idCliente) throws SQLException {
        String sql = "select 1 from rutina_cliente "
                + "where id_rutina = ? and id_cliente = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutina);
            statement.setInt(2, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public boolean existeAsignacionActivaPorId(int idRutinaCliente) throws SQLException {
        String sql = "select 1 from rutina_cliente where id_rutina_cliente = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idRutinaCliente);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public void desasignar(int idRutinaCliente, int usuarioModificacion) throws SQLException {
        String sql = "update rutina_cliente set estado = 'INACTIVO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_rutina_cliente = ? and estado = 'ACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idRutinaCliente);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe asignacion activa con id " + idRutinaCliente);
            }
        }
    }

    private String[] toRutinaRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_rutina")),
            result.getString("nombre"),
            result.getString("descripcion"),
            result.getString("objetivo"),
            result.getString("nivel"),
            String.valueOf(result.getInt("duracion_dias")),
            String.valueOf(result.getDate("fecha_registro")),
            result.getString("estado"),
            getNullableInt(result, "usuario_creacion"),
            getNullableInt(result, "usuario_modificacion"),
            getNullableTimestamp(result, "fecha_modificacion")
        };
    }

    private String[] toAsignadoRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_rutina_cliente")),
            String.valueOf(result.getInt("id_rutina")),
            String.valueOf(result.getInt("id_cliente")),
            result.getString("cliente"),
            result.getString("email"),
            String.valueOf(result.getDate("fecha_inicio")),
            String.valueOf(result.getDate("fecha_fin")),
            result.getString("estado"),
            getNullableInt(result, "usuario_creacion"),
            getNullableInt(result, "usuario_modificacion"),
            getNullableTimestamp(result, "fecha_modificacion")
        };
    }

    private String[] toAsignacionClienteRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_rutina_cliente")),
            String.valueOf(result.getInt("id_rutina")),
            result.getString("nombre"),
            result.getString("descripcion"),
            result.getString("objetivo"),
            result.getString("nivel"),
            String.valueOf(result.getInt("duracion_dias")),
            String.valueOf(result.getDate("fecha_inicio")),
            String.valueOf(result.getDate("fecha_fin")),
            result.getString("estado")
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
