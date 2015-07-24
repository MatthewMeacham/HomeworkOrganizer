package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.lang.Thread;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

import models.Parent;
import models.Student;
import models.Teacher;

import views.html.contact;
import views.html.faq;
import views.html.index;
import views.html.login;
import views.html.privacyPolicy;
import views.html.termsAndConditions;

import com.matthew.hasher.Hasher;

//The controller, it controls all data flow and handles/processes requests and returns the correct information
public class Application extends Controller {

	public final static String[] OPTIONS = { "Homework", "Project", "Test", "Final", "Presentation", "Other" };

	private static Form<Login> loginForm = Form.form(Login.class);
	private static Form<ContactUs> contactUsForm = Form.form(ContactUs.class);

	// ########################################################################

	private static final Hasher HASHER = new Hasher();

	// ########################################################################

	// who the email is going to
	private static final String TO = "orgnizerwebapp@gmail.com";

	// Javax.mail stuff to send email
	private static Properties mailServerProperties;
	private static Session session;
	private static MimeMessage mailMessage;
	
	private volatile static ArrayList<String[]> emailQueue = new ArrayList<String[]>();
	
	//This is how long in Minutes that the email thread should wait before performing operations again
	private static final int WAIT_TIME = 5;
	
	private static Thread emailSendingThread = new Thread(new Runnable() {
		public void run() {			
			// Set properties for the smtp server we are sending to
			mailServerProperties = System.getProperties();
			mailServerProperties.put("mail.smtp.port", "587");
			mailServerProperties.put("mail.smtp.auth", "true");
			mailServerProperties.put("mail.smtp.starttls.enable", "true");

			session = Session.getDefaultInstance(mailServerProperties, null);
			while(true) sendEmails();
		}
	});

	// ########################################################################

	// Directs the request to the index
	public Result index() {
		return ok(index.render(Student.find.all().size() + Parent.find.all().size() + Teacher.find.all().size(), loginForm));
	}

	// Directs the request to the login page
	public Result login() {
		Utilities.setToday();
		return ok(login.render(loginForm, ""));
	}

	// Redirects the request to the index for logging out
	public Result logout() {
		return redirect(routes.Application.index());
	}

	// Directs the user to the FAQ page
	public Result faq() {
		return ok(faq.render());
	}

	// Directs the user to the contact us page
	public Result contactUsPage() {
		return ok(contact.render(""));
	}
	
	//Directs the user to the privacy policy page
	public Result privacyPolicy() {
		return ok(privacyPolicy.render());
	}
	
	//Directs the user to the terms and conditions page
	public Result termsAndConditions() {
		return ok(termsAndConditions.render());
	}

	// Performs the email sending operation and then redirects back to the index
	public Result contactUs() {
		Form<ContactUs> filledForm = contactUsForm.bindFromRequest();
		if (filledForm.hasErrors()) return badRequest(contact.render("Error while processing."));
		String name = filledForm.data().get("name");
		String email = filledForm.data().get("email");
		String message = filledForm.data().get("message");
		String subject = filledForm.data().get("subject");
		if (name.isEmpty() || name.trim().isEmpty()) return badRequest(contact.render("Name can't be empty."));
		if (email.isEmpty() || email.trim().isEmpty()) return badRequest(contact.render("Email can't be empty."));
		if (!email.contains("@")) return badRequest(contact.render("Invalid email address."));
		if (subject.isEmpty() || subject.trim().isEmpty()) return badRequest(contact.render("Subject can't be empty."));
		if (message.isEmpty() || message.trim().isEmpty()) return badRequest(contact.render("Message can't be empty."));
		String[] strings = {name, email, subject, message};
		emailQueue.add(strings);
		try {
			if(!emailSendingThread.isAlive()) emailSendingThread.start();
		} catch (IllegalStateException e) {
			//Ignore this exception, this is thrown when the emailSendingThread hasn't been started before and therefore has no state
			//(a null state), which means we can't perform boolean operators on it, so it will throw this exception to tell us that
		}
		return redirect(routes.Application.index());
	}

