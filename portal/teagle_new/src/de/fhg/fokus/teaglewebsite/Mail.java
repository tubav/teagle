package de.fhg.fokus.teaglewebsite;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mail {
	
	public void sendMail(String from, String[] recipients, String subject, String content) {
		try {
			java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
			
			//Set the host smtp address
			Properties props = new Properties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", "smtp.eurescom.eu");
			props.put("mail.smtp.auth", "true");
		
			Authenticator auth = new SMTPAuthenticator();
			Session mail_session = Session.getInstance(props, auth);
		
			// create a message
			Message msg = new MimeMessage(mail_session);
		
			// set the from and to addresses
			InternetAddress addressFrom = new InternetAddress(from);
			msg.setFrom(addressFrom);
			
			InternetAddress[] addressTo = new InternetAddress[recipients.length]; 
		    for (int i = 0; i < recipients.length; i++){
		        addressTo[i] = new InternetAddress(recipients[i]);
		    }
		    msg.addRecipients(Message.RecipientType.TO, addressTo);
		
			// Setting the Subject and Content Type
			msg.setSubject(subject);
			msg.setContent(content, "text/plain");
			Transport.send(msg);
			
		}
		catch (AddressException e) {
			e.printStackTrace();
		}
		catch (MessagingException e) {
			e.printStackTrace();
		}
	}
	
	private class SMTPAuthenticator extends javax.mail.Authenticator {
		 
		public PasswordAuthentication getPasswordAuthentication() {
			String username = "panlab_teagle";
			String password = "weasel";
			return new PasswordAuthentication(username, password);
		}
	}

}
