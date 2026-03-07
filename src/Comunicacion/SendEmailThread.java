package Comunicacion;

import Utils.Email;
import java.net.*;
import java.io.*;

public class SendEmailThread implements Runnable {

    private static final String END = "\r\n";

    String servidor = System.getenv().getOrDefault("PROYECTOEMAIL_SMTP_HOST", "mail.tecnoweb.org.bo");
    int puerto = parseIntEnv("PROYECTOEMAIL_SMTP_PORT", 25);
    String user_emisor = System.getenv().getOrDefault("PROYECTOEMAIL_SMTP_FROM", "grupo13sc@tecnoweb.org.bo");
    String user_receptor;
    String comando = "";
    private Email email;

    /*
    String servidor = "smtp.googlemail.com";
    int puerto = 465;
    String user_emisor = "fernandocarrasc591@gmail.com";
    String user_receptor;
    String comando = "";
    private Email email;*/

    public SendEmailThread(Email email) {
        this.email = email;
        this.user_receptor = email.getTo();
    }

    @Override
    public void run() {

        try (Socket skCliente = new Socket(servidor, puerto);
                BufferedReader entrada = new BufferedReader(new InputStreamReader(skCliente.getInputStream()));
                DataOutputStream salida = new DataOutputStream(skCliente.getOutputStream())) {
            System.out.println("S : " + readResponse(entrada));

            comando = "EHLO " + servidor + END;
            System.out.print("C : " + comando);
            salida.writeBytes(comando);
            System.out.println("S : " + readResponse(entrada));

            comando = "MAIL FROM:<" + user_emisor + ">" + END;
            System.out.print("C : " + comando);
            salida.writeBytes(comando);
            System.out.println("S : " + readResponse(entrada));

            comando = "RCPT TO:<" + user_receptor + ">" + END;
            System.out.print("C : " + comando);
            salida.writeBytes(comando);
            System.out.println("S : " + readResponse(entrada));

            comando = "DATA" + END;
            System.out.print("C : " + comando);
            salida.writeBytes(comando);
            System.out.println("S : " + readResponse(entrada));

            comando = "Subject: " + email.getSubject() + END;
            comando += "Content-Type: text/html; charset=UTF-8" + END;
            comando += END;
            comando += email.getMessage() + END;
            comando += "." + END;

            System.out.print("C : " + comando);
            salida.writeBytes(comando);
            System.out.println("S : " + readResponse(entrada));

            comando = "QUIT" + END;
            System.out.print("C :" + comando);
            salida.writeBytes(comando);
            System.out.println("S :" + readResponse(entrada));
        } catch (Exception e) {
            System.out.println(" C : " + e.getMessage());
        }

        /*email.getTo();
         email.getSubject();
        email.getMessage();*/
    }

    public static void main(String[] args) {

    }

    private static int parseIntEnv(String key, int defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static String readResponse(BufferedReader entrada) throws IOException {
        String line = entrada.readLine();
        if (line == null) {
            throw new IOException("SMTP server closed the connection");
        }
        StringBuilder response = new StringBuilder(line);
        while (line.length() >= 4 && line.charAt(3) == '-') {
            line = entrada.readLine();
            if (line == null) {
                break;
            }
            response.append('\n').append(line);
        }
        return response.toString();
    }

}