	// Authenticate a request
	public Result authenticate() {
		Form<Login> filledForm = loginForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(login.render(loginForm, "Login error, try again."));
		} else {
			// if
			// (Parent.authenticate(filledForm.data().get("email").toLowerCase(),
			// filledForm.data().get("password")) == null &&
			// Student.authenticate(filledForm.data().get("email").toLowerCase(),
			// filledForm.data().get("password")) == null) {
			// return badRequest(login.render(loginForm,
			// "Invalid email or password."));
			// }

			String email = filledForm.data().get("email").toLowerCase();
			Parent parent = Parent.find.where().eq("email", email).findUnique();
			Teacher teacher = Teacher.find.where().eq("email", email).findUnique();
			List<Student> students = Student.find.where().eq("email", email).findList();

			// Try parent login first
			if (parent != null) {
				String password = null;
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (Parent.authenticate(filledForm.data().get("email"), password) != null) {
					parent = Parent.find.where().eq("email", email).eq("password", password).findUnique();
					return redirect(routes.Parents.toProfile(parent.id.toString()));
				}
			}

			// Else try teacher login
			if (teacher != null) {
				String password = null;
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), teacher.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (Teacher.authenticate(filledForm.data().get("email"), password) != null) {
					teacher = Teacher.find.where().eq("email", email).eq("password", password).findUnique();
					return redirect(routes.Teachers.toProfile(teacher.id));
				}
			}

			// Else do student login
			String password = null;
			Student student = null;
			if (students.size() <= 0) return badRequest(login.render(loginForm, "Invalid email or password."));
			if (students.size() > 1 && Parent.exists(email)) {
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			} else {
				try {
					password = HASHER.hashWithSaltSHA256(filledForm.data().get("password"), students.get(0).salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			student = Student.find.where().eq("email", email).eq("password", password).findUnique();

			if (password == null || student == null) return badRequest(login.render(loginForm, "Invalid email or password."));
			
			if (Student.authenticate(email, password) != null) return redirect(routes.Students.toProfile(student.id.toString()));

			return badRequest(login.render(loginForm, "Invalid email or password."));
		}
	}

	//This method will generate and send the email
	private static boolean generateAndSendEmail(String name, String email, String subject, String message) {
		mailMessage = new MimeMessage(session);
		try {
			// Add properties to the email
			mailMessage.setFrom(new InternetAddress(email));
			mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(TO));
			mailMessage.setSubject(subject);

			mailMessage.setContent("From: " + name + " | " + email + "<br><br><pre><p style=\"font-size:14px; font-family: 'Arial'; \">" + message + "</p></pre>", "text/html");

			Transport transport = session.getTransport("smtp");

			// send it by the orgnizerwebapp email
			transport.connect("smtp.gmail.com", System.getenv("OrgnizerEmailUsername"), System.getenv("OrgnizerEmailPassword"));
			transport.sendMessage(mailMessage, mailMessage.getAllRecipients());
			transport.close();
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings("static-access")
	//This method is used by the emailSendingThread, this thread here will scan through all the emails in the emailQueue
	//and if there are any duplicates (which is possible if a user hits the send button multiple times), this will filter those out
	//Then it will send all the good emails, and sleep for a WAIT_TIME minutes total to keep processing usage down
	public static void sendEmails() {
		//Sleep first for 10 seconds to make sure that all the requests from the user hitting the send button multiple times have been processed
		try {
			emailSendingThread.sleep(10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < emailQueue.size(); i++) {
			for(int j = emailQueue.size() - 1; j >= 0; j--) {
				if(i == j) continue;
				if(compareEmail(emailQueue.get(i), emailQueue.get(j))){
					emailQueue.remove(j);
					if(i > j) i--;
				}
			}
		}
		for(int i = emailQueue.size() - 1; i >= 0; i--) {
			String[] strings = emailQueue.get(i);
			if(generateAndSendEmail(strings[0], strings[1], strings[2], strings[3])) emailQueue.remove(i);
		}
		//Finish out sleeping for WAIT_TIME minutes to keep the processing usage down
		try {
			emailSendingThread.sleep((WAIT_TIME * 60 - 10) * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	//This method compares two emails and returns if they are the same
	public static boolean compareEmail(String[] first, String[] second) {
		if(first[0].equals(second[0]) && first[1].equals(second[1]) && first[2].equals(second[2]) && first[3].equals(second[3])) return true;
		return false;
	}
	
	// Login class that the loginForm forms to for logging in
	public static class Login {
		public String email;
		public String password;

		public String validate() {
			// if (Student.authenticate(email, password) == null &&
			// Parent.authenticate(email, password) == null) {
			// return "Invalid email or password";
			// }
			return null;
		}
	}

	// Account settings class that the accountSettingsForm forms to for changing
	// account settings
	public static class AccountSettings {
		public String email;
		public String currentPassword;
		public String newPassword;
		public String newPasswordAgain;
		public String name;
		public String grade;
	}

	// Contact us class that the contactUsForm forms to for sending an email
	public static class ContactUs {
		public String name;
		public String email;
		public String subject;
		public String message;
	}

	// SchoolClassFromCode class that the schoolClassFromCodeForm forms to for
	// adding a class with a teacher provided id and password
	public static class SchoolClassFromCode {
		public String id;
		public String password;
	}

}
