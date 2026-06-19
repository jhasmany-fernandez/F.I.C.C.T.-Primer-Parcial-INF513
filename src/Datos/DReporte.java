package Datos;

import Conexion.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DReporte {

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

    public List<String[]> reporteUsuarios() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Total usuarios", count("select count(*) from usuario"));
        addMetric(rows, "Usuarios activos", count("select count(*) from usuario where estado = 'ACTIVO'"));
        addMetric(rows, "Usuarios inactivos", count("select count(*) from usuario where estado = 'INACTIVO'"));
        rows.addAll(groupRows("Usuarios por rol",
                "select r.nombre_rol, count(*) "
                + "from usuario u join rol r on r.id_rol = u.id_rol "
                + "group by r.nombre_rol order by r.nombre_rol"));
        return rows;
    }

    public List<String[]> reporteMembresias() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Total membresias", count("select count(*) from membresia"));
        addMetric(rows, "Membresias activas", count("select count(*) from membresia where estado = 'ACTIVO'"));
        addMetric(rows, "Membresias inactivas", count("select count(*) from membresia where estado = 'INACTIVO'"));
        return rows;
    }

    public List<String[]> reportePaquetes() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Total paquetes", count("select count(*) from paquete"));
        addMetric(rows, "Paquetes activos", count("select count(*) from paquete where estado = 'ACTIVO'"));
        addMetric(rows, "Paquetes inactivos", count("select count(*) from paquete where estado = 'INACTIVO'"));
        return rows;
    }

    public List<String[]> reporteSuscripciones() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Total suscripciones", count("select count(*) from suscripcion"));
        addMetric(rows, "Suscripciones activas", count("select count(*) from suscripcion where estado = 'ACTIVO'"));
        addMetric(rows, "Suscripciones vencidas", count("select count(*) from suscripcion where estado = 'VENCIDO'"));
        addMetric(rows, "Suscripciones inactivas", count("select count(*) from suscripcion where estado = 'INACTIVO'"));
        rows.addAll(groupRows("Suscripciones por tipo",
                "select tipo, count(*) from suscripcion group by tipo order by tipo"));
        return rows;
    }

    public List<String[]> reportePagos() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Total pagos", count("select count(*) from pago"));
        addMetric(rows, "Pagos pagados", count("select count(*) from pago where estado = 'PAGADO'"));
        addMetric(rows, "Pagos pendientes", count("select count(*) from pago where estado = 'PENDIENTE'"));
        addMetric(rows, "Pagos parciales", count("select count(*) from pago where estado = 'PARCIAL'"));
        addMetric(rows, "Pagos inactivos", count("select count(*) from pago where estado = 'INACTIVO'"));
        addMetric(rows, "Monto total pagado", money(sum(
                "select coalesce(sum(monto_total), 0) from pago where estado = 'PAGADO'")));
        addMetric(rows, "Monto pendiente real", money(sum(
                "select coalesce(sum(case "
                + "when p.estado = 'PENDIENTE' then p.monto_total "
                + "when p.estado = 'PARCIAL' then greatest(p.monto_total - coalesce(c.total_cuotas, 0), 0) "
                + "else 0 end), 0) "
                + "from pago p "
                + "left join ("
                + "select id_pago, sum(monto) as total_cuotas "
                + "from cuota where estado = 'PAGADA' group by id_pago"
                + ") c on c.id_pago = p.id_pago "
                + "where p.estado in ('PENDIENTE', 'PARCIAL')")));
        rows.addAll(groupRows("Pagos por metodo",
                "select metodo, count(*) from pago group by metodo order by metodo"));
        return rows;
    }

    public List<String[]> reporteRutinas() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Total rutinas", count("select count(*) from rutina"));
        addMetric(rows, "Rutinas activas", count("select count(*) from rutina where estado = 'ACTIVO'"));
        addMetric(rows, "Rutinas inactivas", count("select count(*) from rutina where estado = 'INACTIVO'"));
        addMetric(rows, "Asignaciones activas", count("select count(*) from rutina_cliente where estado = 'ACTIVO'"));
        return rows;
    }

    public List<String[]> reporteSeguimientos() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Total seguimientos", count("select count(*) from seguimiento"));
        addMetric(rows, "Seguimientos activos",
                count("select count(*) from seguimiento where estado_logico = 'ACTIVO'"));
        addMetric(rows, "Seguimientos inactivos",
                count("select count(*) from seguimiento where estado_logico = 'INACTIVO'"));
        rows.addAll(groupRows("Seguimientos por estado",
                "select estado, count(*) from seguimiento "
                + "where estado_logico = 'ACTIVO' group by estado order by estado"));
        return rows;
    }

    public List<String[]> reporteEstadisticas() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        addMetric(rows, "Clientes activos", count(
                "select count(*) "
                + "from usuario u join rol r on r.id_rol = u.id_rol "
                + "where r.nombre_rol = 'Cliente' and u.estado = 'ACTIVO'"));
        addMetric(rows, "Suscripciones activas",
                count("select count(*) from suscripcion where estado = 'ACTIVO'"));
        addMetric(rows, "Ingresos registrados", money(sum(
                "select coalesce(sum(monto_total), 0) from pago where estado = 'PAGADO'")));
        addMetric(rows, "Pagos pendientes",
                count("select count(*) from pago where estado in ('PENDIENTE', 'PARCIAL')"));
        addMetric(rows, "Rutinas asignadas activas",
                count("select count(*) from rutina_cliente where estado = 'ACTIVO'"));
        addMetric(rows, "Seguimientos activos",
                count("select count(*) from seguimiento where estado_logico = 'ACTIVO'"));
        return rows;
    }

    public List<String[]> reporteGeneral() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        rows.addAll(prefixRows("Usuarios", reporteUsuarios()));
        rows.addAll(prefixRows("Membresias", reporteMembresias()));
        rows.addAll(prefixRows("Paquetes", reportePaquetes()));
        rows.addAll(prefixRows("Suscripciones", reporteSuscripciones()));
        rows.addAll(prefixRows("Pagos", reportePagos()));
        rows.addAll(prefixRows("Rutinas", reporteRutinas()));
        rows.addAll(prefixRows("Seguimientos", reporteSeguimientos()));
        rows.addAll(prefixRows("Estadisticas", reporteEstadisticas()));
        return rows;
    }

    public List<String[]> withMetadata(List<String[]> rows, String correo, String rol) {
        List<String[]> result = new ArrayList<>();
        result.add(new String[]{"Fecha/hora generacion", String.valueOf(LocalDateTime.now())});
        result.add(new String[]{"Rol solicitante", rol});
        result.add(new String[]{"Correo solicitante", correo});
        result.addAll(rows);
        return result;
    }

    private long count(String sql) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getLong(1);
        }
    }

    private BigDecimal sum(String sql) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getBigDecimal(1);
        }
    }

    private List<String[]> groupRows(String title, String sql) throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new String[]{title + " - " + result.getString(1), String.valueOf(result.getLong(2))});
            }
        }
        return rows;
    }

    private List<String[]> prefixRows(String prefix, List<String[]> rows) {
        List<String[]> result = new ArrayList<>();
        for (String[] row : rows) {
            result.add(new String[]{prefix + " - " + row[0], row[1]});
        }
        return result;
    }

    private void addMetric(List<String[]> rows, String metric, long value) {
        rows.add(new String[]{metric, String.valueOf(value)});
    }

    private void addMetric(List<String[]> rows, String metric, String value) {
        rows.add(new String[]{metric, value});
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }
        return value.toPlainString();
    }
}
