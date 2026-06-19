package Negocio;

import Datos.DPago;
import Datos.DPago.PagoCuotaInfo;
import Datos.DPago.PagoQrInfo;
import Datos.DPago.SuscripcionClienteInfo;
import Datos.DPago.UsuarioAcceso;
import Integracion.PagoFacilClient;
import Integracion.PagoFacilClient.QrRequest;
import Integracion.PagoFacilClient.QrResponse;
import Integracion.PagoFacilClient.TransactionStatusResponse;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NPago {

    public static final String ACCESS_DENIED_GESTION
            = "Acceso denegado. Solo el Propietario o la Secretaria pueden gestionar pagos.";
    public static final String ACCESS_DENIED_CLIENTE
            = "Acceso denegado. El Cliente solo puede consultar sus propios pagos.";

    private final DPago dPago;
    private final PagoFacilClient pagoFacilClient;

    public NPago() {
        dPago = new DPago();
        pagoFacilClient = new PagoFacilClient();
    }

    public static class GenerarQrResultado {

        private final int idPago;
        private final String paymentNumber;
        private final Long pagofacilTransactionId;
        private final String qrContenido;
        private final String qrImagenBase64;
        private final String callbackUrl;

        public GenerarQrResultado(int idPago, String paymentNumber, Long pagofacilTransactionId,
                String qrContenido, String qrImagenBase64, String callbackUrl) {
            this.idPago = idPago;
            this.paymentNumber = paymentNumber;
            this.pagofacilTransactionId = pagofacilTransactionId;
            this.qrContenido = qrContenido;
            this.qrImagenBase64 = qrImagenBase64;
            this.callbackUrl = callbackUrl;
        }

        public int getIdPago() {
            return idPago;
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
    }

    public static class ConsultaQrResultado {

        private final int idPago;
        private final String estadoLocal;
        private final String estadoExterno;
        private final String mensaje;

        public ConsultaQrResultado(int idPago, String estadoLocal, String estadoExterno, String mensaje) {
            this.idPago = idPago;
            this.estadoLocal = estadoLocal;
            this.estadoExterno = estadoExterno;
            this.mensaje = mensaje;
        }

        public int getIdPago() {
            return idPago;
        }

        public String getEstadoLocal() {
            return estadoLocal;
        }

        public String getEstadoExterno() {
            return estadoExterno;
        }

        public String getMensaje() {
            return mensaje;
        }
    }

    public void validarAccesoGestion(String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
    }

    public List<String[]> listar(String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        return dPago.listar();
    }

    public List<String[]> misPagos(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioCliente(correoRemitente);
        return dPago.listarPorCliente(usuario.getIdUsuario());
    }

    public String[] obtenerPorId(List<String> parametros, String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "pago ver [id_pago]");
        return dPago.obtenerPorId(parseId(parametros.get(0), "id_pago"));
    }

    public List<String[]> listarCuotasPorPago(List<String> parametros, String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "pago ver [id_pago]");
        return dPago.listarCuotasPorPago(parseId(parametros.get(0), "id_pago"));
    }

    public int registrarContado(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 4, "pago registrar_contado [id_suscripcion; monto; fecha_pago; metodo]");

        int idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
        BigDecimal monto = parseMonto(parametros.get(1), "monto");
        Date fechaPago = parseFecha(parametros.get(2), "fecha_pago");
        String metodo = parseMetodo(parametros.get(3));

        validarSuscripcionActiva(idSuscripcion);
        return dPago.agregarContado(idSuscripcion, monto, fechaPago, metodo, usuario.getIdUsuario());
    }

    public int agregar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 6, "pago agregar [id_suscripcion; tipo_pago; monto_total; cantidad_cuotas; metodo; fecha_pago]");

        int idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
        String tipoPago = parseTipoPago(parametros.get(1));
        BigDecimal montoTotal = parseMonto(parametros.get(2), "monto_total");
        int cantidadCuotas = parseCantidadCuotasFlexible(parametros.get(3), tipoPago);
        String metodo = parseMetodo(parametros.get(4));
        Date fechaPago = parseFecha(parametros.get(5), "fecha_pago");

        validarSuscripcionActiva(idSuscripcion);
        if ("CONTADO".equals(tipoPago)) {
            return dPago.agregarContadoConCuota(idSuscripcion, montoTotal, fechaPago, metodo, usuario.getIdUsuario());
        }
        return dPago.agregarCreditoConCuotas(idSuscripcion, montoTotal, cantidadCuotas, fechaPago, metodo,
                usuario.getIdUsuario());
    }

    public int registrarCredito(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 5,
                "pago registrar_credito [id_suscripcion; monto_total; cantidad_cuotas; fecha_inicio; metodo]");

        int idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
        BigDecimal montoTotal = parseMonto(parametros.get(1), "monto_total");
        int cantidadCuotas = parseCantidadCuotas(parametros.get(2));
        Date fechaInicio = parseFecha(parametros.get(3), "fecha_inicio");
        String metodo = parseMetodo(parametros.get(4));

        validarSuscripcionActiva(idSuscripcion);
        return dPago.agregarCredito(idSuscripcion, montoTotal, cantidadCuotas, fechaInicio, metodo,
                usuario.getIdUsuario());
    }

    public void registrarCuota(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 4, "pago registrar_cuota [id_pago; numero_cuota; monto; fecha_pago]");

        int idPago = parseId(parametros.get(0), "id_pago");
        int numeroCuota = parseNumeroCuota(parametros.get(1));
        BigDecimal monto = parseMonto(parametros.get(2), "monto");
        Date fechaPago = parseFecha(parametros.get(3), "fecha_pago");

        PagoCuotaInfo pago = dPago.obtenerPagoParaCuota(idPago);
        validarPagoParaCuota(pago, numeroCuota, monto);
        if (dPago.existeCuota(idPago, numeroCuota)) {
            throw new IllegalArgumentException("Ya existe una cuota registrada para ese pago y numero_cuota");
        }
        BigDecimal totalConNuevaCuota = dPago.sumarCuotasPagadas(idPago).add(monto);
        if (totalConNuevaCuota.compareTo(pago.getMontoTotal()) > 0) {
            throw new IllegalArgumentException("La suma de cuotas pagadas no puede superar monto_total");
        }

        dPago.registrarCuota(idPago, numeroCuota, monto, fechaPago, usuario.getIdUsuario());
    }

    public void pagarCuota(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "pago pagar_cuota [id_cuota]");
        int idCuota = parseId(parametros.get(0), "id_cuota");
        dPago.pagarCuota(idCuota, usuario.getIdUsuario());
    }

    public void modificar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 3, "pago modificar [id_pago; monto; metodo]");

        int idPago = parseId(parametros.get(0), "id_pago");
        BigDecimal monto = parseMonto(parametros.get(1), "monto");
        String metodo = parseMetodo(parametros.get(2));

        validarPagoActivo(idPago);
        BigDecimal sumaCuotas = dPago.sumarCuotasPagadas(idPago);
        if (sumaCuotas.compareTo(monto) > 0) {
            throw new IllegalArgumentException("monto no puede ser menor que la suma de cuotas pagadas");
        }
        dPago.modificar(idPago, monto, metodo, usuario.getIdUsuario());
    }

    public void eliminar(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "pago eliminar [id_pago]");
        int idPago = parseId(parametros.get(0), "id_pago");
        validarPagoActivo(idPago);
        dPago.eliminarLogico(idPago, usuario.getIdUsuario());
    }

    public GenerarQrResultado generarQr(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 6,
                "pago generar_qr [id_suscripcion; monto; fecha_pago; documento_identidad; telefono; concepto]");

        int idSuscripcion = parseId(parametros.get(0), "id_suscripcion");
        BigDecimal monto = parseMonto(parametros.get(1), "monto");
        Date fechaPago = parseFecha(parametros.get(2), "fecha_pago");
        String documentoIdentidad = parseTexto(parametros.get(3), "documento_identidad");
        String telefono = parseTexto(parametros.get(4), "telefono");
        String concepto = parseTexto(parametros.get(5), "concepto");

        validarSuscripcionActiva(idSuscripcion);
        SuscripcionClienteInfo cliente = dPago.obtenerSuscripcionClienteInfo(idSuscripcion);
        String paymentNumber = UUID.randomUUID().toString();
        QrResponse qrResponse = generarQrExterno(cliente, paymentNumber, monto, documentoIdentidad, telefono, concepto);

        int idPago = dPago.agregarQrPendiente(
                idSuscripcion,
                monto,
                fechaPago,
                usuario.getIdUsuario(),
                paymentNumber,
                qrResponse.getPagofacilTransactionId(),
                qrResponse.getQrContent(),
                qrResponse.getQrImageBase64(),
                pagoFacilClient.getConfiguredCallbackUrl(),
                qrResponse.getStatus(),
                qrResponse.getRawResponse()
        );
        return new GenerarQrResultado(
                idPago,
                paymentNumber,
                qrResponse.getPagofacilTransactionId(),
                qrResponse.getQrContent(),
                qrResponse.getQrImageBase64(),
                pagoFacilClient.getConfiguredCallbackUrl()
        );
    }

    public ConsultaQrResultado consultarQr(List<String> parametros, String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "pago consultar_qr [id_pago]");
        int idPago = parseId(parametros.get(0), "id_pago");
        validarPagoActivo(idPago);

        PagoQrInfo pagoQrInfo = dPago.obtenerPagoQrInfo(idPago);
        validarPagoQr(pagoQrInfo, idPago);

        TransactionStatusResponse statusResponse = consultarTransaccionExterna(pagoQrInfo.getPagofacilTransactionId());
        String estadoExterno = normalizeStatus(statusResponse.getStatus());
        String estadoLocal = pagoQrInfo.getEstado();
        int cuotasPagadas = pagoQrInfo.getCuotasPagadas();
        if (isPaidStatus(estadoExterno)) {
            estadoLocal = "PAGADO";
            cuotasPagadas = 1;
        }

        dPago.actualizarEstadoQr(
                idPago,
                estadoLocal,
                cuotasPagadas,
                estadoExterno,
                statusResponse.getRawResponse(),
                usuario.getIdUsuario()
        );
        return new ConsultaQrResultado(idPago, estadoLocal, estadoExterno, statusResponse.getMessage());
    }

    public PagoQrInfo obtenerQrInfo(List<String> parametros, String correoRemitente) throws SQLException {
        obtenerUsuarioGestion(correoRemitente);
        requireSize(parametros, 1, "pago ver [id_pago]");
        return dPago.obtenerPagoQrInfo(parseId(parametros.get(0), "id_pago"));
    }

    private UsuarioAcceso obtenerUsuarioGestion(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario == null) {
            throw new SecurityException(ACCESS_DENIED_GESTION);
        }
        if ("Propietario".equals(usuario.getRol()) || "Secretaria".equals(usuario.getRol())) {
            return usuario;
        }
        if ("Cliente".equals(usuario.getRol())) {
            throw new SecurityException(ACCESS_DENIED_CLIENTE);
        }
        throw new SecurityException(ACCESS_DENIED_GESTION);
    }

    private UsuarioAcceso obtenerUsuarioCliente(String correoRemitente) throws SQLException {
        UsuarioAcceso usuario = obtenerUsuarioActivo(correoRemitente);
        if (usuario != null && "Cliente".equals(usuario.getRol())) {
            return usuario;
        }
        throw new SecurityException("Acceso denegado. Solo el Cliente puede consultar sus propios pagos.");
    }

    private UsuarioAcceso obtenerUsuarioActivo(String correoRemitente) throws SQLException {
        if (correoRemitente == null || correoRemitente.trim().isEmpty()) {
            return null;
        }
        return dPago.obtenerUsuarioActivoPorEmail(correoRemitente.trim());
    }

    private void validarSuscripcionActiva(int idSuscripcion) throws SQLException {
        if (!dPago.existeSuscripcionActivaConClienteActivo(idSuscripcion)) {
            throw new IllegalArgumentException("id_suscripcion debe existir, estar ACTIVO y pertenecer a un Cliente ACTIVO");
        }
    }

    private void validarPagoActivo(int idPago) throws SQLException {
        if (!dPago.estaPagoActivo(idPago)) {
            throw new IllegalArgumentException("No existe pago activo con id " + idPago);
        }
    }

    private void validarPagoParaCuota(PagoCuotaInfo pago, int numeroCuota, BigDecimal monto) {
        if ("INACTIVO".equals(pago.getEstado())) {
            throw new IllegalArgumentException("No se pueden registrar cuotas sobre pagos INACTIVOS");
        }
        if (!"CREDITO".equals(pago.getTipoPago())) {
            throw new IllegalArgumentException("Solo se pueden registrar cuotas sobre pagos CREDITO");
        }
        if (numeroCuota > pago.getCantidadCuotas()) {
            throw new IllegalArgumentException("numero_cuota no puede ser mayor que cantidad_cuotas");
        }
        if (pago.getCuotasPagadas() >= pago.getCantidadCuotas()) {
            throw new IllegalArgumentException("El pago ya tiene todas sus cuotas registradas");
        }
        if (monto.compareTo(pago.getMontoTotal()) > 0) {
            throw new IllegalArgumentException("monto no puede superar monto_total");
        }
    }

    private void validarPagoQr(PagoQrInfo pagoQrInfo, int idPago) {
        if (!"QR".equals(pagoQrInfo.getMetodo())) {
            throw new IllegalArgumentException("El pago con id " + idPago + " no fue generado con metodo QR");
        }
        if (pagoQrInfo.getPagofacilTransactionId() == null) {
            throw new IllegalArgumentException("El pago QR no tiene pagofacil_transaction_id registrado");
        }
    }

    private String parseMetodo(String value) {
        validarObligatorio(value, "metodo");
        String metodo = value.trim().toUpperCase();
        if (!"EFECTIVO".equals(metodo) && !"TRANSFERENCIA".equals(metodo) && !"QR".equals(metodo)) {
            throw new IllegalArgumentException("metodo debe ser EFECTIVO, TRANSFERENCIA o QR");
        }
        return metodo;
    }

    private String parseTipoPago(String value) {
        validarObligatorio(value, "tipo_pago");
        String tipoPago = value.trim().toUpperCase();
        if (!"CONTADO".equals(tipoPago) && !"CREDITO".equals(tipoPago)) {
            throw new IllegalArgumentException("tipo_pago debe ser CONTADO o CREDITO");
        }
        return tipoPago;
    }

    private String parseTexto(String value, String name) {
        validarObligatorio(value, name);
        return value.trim();
    }

    private BigDecimal parseMonto(String value, String name) {
        validarObligatorio(value, name);
        try {
            BigDecimal monto = new BigDecimal(value.trim());
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(name + " debe ser mayor a 0");
            }
            return monto;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " debe ser numerico");
        }
    }

    private int parseCantidadCuotas(String value) {
        int cantidad = parseId(value, "cantidad_cuotas");
        if (cantidad < 2) {
            throw new IllegalArgumentException("cantidad_cuotas debe ser mayor o igual a 2");
        }
        return cantidad;
    }

    private int parseCantidadCuotasFlexible(String value, String tipoPago) {
        int cantidad = parseId(value, "cantidad_cuotas");
        if ("CONTADO".equals(tipoPago) && cantidad != 1) {
            throw new IllegalArgumentException("cantidad_cuotas debe ser 1 para pagos CONTADO");
        }
        if ("CREDITO".equals(tipoPago) && cantidad < 2) {
            throw new IllegalArgumentException("cantidad_cuotas debe ser mayor o igual a 2 para pagos CREDITO");
        }
        return cantidad;
    }

    private int parseNumeroCuota(String value) {
        int numero = parseId(value, "numero_cuota");
        if (numero <= 0) {
            throw new IllegalArgumentException("numero_cuota debe ser mayor a 0");
        }
        return numero;
    }

    private Date parseFecha(String value, String name) {
        validarObligatorio(value, name);
        try {
            return Date.valueOf(value.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(name + " debe tener formato YYYY-MM-DD");
        }
    }

    private int parseId(String value, String name) {
        validarObligatorio(value, name);
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " debe ser numerico");
        }
    }

    private void validarObligatorio(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " es obligatorio");
        }
    }

    private QrResponse generarQrExterno(SuscripcionClienteInfo cliente, String paymentNumber, BigDecimal monto,
            String documentoIdentidad, String telefono, String concepto) {
        try {
            return pagoFacilClient.generarQr(new QrRequest(
                    cliente.getCliente(),
                    documentoIdentidad,
                    telefono,
                    cliente.getEmail(),
                    paymentNumber,
                    monto,
                    String.valueOf(cliente.getIdCliente()),
                    pagoFacilClient.getConfiguredCallbackUrl(),
                    concepto
            ));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el QR con PagoFacil: " + ex.getMessage(), ex);
        }
    }

    private TransactionStatusResponse consultarTransaccionExterna(Long pagofacilTransactionId) {
        try {
            return pagoFacilClient.consultarTransaccion(pagofacilTransactionId.longValue());
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo consultar la transaccion QR en PagoFacil: "
                    + ex.getMessage(), ex);
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "SIN_ESTADO";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isPaidStatus(String status) {
        return "PAGADO".equals(status)
                || "SUCCESS".equals(status)
                || "COMPLETED".equals(status)
                || "APROBADO".equals(status)
                || "APPROVED".equals(status)
                || "PROCESSED".equals(status);
    }

    private void requireSize(List<String> parametros, int expected, String usage) {
        if (parametros.size() != expected) {
            throw new IllegalArgumentException("Parametros invalidos. Uso: " + usage);
        }
    }
}
