package com.telcreat.aio.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSenderThread implements Runnable{

    private final String email;
    private final String subject;
    private final String messageText;

    @Override
    public void run() {
        sendEmail(email,subject,messageText);
    }

    public EmailSenderThread(String email, String subject, String messageText) {
        this.email = email;
        this.subject = subject;
        this.messageText = messageText;
    }

    public void sendEmail(String email, String subject, String messageText) {
        final String sender = "aio@telcreat.com";
        final String password = "Es8qYcGcpmvs";

        Properties props = new Properties();
        props.put("mail.smtp.host", "mail.atreshost.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", "*");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject(subject);
            message.setText(messageText);

            // Send email
            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
