package Comunicacion;

import Utils.Email;
import Utils.MimeCodec;
import Utils.AppEnv;
import java.net.*;
import java.io.*;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SendEmailThread implements Runnable {

    private static final String END = "\r\n";

    String servidor = AppEnv.get("PROYECTOEMAIL_SMTP_HOST", "mail.tecnoweb.org.bo");
    int puerto = parseIntEnv("PROYECTOEMAIL_SMTP_PORT", 25);
    String user_emisor = AppEnv.get("PROYECTOEMAIL_SMTP_FROM", "");
    String smtpUser = AppEnv.get("PROYECTOEMAIL_SMTP_USER", user_emisor);
    String smtpPassword = AppEnv.get("PROYECTOEMAIL_SMTP_PASSWORD", "");
    String user_receptor;
    private Email email;

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

            send(salida, "EHLO " + servidor);
            String ehloResponse = readResponse(entrada);
            System.out.println("S : " + ehloResponse);
            authenticateIfConfigured(salida, entrada, ehloResponse);

            send(salida, "MAIL FROM:<" + user_emisor + ">");
            System.out.println("S : " + readResponse(entrada));

            send(salida, "RCPT TO:<" + user_receptor + ">");
            System.out.println("S : " + readResponse(entrada));

            send(salida, "DATA");
            System.out.println("S : " + readResponse(entrada));

            String date = ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH));
            String msgId = "<" + System.currentTimeMillis() + "." + Math.abs(email.hashCode()) + "@" + servidor + ">";

            StringBuilder headers = new StringBuilder();
            headers.append("From: ").append(user_emisor).append(END);
            headers.append("To: ").append(user_receptor).append(END);
            headers.append("Subject: ").append(MimeCodec.encodeText(email.getSubject())).append(END);
            headers.append("Date: ").append(date).append(END);
            headers.append("Message-ID: ").append(msgId).append(END);

            String body = buildMessageContent(email.getMessage());
            String full = headers.toString() + body + "." + END;

            System.out.println("C : [DATA sending " + full.length() + " bytes]");
            salida.write(full.getBytes(StandardCharsets.UTF_8));
            System.out.println("S : " + readResponse(entrada));

            send(salida, "QUIT");
            System.out.println("S : " + readResponse(entrada));

        } catch (Exception e) {
            System.out.println("C : " + e.getMessage());
        }
    }

    private void authenticateIfConfigured(DataOutputStream salida, BufferedReader entrada, String ehloResponse) throws Exception {
        if (smtpUser == null || smtpUser.trim().isEmpty()
                || smtpPassword == null || smtpPassword.trim().isEmpty()) {
            return;
        }

        String authLine = findAuthLine(ehloResponse);
        if (authLine == null || authLine.trim().isEmpty()) {
            throw new IllegalStateException("El servidor SMTP no anuncia mecanismos AUTH");
        }

        String authUpper = authLine.toUpperCase(Locale.ROOT);
        if (authUpper.contains("CRAM-MD5")) {
            authCramMd5(salida, entrada);
            return;
        }
        if (authUpper.contains("LOGIN")) {
            authLogin(salida, entrada);
            return;
        }
        if (authUpper.contains("PLAIN")) {
            authPlain(salida, entrada);
            return;
        }
        throw new IllegalStateException("El servidor SMTP no ofrece un mecanismo AUTH soportado: " + authLine);
    }

    private void authCramMd5(DataOutputStream salida, BufferedReader entrada) throws Exception {
        send(salida, "AUTH CRAM-MD5");
        String challenge = readResponse(entrada);
        System.out.println("S : " + challenge);
        if (!challenge.startsWith("334 ")) {
            throw new IllegalStateException("SMTP AUTH CRAM-MD5 fallo: " + challenge);
        }
        String challengeBase64 = challenge.substring(4).trim();
        byte[] challengeBytes = Base64.getDecoder().decode(challengeBase64);
        Mac mac = Mac.getInstance("HmacMD5");
        mac.init(new SecretKeySpec(smtpPassword.getBytes(StandardCharsets.UTF_8), "HmacMD5"));
        byte[] digest = mac.doFinal(challengeBytes);
        String responseText = smtpUser + " " + toHex(digest);
        send(salida, Base64.getEncoder().encodeToString(responseText.getBytes(StandardCharsets.UTF_8)));
        String authResponse = readResponse(entrada);
        System.out.println("S : " + authResponse);
        if (!authResponse.startsWith("235")) {
            throw new IllegalStateException("Autenticacion SMTP rechazada: " + authResponse);
        }
    }

    private void authLogin(DataOutputStream salida, BufferedReader entrada) throws Exception {
        send(salida, "AUTH LOGIN");
        String userPrompt = readResponse(entrada);
        System.out.println("S : " + userPrompt);
        if (!userPrompt.startsWith("334")) {
            throw new IllegalStateException("SMTP AUTH LOGIN fallo: " + userPrompt);
        }
        send(salida, Base64.getEncoder().encodeToString(smtpUser.getBytes(StandardCharsets.UTF_8)));
        String passPrompt = readResponse(entrada);
        System.out.println("S : " + passPrompt);
        if (!passPrompt.startsWith("334")) {
            throw new IllegalStateException("SMTP AUTH LOGIN usuario rechazado: " + passPrompt);
        }
        send(salida, Base64.getEncoder().encodeToString(smtpPassword.getBytes(StandardCharsets.UTF_8)));
        String authResponse = readResponse(entrada);
        System.out.println("S : " + authResponse);
        if (!authResponse.startsWith("235")) {
            throw new IllegalStateException("Autenticacion SMTP rechazada: " + authResponse);
        }
    }

    private void authPlain(DataOutputStream salida, BufferedReader entrada) throws Exception {
        String payload = '\0' + smtpUser + '\0' + smtpPassword;
        send(salida, "AUTH PLAIN " + Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8)));
        String authResponse = readResponse(entrada);
        System.out.println("S : " + authResponse);
        if (!authResponse.startsWith("235")) {
            throw new IllegalStateException("Autenticacion SMTP rechazada: " + authResponse);
        }
    }

    private String findAuthLine(String ehloResponse) {
        String[] lines = ehloResponse.split("\\r?\\n");
        for (String line : lines) {
            String normalized = line.trim();
            if (normalized.regionMatches(true, 0, "250-AUTH", 0, 8)
                    || normalized.regionMatches(true, 0, "250 AUTH", 0, 8)) {
                int index = normalized.toUpperCase(Locale.ROOT).indexOf("AUTH");
                return normalized.substring(index + 4).trim();
            }
        }
        return "";
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }

    private void send(DataOutputStream out, String line) throws IOException {
        System.out.println("C : " + line);
        out.write((line + END).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Builds MIME body. If the HTML contains data: URI images, upgrades to
     * multipart/related with CID inline attachments so Gmail renders them.
     */
    private String buildMessageContent(String htmlMessage) {
        List<String[]> images = new ArrayList<>(); // [cid, base64data]
        String modifiedHtml = extractAndReplaceImages(htmlMessage, images);

        if (images.isEmpty()) {
            return "MIME-Version: 1.0" + END
                    + "Content-Type: text/html; charset=UTF-8" + END
                    + END
                    + modifiedHtml + END;
        }

        String boundary = "----=_Rel_" + Long.toHexString(System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        sb.append("MIME-Version: 1.0").append(END);
        sb.append("Content-Type: multipart/related; boundary=\"").append(boundary).append("\"").append(END);
        sb.append(END);

        sb.append("--").append(boundary).append(END);
        sb.append("Content-Type: text/html; charset=UTF-8").append(END);
        sb.append(END);
        sb.append(modifiedHtml).append(END);

        for (String[] img : images) {
            sb.append("--").append(boundary).append(END);
            sb.append("Content-Type: image/png").append(END);
            sb.append("Content-Transfer-Encoding: base64").append(END);
            sb.append("Content-ID: <").append(img[0]).append(">").append(END);
            sb.append("Content-Disposition: inline; filename=\"qr.png\"").append(END);
            sb.append(END);
            sb.append(wrapBase64(img[1])).append(END);
        }

        sb.append("--").append(boundary).append("--").append(END);
        return sb.toString();
    }

    private String extractAndReplaceImages(String html, List<String[]> images) {
        Pattern p = Pattern.compile("src=\"data:image/[^;\"]+;base64,([^\"]+)\"");
        Matcher m = p.matcher(html);
        StringBuffer sb = new StringBuffer();
        int idx = 0;
        while (m.find()) {
            String cid = "img" + idx + "@pagofacil";
            images.add(new String[]{cid, m.group(1)});
            m.appendReplacement(sb, Matcher.quoteReplacement("src=\"cid:" + cid + "\""));
            idx++;
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String wrapBase64(String b64) {
        StringBuilder sb = new StringBuilder();
        int len = b64.length();
        for (int i = 0; i < len; i += 76) {
            sb.append(b64, i, Math.min(i + 76, len)).append(END);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
    }

    private static int parseIntEnv(String key, int defaultValue) {
        return AppEnv.getInt(key, defaultValue);
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
