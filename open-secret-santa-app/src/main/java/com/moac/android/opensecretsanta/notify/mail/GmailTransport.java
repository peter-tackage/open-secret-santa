package com.moac.android.opensecretsanta.notify.mail;

import com.moac.android.opensecretsanta.notify.NotificationFailureException;
import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * From http://stackoverflow.com/questions/12503303/javamail-api-in-android-using-xoauth
 */
public class GmailTransport implements EmailTransporter {

    private static final String TAG = GmailTransport.class.getSimpleName();

    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    public static final String[] FEATURES_MAIL = { "service_mail" };
    public static final String GMAIL_TOKEN_TYPE = "oauth2:https://mail.google.com/";

    @Override
    public synchronized void send(String subject, String body, String senderAddress,
                                  String toAddress, String oauthToken) throws NotificationFailureException {

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "false");
        props.put("mail.smtp.ssl.enable", true);
        Session session = Session.getInstance(props);
        session.setDebug(true);

        SMTPTransport smtpTransport = null;
        try {
            smtpTransport = connectToSmtp(session, "smtp.gmail.com",
          587,
          senderAddress,
          oauthToken);

        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(senderAddress));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");

            if(toAddress.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            smtpTransport.sendMessage(message, message.getAllRecipients());
        } catch (MessagingException mex) {
            throw new NotificationFailureException("Failed to send Gmail email from: " + senderAddress, mex);
        }
        finally {
            if(smtpTransport !=null){
                try {
                    smtpTransport.close();
                } catch (MessagingException e) {
                    throw new NotificationFailureException("Failed to send Gmail email from: " + senderAddress, e);
                }
            }
        }
    }

    private SMTPTransport connectToSmtp(Session session, String host, int port, String userEmail,
                                        String oauthToken) throws MessagingException {

        final URLName unusedUrlName = null;
        SMTPTransport transport = new SMTPTransport(session, unusedUrlName);

        // If the password is non-null, SMTP tries to do AUTH LOGIN.
        final String emptyPassword = null;
        transport.connect(host, port, userEmail, emptyPassword);

        byte[] response = String.format("user=%s\1auth=Bearer %s\1\1", userEmail,
                oauthToken).getBytes();
        response = BASE64EncoderStream.encode(response);

        transport.issueCommand("AUTH XOAUTH2 " + new String(response),
                235);

        return transport;
    }

}
