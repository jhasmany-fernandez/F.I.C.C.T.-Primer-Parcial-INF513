package proyectoemail;

import Utils.AppEnv;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SmokeCorreo {

    private static final String END = "\r\n";

    public static void main(String[] args) throws Exception {
        String smtpHost = AppEnv.get("PROYECTOEMAIL_SMTP_HOST", "mail.tecnoweb.org.bo");
        int smtpPort = AppEnv.getInt("PROYECTOEMAIL_SMTP_PORT", 25);
        String smtpFrom = AppEnv.get("PROYECTOEMAIL_SMTP_FROM", "").trim();
        String pop3Host = AppEnv.get("PROYECTOEMAIL_POP3_HOST", "mail.tecnoweb.org.bo");
        int pop3Port = AppEnv.getInt("PROYECTOEMAIL_POP3_PORT", 110);
        String pop3User = AppEnv.get("PROYECTOEMAIL_POP3_USER", "").trim();
        String pop3Password = AppEnv.get("PROYECTOEMAIL_POP3_PASSWORD", "").trim();
        String to = smtpFrom.isEmpty() ? pop3User : smtpFrom;
        String subject = args.length > 0 && !args[0].trim().isEmpty() ? args[0].trim() : "ayuda";

        requireConfigured("PROYECTOEMAIL_SMTP_FROM", smtpFrom);
        requireConfigured("PROYECTOEMAIL_POP3_USER", pop3User);
        requireConfigured("PROYECTOEMAIL_POP3_PASSWORD", pop3Password);

        sendSmtp(smtpHost, smtpPort, smtpFrom, to, subject);
        waitForPop3(pop3Host, pop3Port, pop3User, pop3Password, subject);
        System.out.println("Smoke de correo OK. SMTP y POP3 operativos.");
    }

    private static void sendSmtp(String host, int port, String from, String to, String subject) throws Exception {
        try (Socket socket = new Socket(host, port);
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            expectOk(readResponse(input), "SMTP banner");
            send(output, "EHLO " + host);
            expectOk(readResponse(input), "SMTP EHLO");
            send(output, "MAIL FROM:<" + from + ">");
            expectOk(readResponse(input), "SMTP MAIL FROM");
            send(output, "RCPT TO:<" + to + ">");
            expectOk(readResponse(input), "SMTP RCPT TO");
            send(output, "DATA");
            String dataResponse = readResponse(input);
            if (!dataResponse.startsWith("354")) {
                throw new IllegalStateException("SMTP DATA fallo: " + dataResponse);
            }

            StringBuilder body = new StringBuilder();
            body.append("From: ").append(from).append(END);
            body.append("To: ").append(to).append(END);
            body.append("Subject: ").append(subject).append(END);
            body.append("Content-Type: text/plain; charset=UTF-8").append(END);
            body.append(END);
            body.append("Smoke test ProyectoEmail").append(END);
            body.append(".").append(END);
            output.write(body.toString().getBytes(StandardCharsets.UTF_8));

            expectOk(readResponse(input), "SMTP mensaje");
            send(output, "QUIT");
            readResponse(input);
        }
    }

    private static void waitForPop3(String host, int port, String user, String password, String expectedSubject) throws Exception {
        long deadline = System.currentTimeMillis() + 30000L;
        while (System.currentTimeMillis() < deadline) {
            try (Socket socket = new Socket(host, port);
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
                expectOk(input.readLine(), "POP3 banner");
                output.writeBytes("USER " + user + END);
                expectOk(input.readLine(), "POP3 USER");
                output.writeBytes("PASS " + password + END);
                expectOk(input.readLine(), "POP3 PASS");
                output.writeBytes("STAT" + END);
                String stat = input.readLine();
                expectOk(stat, "POP3 STAT");
                int count = parseCount(stat);
                for (int i = 1; i <= count; i++) {
                    output.writeBytes("TOP " + i + " 20" + END);
                    String header = readMultiline(input);
                    if (header.contains("Subject: " + expectedSubject)) {
                        output.writeBytes("QUIT" + END);
                        input.readLine();
                        return;
                    }
                }
                output.writeBytes("QUIT" + END);
                input.readLine();
            }
            Thread.sleep(2000L);
        }
        throw new IllegalStateException("No se encontro en POP3 el correo de prueba con asunto '" + expectedSubject + "'");
    }

    private static int parseCount(String statLine) {
        String[] parts = statLine.split("\\s+");
        if (parts.length < 2) {
            throw new IllegalStateException("Respuesta POP3 STAT invalida: " + statLine);
        }
        return Integer.parseInt(parts[1]);
    }

    private static String readMultiline(BufferedReader input) throws Exception {
        StringBuilder lines = new StringBuilder();
        String first = input.readLine();
        expectOk(first, "POP3 TOP");
        while (true) {
            String line = input.readLine();
            if (line == null || ".".equals(line)) {
                break;
            }
            lines.append(line).append('\n');
        }
        return lines.toString();
    }

    private static void send(DataOutputStream output, String line) throws Exception {
        output.write((line + END).getBytes(StandardCharsets.UTF_8));
    }

    private static String readResponse(BufferedReader input) throws Exception {
        String line = input.readLine();
        if (line == null) {
            throw new IllegalStateException("El servidor cerro la conexion");
        }
        StringBuilder response = new StringBuilder(line);
        while (line.length() >= 4 && line.charAt(3) == '-') {
            line = input.readLine();
            if (line == null) {
                break;
            }
            response.append('\n').append(line);
        }
        return response.toString();
    }

    private static void expectOk(String response, String step) {
        if (response == null) {
            throw new IllegalStateException(step + " sin respuesta");
        }
        if (response.startsWith("-ERR")) {
            throw new IllegalStateException(step + " fallo: " + response);
        }
        if (!(response.startsWith("+OK") || response.startsWith("2") || response.startsWith("3"))) {
            throw new IllegalStateException(step + " respuesta inesperada: " + response);
        }
    }

    private static void requireConfigured(String key, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Falta configurar " + key);
        }
    }
}
