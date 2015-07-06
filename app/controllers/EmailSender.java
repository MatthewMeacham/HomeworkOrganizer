package controllers;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

//Class that actually sends the email
public class EmailSender {

	// who the email is going to
	private static final String TO = "orgnizerwebapp@gmail.com";

	// Javax.mail stuff to send email
	private static Properties mailServerProperties;
	private static Session getMailSession;
	private static MimeMessage generateMailMessage;

	public static void generateAndSendEmail(String name, String email, String subject, String message) {
		// Set properties for the smtp server we are sending to
		mailServerProperties = System.getProperties();
		mailServerProperties.put("mail.smtp.port", "587");
		mailServerProperties.put("mail.smtp.auth", "true");
		mailServerProperties.put("mail.smtp.starttls.enable", "true");

		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession);
		try {
			// Add properties to the email
			generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
			generateMailMessage.setFrom(new InternetAddress(email));
			generateMailMessage.setSender(new InternetAddress(email));
			generateMailMessage.setSubject(subject);
			generateMailMessage.setContent("From: " + name + " | " + email + "<br><br><pre>" + message + "</pre>", "text/html");

			Transport transport = getMailSession.getTransport("smtp");

			// send it by the orgnizerwebapp email
			transport.connect("smtp.gmail.com", "orgnizerwebapp", "GETHIP@Gallup!");
			transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
			transport.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}
