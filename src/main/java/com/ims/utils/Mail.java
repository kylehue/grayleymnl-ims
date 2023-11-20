package com.ims.utils;

import com.ims.database.DBCategories;
import com.ims.model.objects.CategoryObject;
import javafx.concurrent.Task;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Mail {
    private static Session session;
    public static void initialize() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.socketFactory.port", "587");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        
        Properties env = Env.get();
        
        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    env.getProperty("mail.email"),
                    env.getProperty("mail.password")
                );
            }
        });
    }
    
    public static void send(String to, String subject, String msg) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(
                    Env.get().getProperty("mail.email")
                ));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                message.setSubject(subject);
                message.setText(msg);
                
                Transport.send(message);
                return null;
            }
        };
        
        task.setOnSucceeded(e -> {
            System.out.println("Email sent successfully!");
        });
        
        task.setOnFailed(e -> {
            System.out.println(e);
        });
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(task);
        executor.shutdown();
    }
}
