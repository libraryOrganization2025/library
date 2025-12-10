package service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Service class responsible for sending emails via SMTP using Gmail.
 * <p>
 * Configures a mail {@link Session} with authentication and TLS security.
 * Provides a method to send plain text emails to a specified recipient.
 * </p>
 *
 * @author Sara
 * @version 1.0
 */
public class EmailService {

    private final String username;
    private final String password;
    private final Session session;

    /**
     * Constructs an {@link EmailService} with the given Gmail credentials.
     * <p>
     * Sets up the SMTP session with authentication and TLS.
     * </p>
     *
     * @param username the Gmail email address used as sender
     * @param password the app-specific password or Gmail password
     */
    public EmailService(String username, String password) {
        this.username = username;
        this.password = password;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Sends a plain text email to a specified recipient.
     *
     * @param to the recipient's email address
     * @param subject the subject line of the email
     * @param text the body text of the email
     * @throws MessagingException if an error occurs while sending the email
     */
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
