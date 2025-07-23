package com.ficct.service;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class ClientePOPService {

    public void recibirCorreos(String host, String user, String password) {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "pop3");
            props.put("mail.pop3.host", host);
            props.put("mail.pop3.port", "110");

            Session session = Session.getInstance(props);
            Store store = session.getStore("pop3");
            store.connect(host, user, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] mensajes = inbox.getMessages();
            for (Message mensaje : mensajes) {
                System.out.println("De: " + mensaje.getFrom()[0]);
                System.out.println("Asunto: " + mensaje.getSubject());
                System.out.println("----------------------------------");
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
