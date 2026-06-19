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

public class DPago {

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

    public static class PagoCuotaInfo {

        private final int idPago;
        private final String tipoPago;
        private final BigDecimal montoTotal;
        private final int cantidadCuotas;
        private final int cuotasPagadas;
        private final String estado;

        public PagoCuotaInfo(int idPago, String tipoPago, BigDecimal montoTotal, int cantidadCuotas,
                int cuotasPagadas, String estado) {
            this.idPago = idPago;
            this.tipoPago = tipoPago;
            this.montoTotal = montoTotal;
            this.cantidadCuotas = cantidadCuotas;
            this.cuotasPagadas = cuotasPagadas;
            this.estado = estado;
        }

        public int getIdPago() {
            return idPago;
        }

        public String getTipoPago() {
            return tipoPago;
        }

        public BigDecimal getMontoTotal() {
            return montoTotal;
        }

        public int getCantidadCuotas() {
            return cantidadCuotas;
        }

        public int getCuotasPagadas() {
            return cuotasPagadas;
        }

        public String getEstado() {
            return estado;
        }
    }

    public static class SuscripcionClienteInfo {

        private final int idSuscripcion;
        private final int idCliente;
        private final String cliente;
        private final String email;

        public SuscripcionClienteInfo(int idSuscripcion, int idCliente, String cliente, String email) {
            this.idSuscripcion = idSuscripcion;
            this.idCliente = idCliente;
            this.cliente = cliente;
            this.email = email;
        }

        public int getIdSuscripcion() {
            return idSuscripcion;
        }

        public int getIdCliente() {
            return idCliente;
        }

        public String getCliente() {
            return cliente;
        }

        public String getEmail() {
            return email;
        }
    }

    public static class PagoQrInfo {

        private final int idPago;
        private final String metodo;
        private final String estado;
        private final int cuotasPagadas;
        private final String paymentNumber;
        private final Long pagofacilTransactionId;
        private final String qrContenido;
        private final String qrImagenBase64;
        private final String callbackUrl;
        private final String estadoExterno;
        private final String respuestaExterna;

        public PagoQrInfo(int idPago, String metodo, String estado, int cuotasPagadas, String paymentNumber,
                Long pagofacilTransactionId, String qrContenido, String qrImagenBase64, String callbackUrl,
                String estadoExterno, String respuestaExterna) {
            this.idPago = idPago;
            this.metodo = metodo;
            this.estado = estado;
            this.cuotasPagadas = cuotasPagadas;
            this.paymentNumber = paymentNumber;
            this.pagofacilTransactionId = pagofacilTransactionId;
            this.qrContenido = qrContenido;
            this.qrImagenBase64 = qrImagenBase64;
            this.callbackUrl = callbackUrl;
            this.estadoExterno = estadoExterno;
            this.respuestaExterna = respuestaExterna;
        }

        public int getIdPago() {
            return idPago;
        }

        public String getMetodo() {
            return metodo;
        }

        public String getEstado() {
            return estado;
        }

        public int getCuotasPagadas() {
            return cuotasPagadas;
        }

        public String getPaymentNumber() {
            return paymentNumber;
        }

        public Long getPagofacilTransactionId() {
            return pagofacilTransactionId;
        }

        public String getQrContenido() {
            return qrContenido;
        }

        public String getQrImagenBase64() {
            return qrImagenBase64;
        }

        public String getCallbackUrl() {
            return callbackUrl;
        }

        public String getEstadoExterno() {
            return estadoExterno;
        }

