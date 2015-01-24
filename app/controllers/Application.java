package controllers;

import play.*;
import play.mvc.*;
import models.*;
import play.data.*;
import views.html.*;
import  play.mvc.Http.Session;
import  play.mvc.Http;
import static play.data.Form.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;

public class Application extends Controller {

	private static Form<Student> studentForm = Form.form(Student.class);
	private static Form<SchoolClass> schoolClassForm = Form.form(SchoolClass.class);
	private static Form<Homework> homeworkForm = Form.form(Homework.class);
	private static Form<Test> testForm = Form.form(Test.class);
	private static Form<Note> noteForm = Form.form(Note.class);
	private static Form<Teacher> teacherForm = Form.form(Teacher.class);
	private static Form<Project> projectForm = Form.form(Project.class);
	
	private static Student student;
	private static List<Homework> homeworks;
	private static List<SchoolClass> schoolClasses;
	private static List<Teacher> teachers;
	private static List<Test> tests;
	private static List<Note> notes;
	private static List<Project> projects;
	private static List<OverviewObject> overview;
	private static List<OverviewObject> passed;
	
	static Session session = Http.Context.current().session();

    public static Result index() {
		return ok(index.render(Student.find.all(), Form.form(Login.class)));
    }
	
	public static Result blog() {
		return ok(blog.render(Student.find.all(), Form.form(Login.class)));
    }
	
	public static Result signup() {
		return ok(signup.render(studentForm));
	}

	public static Result login() {
		return ok(login.render(Form.form(Login.class)));
	}

	public static Result logout() {
		session.clear();
		return redirect(routes.Application.index());
	}

	public static Result students() {
		return ok(students.render(Student.find.all()));
	}
	
	public static Result editPageHomework(String id) {
		Homework homework = Homework.find.ref(Long.valueOf(id));
		return ok(homeworkEdit.render(student, homework, schoolClasses));
	}
	
	public static Result editPageTest(String id) {
		Test test = Test.find.ref(Long.valueOf(id));
		return ok(testEdit.render(student, test, schoolClasses));
	}
	
	public static Result editPageProject(String id) {
		Project project = Project.find.ref(Long.valueOf(id));
		return ok(projectEdit.render(student, project, schoolClasses));
	}
	
	public static Result editOverviewObject(String id, String spanner) {
		switch(spanner) {
		case "H":
			return editPageHomework(id);
		case "T":
			return editPageTest(id);
		case "P":
			return editPageProject(id);
		}
		return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
	}
	
