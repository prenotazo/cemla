package com.prenotaOnLine.message;

import java.security.Security;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

public class GoogleMessage {

	private GoogleMessage() {

	}

	public static void sendEmail(final String title, final String message) throws Exception {
		sendMessage("ricardo.castiglione@gmail.com", title, message);
	}

	public static void sendSms(final String title, final String message) throws Exception {
		sendMessage("1158340144@sms.movistar.net.ar", title, message);
	}

	@SuppressWarnings("restriction")
	private static void sendMessage(final String recipientEmail, final String title, final String message) throws Exception {
		final String username = "brainfields";
		final String password = "grupo403";

		Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
		final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

		// Get a Properties object
		final Properties props = System.getProperties();
		props.setProperty("mail.smtps.host", "smtp.gmail.com");
		props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
		props.setProperty("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.port", "465");
		props.setProperty("mail.smtp.socketFactory.port", "465");
		props.setProperty("mail.smtps.auth", "true");

		/*
        If set to false, the QUIT command is sent and the connection is immediately closed. If set
        to true (the default), causes the transport to wait for the response to the QUIT command.

        ref :   http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
                http://forum.java.sun.com/thread.jspa?threadID=5205249
                smtpsend.java - demo program from javamail
		 */
		props.put("mail.smtps.quitwait", "false");

		final Session session = Session.getInstance(props, null);

		// -- Create a new message --
		final MimeMessage msg = new MimeMessage(session);

		// -- Set the FROM and TO fields --
		msg.setFrom(new InternetAddress(username + "@gmail.com"));
		msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail, false));

		msg.setSubject(title);
		msg.setText(message, "utf-8");
		msg.setSentDate(new Date());

		final SMTPTransport t = (SMTPTransport)session.getTransport("smtps");

		t.connect("smtp.gmail.com", username, password);
		t.sendMessage(msg, msg.getAllRecipients());
		t.close();
	}

	public static void main(final String[] args) {
		try {
			GoogleMessage.sendEmail("titleEmail", "messageEmail");
			GoogleMessage.sendSms("titleSms", "messageSms");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}