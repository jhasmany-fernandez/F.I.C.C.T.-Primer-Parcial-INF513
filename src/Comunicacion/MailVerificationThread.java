package Comunicacion;

import Conexion.IEmailEventListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.sasl.AuthenticationException;
import Utils.AppEnv;
import Utils.Command;
import Utils.Email;
import Utils.Extractor;


public class MailVerificationThread implements Runnable {
    
    private final static String HOST = AppEnv.get("PROYECTOEMAIL_POP3_HOST", "mail.tecnoweb.org.bo");
    private final static int PORT_POP = parseIntEnv("PROYECTOEMAIL_POP3_PORT", 110);
    private final static String USER = sanitizeCredential(
            AppEnv.get("PROYECTOEMAIL_POP3_USER", ""),
            ""
    );
    private final static String PASSWORD = sanitizeCredential(
            AppEnv.get("PROYECTOEMAIL_POP3_PASSWORD", ""),
            ""
    );
    
    /*private final static int PORT_POP = 995;
    private final static String HOST = "pop.googlemail.com";
    private final static String USER = "fernandocarrasc591@gmail.com";
    private final static String PASSWORD = "ifpcxviqldhhpyvn";*/
    
    private Socket socket;
    private BufferedReader input;
    private DataOutputStream output;
    
    private IEmailEventListener emailEventListener;

    public IEmailEventListener getEmailEventListener() {
        return emailEventListener;
    }

    public void setEmailEventListener(IEmailEventListener emailEventListener) {
        this.emailEventListener = emailEventListener;
    }
    
    public MailVerificationThread() {
        socket = null;
        input = null;
        output = null;
    }

    @Override
    public void run() {
        while(true) {
            try {
                List<Email> emails = null;
                System.out.println("POP3 CONFIG HOST: " + HOST + ":" + PORT_POP);
                System.out.println("POP3 CONFIG USER: " + USER);
                System.out.println("POP3 CONFIG PASS LENGTH: " + (PASSWORD == null ? 0 : PASSWORD.length()));
                socket = new Socket(HOST, PORT_POP);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new DataOutputStream(socket.getOutputStream());
                System.out.println("**************** Conexion establecida ****************");
                
                authUser(USER, PASSWORD);
                
                int count = getEmailCount();
                if(count > 0 ) {
                    emails = getEmails(count);
                    System.out.println(emails);
                    deleteEmails(count);
                }
                output.writeBytes(Command.quit());
                input.readLine();
                input.close();
                output.close();
                socket.close();
                System.out.println("**************** Conexion cerrada ****************");
                
                if(count > 0) {
                    System.out.println("ENtraditaaaaaaaaaaaaaaaa");
                    try {
                        emailEventListener.onReceiveEmailEvent(emails);
                    } catch (Throwable ex) {
                        Logger.getLogger(MailVerificationThread.class.getName()).log(Level.SEVERE,
                                "Error procesando correos recibidos", ex);
                    }
                }
                
                Thread.sleep(10000);
                
            } catch (IOException ex) {
                Logger.getLogger(MailVerificationThread.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MailVerificationThread.class.getName()).log(Level.SEVERE, null, ex);
                Thread.currentThread().interrupt();
                return;
            } catch (Throwable ex) {
                Logger.getLogger(MailVerificationThread.class.getName()).log(Level.SEVERE,
                        "Fallo no controlado en el hilo POP3", ex);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
    
    private void authUser(String email, String password) throws IOException {
        if(socket != null && input != null && output != null) {
            String welcome = input.readLine();
            System.out.println("POP3 WELCOME: " + welcome);
            output.writeBytes(Command.user(email));
            String userResponse = input.readLine();
            System.out.println("POP3 USER RESPONSE: " + userResponse);
            output.writeBytes(Command.pass(password));
            String passResponse = input.readLine();
            System.out.println("POP3 PASS RESPONSE: " + passResponse);
            if ((userResponse != null && userResponse.contains("-ERR"))
                    || (passResponse != null && passResponse.contains("-ERR"))) {
                throw new AuthenticationException("POP3 auth failed. USER=" + userResponse + " PASS=" + passResponse);
            }
        }
    }

    private static String sanitizeCredential(String raw, String defaultValue) {
        String value = (raw == null || raw.isEmpty()) ? defaultValue : raw;
        value = value.trim().replace("\r", "").replace("\n", "");
        if (value.length() >= 2) {
            boolean quoted = (value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'"));
            if (quoted) {
                value = value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }

    private static int parseIntEnv(String key, int defaultValue) {
        return AppEnv.getInt(key, defaultValue);
    }
    
    private void deleteEmails(int emails) throws IOException {
        for(int i = 1; i <= emails; i++) {
            output.writeBytes(Command.dele(i));
        }
    }
    
    private int getEmailCount() throws IOException {
        output.writeBytes(Command.stat());
        String line = input.readLine();
        String[] data = line.split(" ");        
        return Integer.parseInt(data[1]);
    }
    
    private List<Email> getEmails(int count) throws IOException {
        List<Email> emails = new ArrayList<>();
        for(int i = 1; i <= count; i++) {
            output.writeBytes(Command.retr(i));
            String text = readMultiline();
            emails.add(Extractor.getEmail(text));
        }
        return emails;
    }
    
    private String readMultiline() throws IOException {
        String lines = "";
        while(true) {
            String line = input.readLine();
            if(line == null) {
                throw new IOException("Server no responde (ocurrio un error al abrir el correo)");
            }
            if(line.equals(".")) {
                break;
            }
            lines = lines + "\n" + line;
        }
        return lines;
    }
}
