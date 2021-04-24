package com.telcreat.aio.service;


import com.telcreat.aio.model.VerificationToken;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmail {

        //send ---> Function used for sending mails.
    public void send(String email, VerificationToken verificationToken) {
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
            message.setSubject("Verification is required! AIO Commerce");

            String passwordRecoveryLink = "\n\n\nIf you think there is something wrong, change your password: http://localhost:8080/auth/recoverPassword?token=" + verificationToken.getToken() + "&code=" + verificationToken.getCode();
            message.setText("Your verification code is: " + verificationToken.getCode() + passwordRecoveryLink);

            // Send email
            Transport.send(message);

            System.out.println("Verification message sent successfully....");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}