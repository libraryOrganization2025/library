package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final String username;
    private final String password;
    private final Session session;

    public EmailService(String username, String password) {
        this.username = username;
        this.password = password;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");     // نفعّل STARTTLS
        props.put("mail.smtp.host", "smtp.gmail.com");      // سيرفر Gmail
        props.put("mail.smtp.port", "587");                 // بورت TLS

        // ⭐ أهم سطرين لحل مشكلة SSL:
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // ثق بهذا الهوست
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");    // استخدم TLS 1.2

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendEmail(String to, String subject, String text) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(text);

            Transport.send(message);
            //System.out.println("✅ Email sent to " + to);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to send email to " + to);
        }
    }
}
