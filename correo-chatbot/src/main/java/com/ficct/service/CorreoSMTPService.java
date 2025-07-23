package com.empresa.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.util.Properties;

/**
 * Servicio encargado de enviar correos electrónicos usando SMTP.
 */
public class CorreoSMTPService {

    // Estos valores deberían cargarse desde un archivo .properties en producción
    private static final String REMITENTE = "tu_correo@gmail.com";
    private static final String CLAVE_APP = "tu_clave_app";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    /**
     * Envía un correo a un destinatario.
     *
     * @param destinatario Dirección de correo del destinatario
     * @param asunto       Asunto del correo
     * @param mensaje      Cuerpo del mensaje
     */
    public static void enviarCorreo(String destinatario, String asunto, String mensaje) {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, CLAVE_APP);
            }
        });

        try {
            Message correo = new MimeMessage(session);
            correo.setFrom(new InternetAddress(REMITENTE));
            correo.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            correo.setSubject(asunto);
            correo.setText(mensaje);

            Transport.send(correo);
            System.out.println("Correo enviado a " + destinatario);

        } catch (MessagingException e) {
            System.err.println("Error al enviar correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
