package com.telcreat.aio.service;


import com.telcreat.aio.model.ShopOrder;
import com.telcreat.aio.model.VerificationToken;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmail {

        //send ---> Function used for sending mails.
    public void sendVerification(String email, VerificationToken verificationToken) {
        String subject = "Verification is required! AIO Commerce";
        String userCode = "Your verification code is: " + verificationToken.getCode();
        String userCodeLink = "\nEnter your code here: http://localhost:8080/auth/verification?token=" + verificationToken.getToken();
        String userPasswordRecoveryLink = "\n\n\nIf you think there is something wrong, change your password: http://localhost:8080/auth/recoverPassword?token=" + verificationToken.getToken() + "&code=" + verificationToken.getCode();
        String messageText = userCode + userCodeLink + userPasswordRecoveryLink;

        Thread thread = new Thread(new EmailSenderThread(email, subject, messageText));
        thread.start();

    }

    public void sendOrderCancelledNotification(ShopOrder shopOrder){
        String subject = "Order " + shopOrder.getId() + " has been cancelled.";
        String part1 = "We would like to inform you that order with ID:" + shopOrder.getId() + " has been cancelled";
        String part2_user = "\nPlease check your other order status here: http://localhost:8080/user/myOrders?userId=" + shopOrder.getUser().getId();
        String part2_owner = "\nPlease check your other order status here: http://localhost:8080/shop/myOrders?shopId=" + shopOrder.getShop().getId();

        Thread userThread = new Thread(new EmailSenderThread(shopOrder.getUser().getEmail(), subject, part1 + part2_user));
        userThread.start();

        Thread ownerThread = new Thread(new EmailSenderThread(shopOrder.getShop().getOwner().getEmail(), subject, part1 + part2_owner));
        ownerThread.start();
    }

    public void sendNewOrderNotificationToOwner(String email, ShopOrder shopOrder){
        String subject = "You received a new Order";
        String part1 = "You received a new order from " + shopOrder.getUser().getName();
        String part2 = "\nClick here to check the new order: http://localhost:8080/shop/myOrders?shopId=" + shopOrder.getShop().getId();
        String messageText = part1 + part2;

        Thread thread = new Thread(new EmailSenderThread(shopOrder.getShop().getOwner().getEmail(), subject, messageText));
        thread.start();
    }
}