        public String getRespuestaExterna() {
            return respuestaExterna;
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

    public boolean existeSuscripcionActivaConClienteActivo(int idSuscripcion) throws SQLException {
        String sql = "select 1 "
                + "from suscripcion s "
                + "join usuario u on u.id_usuario = s.id_cliente "
                + "join rol r on r.id_rol = u.id_rol "
                + "where s.id_suscripcion = ? "
                + "and s.estado = 'ACTIVO' "
                + "and u.estado = 'ACTIVO' "
                + "and r.nombre_rol = 'Cliente'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSuscripcion);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public SuscripcionClienteInfo obtenerSuscripcionClienteInfo(int idSuscripcion) throws SQLException {
        String sql = "select s.id_suscripcion, u.id_usuario as id_cliente, u.nombre as cliente, u.email "
                + "from suscripcion s "
                + "join usuario u on u.id_usuario = s.id_cliente "
                + "join rol r on r.id_rol = u.id_rol "
                + "where s.id_suscripcion = ? "
                + "and s.estado = 'ACTIVO' "
                + "and u.estado = 'ACTIVO' "
                + "and r.nombre_rol = 'Cliente'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idSuscripcion);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new SuscripcionClienteInfo(
                            result.getInt("id_suscripcion"),
                            result.getInt("id_cliente"),
                            result.getString("cliente"),
                            result.getString("email")
                    );
                }
            }
        }
        throw new SQLException("No existe suscripcion activa con cliente activo para id " + idSuscripcion);
    }

    public List<String[]> listar() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where p.estado <> 'INACTIVO' order by p.id_pago");
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
                        + " where s.id_cliente = ? and p.estado <> 'INACTIVO' order by p.id_pago")) {
            statement.setInt(1, idCliente);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toRow(result));
                }
            }
        }
        return rows;
    }

    public String[] obtenerPorId(int idPago) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(baseSelect()
                        + " where p.id_pago = ?")) {
            statement.setInt(1, idPago);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return toRow(result);
                }
            }
        }
        throw new SQLException("No existe pago con id " + idPago);
    }

    public List<String[]> listarCuotasPorPago(int idPago) throws SQLException {
        String sql = "select id_cuota, id_pago, numero_cuota, monto, fecha_pago, estado "
                + "from cuota where id_pago = ? order by numero_cuota";
        List<String[]> rows = new ArrayList<>();
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPago);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rows.add(toCuotaRow(result));
                }
            }
        }
        return rows;
    }

    public int agregarContado(int idSuscripcion, BigDecimal monto, Date fechaPago, String metodo,
            int usuarioCreacion) throws SQLException {
        String sql = "insert into pago "
                + "(id_suscripcion, tipo_pago, monto_total, cantidad_cuotas, cuotas_pagadas, metodo, "
                + "fecha_pago, estado, usuario_creacion) "
                + "values (?, 'CONTADO', ?, 1, 1, ?, ?, 'PAGADO', ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idSuscripcion);
            statement.setBigDecimal(2, monto);
            statement.setString(3, metodo);
            statement.setDate(4, fechaPago);
            statement.setInt(5, usuarioCreacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para el pago contado");
        }
    }

    public int agregarContadoConCuota(int idSuscripcion, BigDecimal monto, Date fechaPago, String metodo,
            int usuarioCreacion) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar()) {
            connection.setAutoCommit(false);
            try {
                int idPago;
                String pagoSql = "insert into pago "
                        + "(id_suscripcion, tipo_pago, monto_total, cantidad_cuotas, cuotas_pagadas, metodo, "
                        + "fecha_pago, estado, usuario_creacion) "
                        + "values (?, 'CONTADO', ?, 1, 1, ?, ?, 'PAGADO', ?)";
                try (PreparedStatement statement = connection.prepareStatement(pagoSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, idSuscripcion);
                    statement.setBigDecimal(2, monto);
                    statement.setString(3, metodo);
                    statement.setDate(4, fechaPago);
                    statement.setInt(5, usuarioCreacion);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se pudo generar el id del pago contado");
                        }
                        idPago = keys.getInt(1);
                    }
                }

                String cuotaSql = "insert into cuota (id_pago, numero_cuota, monto, fecha_pago, estado) "
                        + "values (?, 1, ?, ?, 'PAGADA')";
                try (PreparedStatement statement = connection.prepareStatement(cuotaSql)) {
                    statement.setInt(1, idPago);
                    statement.setBigDecimal(2, monto);
                    statement.setDate(3, fechaPago);
                    statement.executeUpdate();
                }
                connection.commit();
                return idPago;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public int agregarCredito(int idSuscripcion, BigDecimal montoTotal, int cantidadCuotas, Date fechaInicio,
            String metodo, int usuarioCreacion) throws SQLException {
        String sql = "insert into pago "
                + "(id_suscripcion, tipo_pago, monto_total, cantidad_cuotas, cuotas_pagadas, metodo, "
                + "fecha_pago, estado, usuario_creacion) "
                + "values (?, 'CREDITO', ?, ?, 0, ?, ?, 'PENDIENTE', ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idSuscripcion);
            statement.setBigDecimal(2, montoTotal);
            statement.setInt(3, cantidadCuotas);
            statement.setString(4, metodo);
            statement.setDate(5, fechaInicio);
            statement.setInt(6, usuarioCreacion);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para el pago credito");
        }
    }

    public int agregarCreditoConCuotas(int idSuscripcion, BigDecimal montoTotal, int cantidadCuotas, Date fechaInicio,
            String metodo, int usuarioCreacion) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar()) {
            connection.setAutoCommit(false);
            try {
                int idPago;
                String pagoSql = "insert into pago "
                        + "(id_suscripcion, tipo_pago, monto_total, cantidad_cuotas, cuotas_pagadas, metodo, "
                        + "fecha_pago, estado, usuario_creacion) "
                        + "values (?, 'CREDITO', ?, ?, 0, ?, ?, 'PENDIENTE', ?)";
                try (PreparedStatement statement = connection.prepareStatement(pagoSql, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, idSuscripcion);
                    statement.setBigDecimal(2, montoTotal);
                    statement.setInt(3, cantidadCuotas);
                    statement.setString(4, metodo);
                    statement.setDate(5, fechaInicio);
                    statement.setInt(6, usuarioCreacion);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No se pudo generar el id del pago credito");
                        }
                        idPago = keys.getInt(1);
                    }
                }

                BigDecimal cuotaBase = montoTotal.divide(BigDecimal.valueOf(cantidadCuotas), 2, java.math.RoundingMode.DOWN);
                BigDecimal acumulado = BigDecimal.ZERO;
                String cuotaSql = "insert into cuota (id_pago, numero_cuota, monto, estado) values (?, ?, ?, 'PENDIENTE')";
                try (PreparedStatement statement = connection.prepareStatement(cuotaSql)) {
                    for (int i = 1; i <= cantidadCuotas; i++) {
                        BigDecimal montoCuota = (i == cantidadCuotas)
                                ? montoTotal.subtract(acumulado)
                                : cuotaBase;
                        acumulado = acumulado.add(montoCuota);
                        statement.setInt(1, idPago);
                        statement.setInt(2, i);
                        statement.setBigDecimal(3, montoCuota);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
                return idPago;
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public int agregarQrPendiente(int idSuscripcion, BigDecimal montoTotal, Date fechaPago, int usuarioCreacion,
            String paymentNumber, Long pagofacilTransactionId, String qrContenido, String qrImagenBase64,
            String callbackUrl, String estadoExterno, String respuestaExterna) throws SQLException {
        String sql = "insert into pago "
                + "(id_suscripcion, tipo_pago, monto_total, cantidad_cuotas, cuotas_pagadas, metodo, "
                + "fecha_pago, estado, usuario_creacion, payment_number, pagofacil_transaction_id, qr_contenido, "
                + "qr_imagen_base64, callback_url, estado_externo, respuesta_externa) "
                + "values (?, 'CONTADO', ?, 1, 0, 'QR', ?, 'PENDIENTE', ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idSuscripcion);
            statement.setBigDecimal(2, montoTotal);
            statement.setDate(3, fechaPago);
            statement.setInt(4, usuarioCreacion);
            statement.setString(5, paymentNumber);
            setNullableLong(statement, 6, pagofacilTransactionId);
            statement.setString(7, qrContenido);
            statement.setString(8, qrImagenBase64);
            statement.setString(9, callbackUrl);
            statement.setString(10, estadoExterno);
            statement.setString(11, respuestaExterna);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new SQLException("No se pudo obtener el id generado para el pago QR");
        }
    }

    public PagoCuotaInfo obtenerPagoParaCuota(int idPago) throws SQLException {
        String sql = "select id_pago, tipo_pago, monto_total, cantidad_cuotas, cuotas_pagadas, estado "
                + "from pago where id_pago = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPago);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new PagoCuotaInfo(
                            result.getInt("id_pago"),
                            result.getString("tipo_pago"),
                            result.getBigDecimal("monto_total"),
                            result.getInt("cantidad_cuotas"),
                            result.getInt("cuotas_pagadas"),
                            result.getString("estado")
                    );
                }
            }
        }
        throw new SQLException("No existe pago con id " + idPago);
    }

    public boolean existeCuota(int idPago, int numeroCuota) throws SQLException {
        String sql = "select 1 from cuota where id_pago = ? and numero_cuota = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPago);
            statement.setInt(2, numeroCuota);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public BigDecimal sumarCuotasPagadas(int idPago) throws SQLException {
        String sql = "select coalesce(sum(monto), 0) as total from cuota where id_pago = ? and estado = 'PAGADA'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPago);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBigDecimal("total");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public void registrarCuota(int idPago, int numeroCuota, BigDecimal monto, Date fechaPago,
            int usuarioModificacion) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar()) {
            connection.setAutoCommit(false);
            try {
                String insertSql = "insert into cuota (id_pago, numero_cuota, monto, fecha_pago, estado) "
                        + "values (?, ?, ?, ?, 'PAGADA')";
                try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
                    statement.setInt(1, idPago);
                    statement.setInt(2, numeroCuota);
                    statement.setBigDecimal(3, monto);
                    statement.setDate(4, fechaPago);
                    statement.executeUpdate();
                }

                String updateSql = "update pago set cuotas_pagadas = cuotas_pagadas + 1, "
                        + "estado = case "
                        + "when cuotas_pagadas + 1 >= cantidad_cuotas then 'PAGADO' "
                        + "else 'PARCIAL' end, "
                        + "usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                        + "where id_pago = ? and estado <> 'INACTIVO'";
                try (PreparedStatement statement = connection.prepareStatement(updateSql)) {
                    statement.setInt(1, usuarioModificacion);
                    statement.setInt(2, idPago);
                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("No existe pago activo con id " + idPago);
                    }
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void pagarCuota(int idCuota, int usuarioModificacion) throws SQLException {
        try (Connection connection = DBConnection.getInstance().conectar()) {
            connection.setAutoCommit(false);
            try {
                int idPago;
                String obtenerSql = "select id_pago from cuota where id_cuota = ? and estado = 'PENDIENTE'";
                try (PreparedStatement statement = connection.prepareStatement(obtenerSql)) {
                    statement.setInt(1, idCuota);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("No existe cuota pendiente con id " + idCuota);
                        }
                        idPago = result.getInt("id_pago");
                    }
                }

                String cuotaSql = "update cuota set estado = 'PAGADA', fecha_pago = CURRENT_DATE where id_cuota = ?";
                try (PreparedStatement statement = connection.prepareStatement(cuotaSql)) {
                    statement.setInt(1, idCuota);
                    statement.executeUpdate();
                }

                String pagoSql = "update pago set cuotas_pagadas = cuotas_pagadas + 1, "
                        + "estado = case when cuotas_pagadas + 1 >= cantidad_cuotas then 'PAGADO' else 'PARCIAL' end, "
                        + "usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                        + "where id_pago = ? and estado <> 'INACTIVO'";
                try (PreparedStatement statement = connection.prepareStatement(pagoSql)) {
                    statement.setInt(1, usuarioModificacion);
                    statement.setInt(2, idPago);
                    if (statement.executeUpdate() == 0) {
                        throw new SQLException("No existe pago activo asociado a la cuota " + idCuota);
                    }
                }
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void modificar(int idPago, BigDecimal montoTotal, String metodo, int usuarioModificacion)
            throws SQLException {
        String sql = "update pago set monto_total = ?, metodo = ?, usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_pago = ? and estado <> 'INACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, montoTotal);
            statement.setString(2, metodo);
            statement.setInt(3, usuarioModificacion);
            statement.setInt(4, idPago);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe pago activo con id " + idPago);
            }
        }
    }

    public void eliminarLogico(int idPago, int usuarioModificacion) throws SQLException {
        String sql = "update pago set estado = 'INACTIVO', usuario_modificacion = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_pago = ? and estado <> 'INACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, usuarioModificacion);
            statement.setInt(2, idPago);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe pago activo con id " + idPago);
            }
        }
    }

    public boolean estaPagoActivo(int idPago) throws SQLException {
        String sql = "select 1 from pago where id_pago = ? and estado <> 'INACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPago);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public PagoQrInfo obtenerPagoQrInfo(int idPago) throws SQLException {
        String sql = "select id_pago, metodo, estado, cuotas_pagadas, payment_number, pagofacil_transaction_id, "
                + "qr_contenido, qr_imagen_base64, callback_url, estado_externo, respuesta_externa "
                + "from pago where id_pago = ?";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPago);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    long rawTransactionId = result.getLong("pagofacil_transaction_id");
                    Long transactionId = result.wasNull() ? null : rawTransactionId;
                    return new PagoQrInfo(
                            result.getInt("id_pago"),
                            result.getString("metodo"),
                            result.getString("estado"),
                            result.getInt("cuotas_pagadas"),
                            result.getString("payment_number"),
                            transactionId,
                            result.getString("qr_contenido"),
                            result.getString("qr_imagen_base64"),
                            result.getString("callback_url"),
                            result.getString("estado_externo"),
                            result.getString("respuesta_externa")
                    );
                }
            }
        }
        throw new SQLException("No existe pago con id " + idPago);
    }

    public void actualizarEstadoQr(int idPago, String estadoLocal, int cuotasPagadas, String estadoExterno,
            String respuestaExterna, int usuarioModificacion) throws SQLException {
        String sql = "update pago set estado = ?, cuotas_pagadas = ?, estado_externo = ?, respuesta_externa = ?, "
                + "usuario_modificacion = ?, fecha_modificacion = CURRENT_TIMESTAMP "
                + "where id_pago = ? and estado <> 'INACTIVO'";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estadoLocal);
            statement.setInt(2, cuotasPagadas);
            statement.setString(3, estadoExterno);
            statement.setString(4, respuestaExterna);
            statement.setInt(5, usuarioModificacion);
            statement.setInt(6, idPago);
            if (statement.executeUpdate() == 0) {
                throw new SQLException("No existe pago activo con id " + idPago);
            }
        }
    }

    public int actualizarEstadoQrPorReferenciaExterna(String paymentNumber, Long pagofacilTransactionId,
            String estadoLocal, int cuotasPagadas, String estadoExterno, String respuestaExterna)
            throws SQLException {
        String sql = "update pago set estado = ?, cuotas_pagadas = ?, estado_externo = ?, respuesta_externa = ?, "
                + "fecha_modificacion = CURRENT_TIMESTAMP "
                + "where estado <> 'INACTIVO' and (payment_number = ? or pagofacil_transaction_id = ?)";
        try (Connection connection = DBConnection.getInstance().conectar();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estadoLocal);
            statement.setInt(2, cuotasPagadas);
            statement.setString(3, estadoExterno);
            statement.setString(4, respuestaExterna);
            statement.setString(5, paymentNumber);
            setNullableLong(statement, 6, pagofacilTransactionId);
            return statement.executeUpdate();
        }
    }

    private String baseSelect() {
        return "select p.id_pago, p.id_suscripcion, s.id_cliente, u.nombre as cliente, "
                + "p.tipo_pago, p.monto_total, p.cantidad_cuotas, p.cuotas_pagadas, p.metodo, "
                + "p.fecha_pago, p.fecha_registro, p.estado, p.usuario_creacion, "
                + "p.usuario_modificacion, p.fecha_modificacion, p.payment_number, "
                + "p.pagofacil_transaction_id, p.estado_externo "
                + "from pago p "
                + "join suscripcion s on s.id_suscripcion = p.id_suscripcion "
                + "join usuario u on u.id_usuario = s.id_cliente";
    }

    private String[] toRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_pago")),
            String.valueOf(result.getInt("id_suscripcion")),
            String.valueOf(result.getInt("id_cliente")),
            result.getString("cliente"),
            result.getString("tipo_pago"),
            result.getBigDecimal("monto_total").toPlainString(),
            String.valueOf(result.getInt("cantidad_cuotas")),
            String.valueOf(result.getInt("cuotas_pagadas")),
            result.getString("metodo"),
            getNullableDate(result, "fecha_pago"),
            String.valueOf(result.getDate("fecha_registro")),
            result.getString("estado"),
            getNullableInt(result, "usuario_creacion"),
            getNullableInt(result, "usuario_modificacion"),
            getNullableTimestamp(result, "fecha_modificacion"),
            getNullableString(result, "payment_number"),
            getNullableLong(result, "pagofacil_transaction_id"),
            getNullableString(result, "estado_externo")
        };
    }

    private String[] toCuotaRow(ResultSet result) throws SQLException {
        return new String[]{
            String.valueOf(result.getInt("id_cuota")),
            String.valueOf(result.getInt("id_pago")),
            String.valueOf(result.getInt("numero_cuota")),
            result.getBigDecimal("monto").toPlainString(),
            getNullableDate(result, "fecha_pago"),
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

    private String getNullableDate(ResultSet result, String column) throws SQLException {
        Date value = result.getDate(column);
        if (value == null) {
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

    private String getNullableString(ResultSet result, String column) throws SQLException {
        String value = result.getString(column);
        if (value == null) {
            return "";
        }
        return value;
    }

    private String getNullableLong(ResultSet result, String column) throws SQLException {
        long value = result.getLong(column);
        if (result.wasNull()) {
            return "";
        }
        return String.valueOf(value);
    }

    private void setNullableLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, java.sql.Types.BIGINT);
            return;
        }
        statement.setLong(index, value);
    }
}
