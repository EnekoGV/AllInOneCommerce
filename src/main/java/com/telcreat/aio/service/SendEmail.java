package com.telcreat.aio.service;


import com.telcreat.aio.model.ShopOrder;
import com.telcreat.aio.model.VerificationToken;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class SendEmail {

    private final String default_URL = "http://localhost:8080";

        //send ---> Function used for sending mails.
    public void sendVerification(String email, VerificationToken verificationToken) {
        String subject = "Verification is required! AIO Commerce";
        String userCode = "Your verification code is: " + verificationToken.getCode();
        String userCodeLink = "\nEnter your code here: " + default_URL +  "/auth/verification?token=" + verificationToken.getToken();
        String userPasswordRecoveryLink = "\n\n\nIf you think there is something wrong, change your password: " + default_URL +  "/auth/recoverPassword?token=" + verificationToken.getToken() + "&code=" + verificationToken.getCode();
        String messageText = userCode + userCodeLink + userPasswordRecoveryLink;

        Thread thread = new Thread(new EmailSenderThread(email, subject, messageText));
        thread.start();

    }

    public void sendOrderCancelledNotification(ShopOrder shopOrder){
        String subject = "Order " + shopOrder.getId() + " has been cancelled.";
        String part1 = "We would like to inform you that order with ID: " + shopOrder.getId() + " has been cancelled";
        String part2_user = "\nPlease check your other order status here: " + default_URL +  "/user/myOrders?userId=" + shopOrder.getUser().getId();
        String part2_owner = "\nPlease check your other order status here: " + default_URL +  "/shop/myOrders?shopId=" + shopOrder.getShop().getId();

        Thread userThread = new Thread(new EmailSenderThread(shopOrder.getUser().getEmail(), subject, part1 + part2_user));
        userThread.start();

        Thread ownerThread = new Thread(new EmailSenderThread(shopOrder.getShop().getOwner().getEmail(), subject, part1 + part2_owner));
        ownerThread.start();
    }

    public void sendOrderStatusUpdateNotificationToUser(ShopOrder shopOrder){
        String subject = "Order status updated.";
        String part1 = "We would like to inform you that the status of the order with ID: " + shopOrder.getId() + " has been updated to " + shopOrder.getShopOrderStatus().name();
        String part2 = "\nPlease check your other order status here: " + default_URL +  "/user/myOrders?userId=" + shopOrder.getUser().getId();
        String messageText = part1 + part2;

        Thread thread = new Thread(new EmailSenderThread(shopOrder.getUser().getEmail(), subject, messageText));
        thread.start();
    }

    public void sendNewOrderNotificationToOwner(ShopOrder shopOrder){
        String subject = "You received a new Order";
        String part1 = "You received a new order from " + shopOrder.getUser().getName();
        String part2 = "\nClick here to check the new order: " + default_URL +  "/shop/myOrders?shopId=" + shopOrder.getShop().getId();
        String messageText = part1 + part2;

        Thread thread = new Thread(new EmailSenderThread(shopOrder.getShop().getOwner().getEmail(), subject, messageText));
        thread.start();
    }
}