	public static Result newStudent() {	
		Form<Student> filledForm = studentForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(index.render(Student.find.all(), Form.form(Login.class)));
		} else {
			Student.create(filledForm.data().get("name"), filledForm.data().get("email"), filledForm.data().get("password"), filledForm.data().get("grade"));
			return ok(index.render(Student.find.all(), Form.form(Login.class)));
		}
	}
	
	public static Result newSchoolClass() {
		Form<SchoolClass> filledForm = schoolClassForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		} else {		
			SchoolClass newSchoolClass = SchoolClass.create(filledForm.data().get("subject"), filledForm.data().get("studentEmail"));
			schoolClasses.add(newSchoolClass);
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		}	
	}
	
	public static String getSubject(Homework homework) {
		if(homework == null) return ""; 
		try {
			if(homework.schoolClass.subject != null) {
				return homework.schoolClass.subject;
			}
		} catch(Exception e) {
			return "";
		}
		return "";
	}
	
	public static Result newHomework() {
		Form<Homework> filledForm = homeworkForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		} else {
			Homework newHomework = Homework.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("description"));
			homeworks.add(newHomework);
			OverviewObject overviewObject = OverviewObject.create(newHomework);
			overview.add(overviewObject);
			if(homeworks.size() > 0) sortHomeworkListRecursively(0, homeworks.get(homeworks.size() - 1));
			if(overview.size() > 0) sortOverviewListRecursively(0, overview.get(overview.size() - 1));
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		}
	}
	
	public static Result newTest() {
		Form<Test> filledForm = testForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			System.out.println("HAD ERRORS");
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		} else {
			System.out.println("DID NOT HAVE ERRORS");
			Test newTest = Test.create(filledForm.data().get("dateOf"), filledForm.data().get("schoolClassId"), filledForm.data().get("description"));
			tests.add(newTest);
			OverviewObject overviewObject = OverviewObject.create(newTest);
			overview.add(overviewObject);
			if(tests.size() > 0) sortTestListRecursively(0, tests.get(tests.size() - 1));
			if(overview.size() > 0) sortOverviewListRecursively(0, overview.get(overview.size() - 1));
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		}
	}
	
	public static Result newProject() {
		Form<Project> filledForm = projectForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			System.out.println("HAD ERRORS");
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		} else {
			System.out.println("DID NOT HAVE ERRORS");
			Project newProject = Project.create(filledForm.data().get("dueDate"), filledForm.data().get("schoolClassId"), filledForm.data().get("description"));
			projects.add(newProject);
			OverviewObject overviewObject = OverviewObject.create(newProject);
			overview.add(overviewObject);
			if(projects.size() > 0) sortProjectListRecursively(0, projects.get(projects.size() - 1));
			if(overview.size() > 0) sortOverviewListRecursively(0, overview.get(overview.size() - 1));
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		}
	}
	
	public static Result editHomework(String id) {
		Form<Homework> filledForm = homeworkForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		} else {
			Homework.edit(Long.parseLong(id), SchoolClass.find.ref(Long.parseLong(filledForm.data().get("schoolClassId"))), filledForm.data().get("dueDate"), filledForm.data().get("description"));
			createHomeworkList();
			createOverviewList();
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		}
	}
	
	public static Result editTest(String id) {
		Form<Test> filledForm = testForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		} else {
			Test.edit(Long.parseLong(id), SchoolClass.find.ref(Long.parseLong(filledForm.data().get("schoolClassId"))), filledForm.data().get("dateOf"), filledForm.data().get("description"));
			createTestList();
			createOverviewList();
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		}
	}
	
	public static Result editProject(String id) {
		Form<Project> filledForm = projectForm.bindFromRequest();
		if(filledForm.hasErrors()) {
			return badRequest(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		} else {
			Project.edit(Long.parseLong(id), SchoolClass.find.ref(Long.parseLong(filledForm.data().get("schoolClassId"))), filledForm.data().get("dueDate"), filledForm.data().get("description"));
			createProjectList();
			createOverviewList();
			return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
		}
	}
	
	public static Result deleteHomework(String id) {
		Homework.find.ref(Long.valueOf(id)).delete();
		createHomeworkList();
		createOverviewList();
		return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
	}
	
	public static Result deleteTest(String id) {
		Test.find.ref(Long.valueOf(id)).delete();
		createTestList();
		createOverviewList();
		return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
	}
	
	public static Result deleteProject(String id) {
		Project.find.ref(Long.valueOf(id)).delete();
		createProjectList();
		createOverviewList();
		return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
	}
	
	public static Result deleteOverviewObject(String id, String spanner) {
		switch(spanner) {
		case "H":
			Homework.find.ref(Long.valueOf(id)).delete();
			createHomeworkList();
			break;
		case "T":
			Test.find.ref(Long.valueOf(id)).delete();
			createTestList();
			break;
		case "P":
			Project.find.ref(Long.valueOf(id)).delete();
			createProjectList();
			break;
		}
		createOverviewList();
		return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
	}
	
	public static Result deletePassedObject(String id, String spanner) {
		switch(spanner) {
		case "H":
			Homework.find.ref(Long.valueOf(id)).delete();
			createHomeworkList();
			break;
		case "T":
			Test.find.ref(Long.valueOf(id)).delete();
			createTestList();
			break;
		case "P":
			Project.find.ref(Long.valueOf(id)).delete();
			createProjectList();
			break;
		}
		createPassedList();
		return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));		
	}	
	
	public static void sortHomeworkListRecursively(int index, Homework homework) {
		if(homework.total > homeworks.get(index).total) {
			sortHomeworkListRecursively(index + 1, homework);
			return;
		} else {
			for(int u = homeworks.size() - 2; u >= index; u--) {
				homeworks.set(u + 1, homeworks.get(u));
			}
			homeworks.set(index, homework);
		}
	}
	
	public static void sortTestListRecursively(int index, Test test) {
		if(test.total > tests.get(index).total) {
			sortTestListRecursively(index + 1, test);
			return;
		} else {
			for(int u = tests.size() - 2; u >= index; u--) {
				tests.set(u + 1, tests.get(u));
			}
			tests.set(index, test);
		}
	}
	
	public static void sortProjectListRecursively(int index, Project project) {
		if(project.total > projects.get(index).total) {
			sortProjectListRecursively(index + 1, project);
			return;
		} else {
			for(int u = projects.size() - 2; u >= index; u--) {
				projects.set(u + 1, projects.get(u));
			}
			projects.set(index, project);
		}
	}
	
	public static void sortOverviewListRecursively(int index, OverviewObject overviewObject) {
		if(overviewObject.total > overview.get(index).total) {
			sortOverviewListRecursively(index + 1, overviewObject);
			return;
		} else {
			for(int u = overview.size() - 2; u >= index; u--) {
				overview.set(u + 1, overview.get(u));
			}
			overview.set(index, overviewObject);
		}
	}
	
	public static void sortPassedListRecursively(int index, OverviewObject overviewObject) {
		if(overviewObject.total > passed.get(index).total) {
			sortPassedListRecursively(index + 1, overviewObject);
			return;
		} else {
			for(int u = passed.size() - 2; u >= index; u--) {
				passed.set(u + 1, passed.get(u));
			}
			passed.set(index, overviewObject);
		}
	}
	
	public static void sortOverviewListFully() {
		Object[] overviewArray = overview.toArray();
		overview.clear();
		for(int i = 0; i < overviewArray.length; i++) {
			overview.add((OverviewObject) overviewArray[i]);
			sortOverviewListRecursively(0, overview.get(overview.size() - 1));
		}
	}
	
	public static void sortHomeworkListFully() {
		Object[] homeworkArray = homeworks.toArray();
		homeworks.clear();
		for(int i = 0; i < homeworkArray.length; i++) {
			homeworks.add((Homework) homeworkArray[i]);
			sortHomeworkListRecursively(0, homeworks.get(homeworks.size() - 1));
		}
	}
	
	public static void sortTestListFully() {
		Object[] testArray = tests.toArray();
		tests.clear();
		for(int i = 0; i < testArray.length; i++) {
			tests.add((Test) testArray[i]);
			sortTestListRecursively(0, tests.get(tests.size() - 1));
		}
	}
	
	public static void sortProjectListFully() {
		Object[] projectArray = projects.toArray();
		projects.clear();
		for(int i = 0; i < projectArray.length; i++) {
			projects.add((Project) projectArray[i]);
			sortProjectListRecursively(0, projects.get(projects.size() - 1));
		}
	}
	
	public static void sortPassedListFully() {
		Object[] passedArray = passed.toArray();
		passed.clear();
		for(int i = 0; i < passedArray.length; i++) {
			passed.add((OverviewObject) passedArray[i]);
			sortPassedListRecursively(0, passed.get(passed.size() - 1));
		}
	}
	
	public static void checkVeracityOfSchoolClassList() {
		for(int i = 0; i < schoolClasses.size(); i++) {
			if(!schoolClasses.get(i).student.email.equals(student.email)) {
				schoolClasses.remove(i);
				i--;
			}
		}
	}
	
	public static void checkVeracityOfHomeworkList() {
		for(int i = 0; i < homeworks.size(); i++) {
			if(!homeworks.get(i).schoolClass.student.email.equals(student.email)) {
				homeworks.remove(i);
				i--;
			}
		}
	}
	
	public static void checkVeracityOfTeachersList() {
		for(int i = 0; i < teachers.size(); i++) {
			if(!teachers.get(i).schoolClass.student.email.equals(student.email)) {
				teachers.remove(i);
				i--;
			}
		}
	}
	
	public static void checkVeracityOfTestsList() {
		for(int i = 0; i < tests.size(); i++) {
			if(!tests.get(i).schoolClass.student.email.equals(student.email)) {
				tests.remove(i);
				i--;
			}
		}
	}
	
	public static void checkVeracityOfNotesList() {
		for(int i = 0; i < notes.size(); i++) {
			if(!notes.get(i).student.email.equals(student.email)) {
				notes.remove(i);
				i--;
			}
		}
	}
	
	public static void checkVeracityOfProjectsList() {
		for(int i = 0; i < projects.size(); i++) {
			if(!projects.get(i).schoolClass.student.email.equals(student.email)) {
				projects.remove(i);
				i--;
			}
		}
	}
	
	public static void createHomeworkList() {
		homeworks = Homework.find.findList();
		checkVeracityOfHomeworkList();
		sortHomeworkListFully();
	}
	
	public static void createTestList() {
		tests = Test.find.findList();
		checkVeracityOfTestsList();
		sortTestListFully();
	}
	
	public static void createProjectList() {
		projects = Project.find.findList();
		checkVeracityOfProjectsList();
		sortProjectListFully();
	}
	
	public static void createSchoolClassList() {
		schoolClasses = SchoolClass.find.findList();
		checkVeracityOfSchoolClassList();
	}
	
	public static void createTeacherList() {
		teachers = Teacher.find.findList();
		checkVeracityOfTeachersList();
	}
	
	public static void createNotesList() {
		notes = Note.find.findList();
		checkVeracityOfNotesList();
	}
	
	public static void createOverviewList() {
		overview = new ArrayList<OverviewObject>();
		for(int i = 0; i < homeworks.size(); i++) {
			OverviewObject overviewObject = new OverviewObject(homeworks.get(i));
			overview.add(overviewObject);
		}
		for(int i = 0; i < tests.size(); i++) {
			OverviewObject overviewObject = new OverviewObject(tests.get(i));
			overview.add(overviewObject);
		}
		for(int i = 0; i < projects.size(); i++) {
			OverviewObject overviewObject = new OverviewObject(projects.get(i));
			overview.add(overviewObject);
		}
		sortOverviewListFully();
	}
	
	public static void createPassedList() {
		passed = new ArrayList<OverviewObject>();
		int day = Calendar.getInstance().get(Calendar.DATE);
		int month = Calendar.getInstance().get(Calendar.MONTH)+1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int total = (year * 366) - ((12 - month) * 31) - (31 - day);
		for(int i = 0; i < homeworks.size(); i++) {
			if(homeworks.get(i).total < total) {
				passed.add(new OverviewObject(homeworks.get(i)));
				homeworks.remove(i);
				i--;
			}
		}
		for(int i = 0; i < tests.size(); i++) {
			if(tests.get(i).total < total) {
				passed.add(new OverviewObject(tests.get(i)));
				tests.remove(i);
				i--;
			}
		}
		for(int i = 0; i < projects.size(); i++) {
			if(projects.get(i).total < total) {
				passed.add(new OverviewObject(projects.get(i)));
				projects.remove(i);
				i--;
			}
		}
		sortPassedListFully();
	}
	
	public static void createLists() {	
		student = Student.find.ref(session.get("email"));		
		createSchoolClassList();
		createHomeworkList();
		createTestList();
		createProjectList();
		createTeacherList();
		createNotesList();
		createPassedList();
		createOverviewList();
	}
	
	
	public static Result profileLogin() {
		createLists();
		
		return ok(profile.render(student, homeworks, schoolClasses, teachers, tests, notes, projects, overview, passed));
	}
						
	public static Result authenticate() {
		Form<Login> loginForm = null;
		try {
			loginForm = Form.form(Login.class).bindFromRequest();
		} catch(Exception e) {
			return null;
		}
		if(loginForm.hasErrors()) {
			return badRequest(index.render(Student.find.all(), Form.form(Login.class)));
		} else {
			session.clear();
			session.put("email", loginForm.get().email);
			return redirect(routes.Application.profileLogin());
		}
	}

	public static class Login {
		public String email;
		public String password;
		
		public String validate() {
			if(Student.authenticate(email, password) == null) {
				return "Invalid email or password";
			} 
			System.err.println("Authenticated");
			return null;
		}
	}
	
}
