package com.moac.android.opensecretsanta.mail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;
import com.sun.mail.util.BASE64EncoderStream;

/**
 * From http://stackoverflow.com/questions/12503303/javamail-api-in-android-using-xoauth
 */
public class GmailOAuth2Sender {

    private static final String TAG = GmailOAuth2Sender.class.getSimpleName();

    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
    public static final String[] FEATURES_MAIL = { "service_mail" };
    public static final String GMAIL_TOKEN_TYPE =  "oauth2:https://mail.google.com/";

    private Session session;

    private SMTPTransport connectToSmtp(String host, int port, String userEmail,
                                        String oauthToken, boolean debug) throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.sasl.enable", "false");
        props.put("mail.smtp.ssl.enable", true);
        session = Session.getInstance(props);
        session.setDebug(debug);
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

    public synchronized void sendMail(String subject, String body, String user,
                                      String oauthToken, String recipients) throws MessagingException {

        SMTPTransport smtpTransport = connectToSmtp("smtp.gmail.com",
          587,
          user,
          oauthToken,
          true);

        MimeMessage message = new MimeMessage(session);
        message.setSender(new InternetAddress(user));
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");

        try {
            if(recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            smtpTransport.sendMessage(message, message.getAllRecipients());
        } finally {
            smtpTransport.close();
        }
    }
}
