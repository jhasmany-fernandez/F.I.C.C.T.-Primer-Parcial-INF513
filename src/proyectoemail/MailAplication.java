/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package proyectoemail;

import Comunicacion.MailVerificationThread;
import Conexion.IEmailEventListener;
import Metodos.Membresias;
import Metodos.Usuarios;
import Utils.Email;
import Utils.HtmlBuilder;
import java.util.ArrayList;
import java.util.List;

public class MailAplication implements IEmailEventListener {

    private final MailVerificationThread mailVerificationThread;
    private final Usuarios usuarios;
    private final Membresias membresias;
    private final String smtpFrom;

    public MailAplication() {
        mailVerificationThread = new MailVerificationThread();
        mailVerificationThread.setEmailEventListener(MailAplication.this);
        usuarios = new Usuarios();
        membresias = new Membresias();
        smtpFrom = System.getenv().getOrDefault("PROYECTOEMAIL_SMTP_FROM", "").trim();
    }

    public void start() throws InterruptedException {
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
        if (from.isEmpty()) {
            return true;
        }
        if (!smtpFrom.isEmpty() && from.equalsIgnoreCase(smtpFrom)) {
            return true;
        }
        return from.toLowerCase().startsWith("mailer-daemon@");
    }

}
