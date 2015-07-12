package controllers;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.PersistenceException;

import models.Assignment;
import models.Note;
import models.Parent;
import models.SchoolClass;
import models.Student;
import models.Teacher;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.parentProfile;
import views.html.parentSignUp;
import views.html.profile;
import views.html.signup;
import views.html.studentSignUp;
import views.html.teacherProfile;

import com.matthew.hasher.Hasher;

//The controller, it controls all data flow and handles/processes requests and returns the correct information
public class Application extends Controller {

	public final static String[] OPTIONS = { "Homework", "Project", "Test",
			"Final", "Presentation", "Other" };

	private static Form<Login> loginForm = Form.form(Login.class);
	private static Form<ContactUs> contactUsForm = Form.form(ContactUs.class);

	private static final Hasher HASHER = new Hasher();

	// The number of days in today, this is set to year * 366 - (12 - month) *
	// 31 - (31 - day)
	private static int today;
	// who the email is going to
	private static final String TO = "orgnizerwebapp@gmail.com";
	// Javax.mail stuff to send email
	private static Properties mailServerProperties;
	private static Session getMailSession;
	static MimeMessage generateMailMessage;

	// Directs the request to the index
	public Result index() {
		return ok(index.render(Student.find.all().size()
				+ Parent.find.all().size() + Teacher.find.all().size(),
				loginForm));
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
	public Result FAQ() {
		return ok(views.html.faq.render());
	}

	// Directs the user to the contact us page
	public Result contactUsPage() {
		return ok(views.html.contact.render(""));
	}

	// Refreshed the profile page
	public Result refresh(Long studentID) {
		System.out.println("CALLED");
		Student student = Student.find.where().eq("ID", studentID).findUnique();
		if (student == null)
			return badRequest(index.render(Student.find.all().size()
					+ Parent.find.all().size() + Teacher.find.all().size(),
					loginForm));
		return redirect(routes.Application.profileLogin(String
				.valueOf(student.id)));
	}

	// Performs the email sending operation and then redirects back to the index
	public Result contactUs() {
		Form<ContactUs> filledForm = contactUsForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.contact
					.render("Error while processing."));
		}
		String name = filledForm.data().get("name");
		String email = filledForm.data().get("email");
		String message = filledForm.data().get("message");
		String subject = filledForm.data().get("subject");
		if (name.isEmpty() || name.trim().isEmpty())
			return badRequest(views.html.contact
					.render("Message can't be empty."));
		if (email.isEmpty() || email.trim().isEmpty())
			return badRequest(views.html.contact
					.render("Message can't be empty."));
		if (subject.isEmpty() || subject.trim().isEmpty())
			return badRequest(views.html.contact
					.render("Subject can't be empty."));
		if (message.isEmpty() || message.trim().isEmpty())
			return badRequest(views.html.contact
					.render("Message can't be empty."));
		Application.generateAndSendEmail(name, email, subject, message);
		return redirect(routes.Application.index());
	}

	// TODO need to do these
	public Result addNote(String studentID) {
		return TODO;
	}

	// Delete a late assignment
	public Result deleteLateAssignment(String assignmentID, String studentID) {
		Assignment lateAssignment = Assignment.find.ref(Long
				.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (lateAssignment == null)
			return badRequest(profile.render(student,
					Utilities.createSchoolClassesList(student),
					Utilities.createAssignmentsList(student),
					Utilities.createFinishedAssignmentsList(student),
					Utilities.createLateAssignmentsList(student),
					Utilities.createTeachersList(student),
					Utilities.createNotesList(student), today,
					"lateAssignments", "Error while processing."));
		try {
			lateAssignment.delete();
		} catch (PersistenceException e) {
			return ok(profile.render(student,
					Utilities.createSchoolClassesList(student),
					Utilities.createAssignmentsList(student),
					Utilities.createFinishedAssignmentsList(student),
					Utilities.createLateAssignmentsList(student),
					Utilities.createTeachersList(student),
					Utilities.createNotesList(student), today,
					"lateAssignments", ""));
		}
		return ok(profile.render(student,
				Utilities.createSchoolClassesList(student),
				Utilities.createAssignmentsList(student),
				Utilities.createFinishedAssignmentsList(student),
				Utilities.createLateAssignmentsList(student),
				Utilities.createTeachersList(student),
				Utilities.createNotesList(student), today, "lateAssignments",
				""));
	}

	// Delete a finished assignment
	public Result deleteFinishedAssignment(String assignmentID, String studentID) {
		Assignment finishedAssignment = Assignment.find.ref(Long
				.valueOf(assignmentID));
		Student student = Student.find.ref(Long.valueOf(studentID));
		if (finishedAssignment == null)
			return badRequest(profile.render(student,
					Utilities.createSchoolClassesList(student),
					Utilities.createAssignmentsList(student),
					Utilities.createFinishedAssignmentsList(student),
					Utilities.createLateAssignmentsList(student),
					Utilities.createTeachersList(student),
					Utilities.createNotesList(student), today,
					"finishedAssignments", "Error while processing."));
		try {
			finishedAssignment.delete();
		} catch (PersistenceException e) {
			return ok(profile.render(student,
					Utilities.createSchoolClassesList(student),
					Utilities.createAssignmentsList(student),
					Utilities.createFinishedAssignmentsList(student),
					Utilities.createLateAssignmentsList(student),
					Utilities.createTeachersList(student),
					Utilities.createNotesList(student), today,
					"finishedAssignments", ""));
		}
		return ok(profile.render(student,
				Utilities.createSchoolClassesList(student),
				Utilities.createAssignmentsList(student),
				Utilities.createFinishedAssignmentsList(student),
				Utilities.createLateAssignmentsList(student),
				Utilities.createTeachersList(student),
				Utilities.createNotesList(student), today,
				"finishedAssignments", ""));
	}

	// Authenticate a request
	public Result authenticate() {
		Form<Login> filledForm = loginForm.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(login
					.render(loginForm, "Login error, try again."));
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
			Teacher teacher = Teacher.find.where().eq("email", email)
					.findUnique();
			List<Student> students = Student.find.where().eq("email", email)
					.findList();

			// Try parent login first
			if (parent != null) {
				String password = null;
				try {
					password = HASHER.hashWithSaltSHA256(
							filledForm.data().get("password"), parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (Parent.authenticate(filledForm.data().get("email"),
						password) != null) {
					parent = Parent.find.where().eq("email", email)
							.eq("password", password).findUnique();
					return redirect(routes.Application
							.parentProfileLogin(String.valueOf(parent.id)));
				}
			}

			// Else try teacher login
			if (teacher != null) {
				String password = null;
				try {
					password = HASHER.hashWithSaltSHA256(
							filledForm.data().get("password"), teacher.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (Teacher.authenticate(filledForm.data().get("email"),
						password) != null) {
					teacher = Teacher.find.where().eq("email", email)
							.eq("password", password).findUnique();
					return redirect(routes.Application
							.teacherProfileLogin(teacher.id));
				}
			}

			// Else do student login
			String password = null;
			Student student = null;
			if (students.size() <= 0)
				return badRequest(login.render(loginForm,
						"Invalid email or password."));
			if (students.size() > 1 && Parent.exists(email)) {
				try {
					password = HASHER.hashWithSaltSHA256(
							filledForm.data().get("password"),
							students.get(0).parent.salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				student = Student.find.where().eq("email", email)
						.eq("password", password).findUnique();
			} else {
				try {
					password = HASHER.hashWithSaltSHA256(
							filledForm.data().get("password"),
							students.get(0).salt);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				student = Student.find.where().eq("email", email)
						.eq("password", password).findUnique();

			}
			if (password == null || student == null)
				return badRequest(login.render(loginForm,
						"Invalid email or password."));

			if (Student.authenticate(email, password) != null)
				return redirect(routes.Application.profileLogin(String
						.valueOf(student.id)));

			return badRequest(login.render(loginForm,
					"Invalid email or password."));
		}
	}

	// For All of the create_Lists Methods:
	// Recall that SQL uses underscores, you can't use student.id it becomes
	// student_id

	public static void generateAndSendEmail(String name, String email,
			String subject, String message) {
		// Set properties for the smtp server we are sending to
		mailServerProperties = System.getProperties();
		mailServerProperties.put("mail.smtp.port", "587");
		mailServerProperties.put("mail.smtp.auth", "true");
		mailServerProperties.put("mail.smtp.starttls.enable", "true");

		getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		generateMailMessage = new MimeMessage(getMailSession);
		try {
			// Add properties to the email
			generateMailMessage.addRecipient(Message.RecipientType.TO,
					new InternetAddress(TO));
			generateMailMessage.setFrom(new InternetAddress(email));
			generateMailMessage.setSender(new InternetAddress(email));
			generateMailMessage.setSubject(subject);

			generateMailMessage
					.setContent(
							"From: "
									+ name
									+ " | "
									+ email
									+ "<br><br><pre><p style=\"font-size:14px; font-family: 'Arial'; \">"
									+ message + "</p></pre>", "text/html");

			Transport transport = getMailSession.getTransport("smtp");

			// send it by the orgnizerwebapp email
			transport.connect("smtp.gmail.com", "orgnizerwebapp",
					"GETHIP@Gallup!");
			transport.sendMessage(generateMailMessage,
					generateMailMessage.getAllRecipients());
			transport.close();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
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
