/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoemail;

import Comunicacion.MailVerificationThread;
import Conexion.IEmailEventListener;
import Integracion.PagoFacilCallbackServer;
import Metodos.Membresias;
import Metodos.Pagos;
import Metodos.Paquetes;
import Metodos.Reportes;
import Metodos.Rutinas;
import Metodos.Seguimientos;
import Metodos.Suscripciones;
import Metodos.Usuarios;
import Utils.AppEnv;
import Utils.Email;
import Utils.HtmlBuilder;
import java.util.ArrayList;
import java.util.List;

public class MailAplication implements IEmailEventListener {

    private final MailVerificationThread mailVerificationThread;
    private final Usuarios usuarios;
    private final Membresias membresias;
    private final Paquetes paquetes;
    private final Suscripciones suscripciones;
    private final Pagos pagos;
    private final Rutinas rutinas;
    private final Seguimientos seguimientos;
    private final Reportes reportes;
    private final String smtpFrom;
    private PagoFacilCallbackServer pagoFacilCallbackServer;

    public MailAplication() {
        mailVerificationThread = new MailVerificationThread();
        mailVerificationThread.setEmailEventListener(MailAplication.this);
        usuarios = new Usuarios();
        membresias = new Membresias();
        paquetes = new Paquetes();
        suscripciones = new Suscripciones();
        pagos = new Pagos();
        rutinas = new Rutinas();
        seguimientos = new Seguimientos();
        reportes = new Reportes();
        smtpFrom = AppEnv.get("PROYECTOEMAIL_SMTP_FROM", "").trim();
    }

    public void start() throws InterruptedException {
        startPagoFacilCallbackServerIfEnabled();
        Thread thread = new Thread(mailVerificationThread);
        thread.setName("Mail Verfication Thread");
        thread.start();
    }

    @Override
    public void onReceiveEmailEvent(List<Email> emails) {
        System.out.println("onReceiveEmailEvent()");
        for (Email email : emails) {
            processEmail(email);
        }
    }

    private void processEmail(Email email) {
        if (shouldIgnore(email)) {
            System.out.println("Correo ignorado: from=" + email.getFrom() + " subject=" + email.getSubject());
            return;
        }

        String subject = email.getSubject() == null ? "" : email.getSubject().trim();
        if (subject.isEmpty()) {
            sendError(email.getFrom(), "Comando vacio", "El asunto del correo no contiene ningun comando.");
            return;
        }

        String command = getCommand(subject);
        String action = getAction(subject);
        List<String> params = getParams(subject);
        handleCommand(command, action, params, email);
    }

    private void handleCommand(String command, String action, List<String> params, Email email) {
        switch (command.toLowerCase()) {
            case "help":
            case "ayuda":
                send(email.getFrom(), HtmlBuilder.generateHelp());
                break;
            // CU1 Gestion de Usuarios: enruta las acciones recibidas por correo.
            case "usuario":
                usuarios.ejecutar(action, params, email.getFrom());
                break;
            // CU2 Gestion de Membresias: enruta acciones como mostrar, agregar y renovar.
            case "membresia":
                membresias.ejecutar(action, params, email.getFrom());
                break;
            case "paquete":
                paquetes.ejecutar(action, params, email.getFrom());
                break;
            case "suscripcion":
                suscripciones.ejecutar(action, params, email.getFrom());
                break;
            case "pago":
                pagos.ejecutar(action, params, email.getFrom());
                break;
            case "rutina":
                rutinas.ejecutar(action, params, email.getFrom());
                break;
            case "seguimiento":
                seguimientos.ejecutar(action, params, email.getFrom());
                break;
            case "reporte":
                reportes.ejecutar(action, params, email.getFrom());
                break;
            default:
                sendError(
                        email.getFrom(),
                        "Comando no configurado",
                        "El comando '" + command + "' no tiene un caso de uso asociado todavia."
                );
                System.out.println("Comando no configurado: " + command + " params=" + params);
                break;
        }
    }

    private String getCommand(String subject) {
        int bracketIndex = subject.indexOf('[');
        String commandPart = bracketIndex >= 0 ? subject.substring(0, bracketIndex) : subject;
        commandPart = commandPart.trim();
        if (commandPart.isEmpty()) {
            return "";
        }
        return commandPart.split("\\s+")[0];
    }

    private String getAction(String subject) {
        int bracketIndex = subject.indexOf('[');
        String commandPart = bracketIndex >= 0 ? subject.substring(0, bracketIndex) : subject;
        String[] parts = commandPart.trim().split("\\s+");
        if (parts.length < 2) {
            return "";
        }
        return parts[1];
    }

    private List<String> getParams(String subject) {
        List<String> params = new ArrayList<>();
        int start = subject.indexOf('[');
        int end = subject.lastIndexOf(']');
        if (start < 0 || end <= start) {
            return params;
        }
        String rawParams = subject.substring(start + 1, end).trim();
        if (rawParams.isEmpty()) {
            return params;
        }
        for (String param : rawParams.split(";")) {
            params.add(param.trim());
        }
        return params;
    }

    private void sendError(String to, String title, String message) {
        send(to, HtmlBuilder.generateError(title, message));
    }

    private void send(String to, String html) {
        Email emailObject = new Email(to, Email.SUBJECT, html);
        Email.sendEmail(emailObject);
    }

    private boolean shouldIgnore(Email email) {
        String from = email.getFrom() == null ? "" : email.getFrom().trim();
        String subject = email.getSubject() == null ? "" : email.getSubject().trim();
        if (from.isEmpty()) {
            return true;
        }
        // Permite comandos enviados desde la misma cuenta monitoreada, pero evita
        // reprocesar las respuestas automaticas que la propia app genera.
        if (!smtpFrom.isEmpty()
                && from.equalsIgnoreCase(smtpFrom)
                && Email.SUBJECT.equalsIgnoreCase(subject)) {
            return true;
        }
        return from.toLowerCase().startsWith("mailer-daemon@");
    }

    private void startPagoFacilCallbackServerIfEnabled() {
        String enabledValue = AppEnv.get("PROYECTOEMAIL_CALLBACK_ENABLED", "false").trim();
        if ("false".equalsIgnoreCase(enabledValue) || "0".equals(enabledValue)) {
            System.out.println("PagoFacil callback server deshabilitado por entorno.");
            return;
        }
        try {
            pagoFacilCallbackServer = new PagoFacilCallbackServer();
            pagoFacilCallbackServer.start();
        } catch (Exception ex) {
            System.out.println("No se pudo iniciar PagoFacil callback server: " + ex.getMessage());
        }
    }

}
