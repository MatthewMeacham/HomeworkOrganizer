package controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import models.Assignment;
import models.Note;
import models.Parent;
import models.SchoolClass;
import models.Student;
import models.Teacher;
import play.mvc.Controller;

public class Utilities extends Controller {

	// The number of days in today
	public static int today;
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");

	// Checks to see if the student account with the given studentID is a child
	// account meaning it was parent created
	public static boolean childAccount(String studentID) {
		List<Parent> parents = Parent.find.all();
		Student student = Student.find.where().eq("ID", UUID.fromString(studentID)).findUnique();
		for (Parent parent : parents) {
			if (student.email.equals(parent.email)) return true;
		}
		return false;
	}

	// Sets the variable today, the number of days in the date since 0AD
	public static void setToday() {
		int day = Calendar.getInstance().get(Calendar.DATE);
		// have to add one because 0 is January
		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
		int year = Calendar.getInstance().get(Calendar.YEAR);
		int total = calculateTotal(day, month, year);
		today = total;
	}
	
	public static String getMonthString(int month) {
		if(month < 10) return "0" + month;
		return "" + month;
	}
	
	public static String getDayString(int day) {
		if(day < 10) return "0" + day;
		return "" + day;
	}
	
	public static int calculateTotal(int day, int month, int year) {
		String dateString = getDayString(day) + " " + getMonthString(month) + " " + year;
		try {
			Date date = dateFormat.parse(dateString);
			long time = date.getTime();
			return (int) TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS);
		} catch (ParseException e) {
			return 0;
		}
	}


	// For All of the create_Lists Methods:
	// Recall that SQL uses underscores, you can't use student.id it becomes
	// student_id

	// Creates the school classes list for the given student
	// TODO FIND BETTER WAY TO DO THIS
	public static List<SchoolClass> createSchoolClassesList(Student student) {
		List<SchoolClass> schoolClasses = SchoolClass.find.all();
		List<SchoolClass> returnSchoolClasses = new ArrayList<SchoolClass>();

		for (int i = 0; i < schoolClasses.size(); i++) {
			if (schoolClasses.get(i).students == null || schoolClasses.get(i).students.size() <= 0) continue;
			for (int j = 0; j < schoolClasses.get(i).students.size(); j++) {
				if (schoolClasses.get(i).students.get(j).id.equals(student.id)) {
					returnSchoolClasses.add(schoolClasses.get(i));
					break;
				}
			}
		}

		return returnSchoolClasses;
	}

	// Creates the assignments list for the student
	// TODO FIND A BETTER WAY TO DO THIS
	// TODO TRIPLE NESTED FOR LOOP IS SUCH A BAAADDDD IDEA
	public static List<Assignment> createAssignmentsList(Student student) {
		setToday();

		List<Assignment> assignments = new ArrayList<Assignment>();
		List<SchoolClass> schoolClasses = createSchoolClassesList(student);

		assignments.addAll(Assignment.find.where().eq("FOREIGN_ID", student.id).findList());

		for (int i = 0; i < schoolClasses.size(); i++) {
			List<Assignment> schoolClassAssignments = Assignment.find.where().eq("SCHOOL_CLASS_ID", schoolClasses.get(i).id).findList();
			for (int j = schoolClassAssignments.size() - 1; j >= 0; j--) {
				boolean same = false;
				for (int k = 0; k < assignments.size(); k++) {
					if (Assignment.same(assignments.get(k), schoolClassAssignments.get(j))) {
						same = true;
						break;
					}
				}
				if(!same) assignments.add(Assignment.create(schoolClassAssignments.get(j), student.id));
			}
		}
		
		for(int i = assignments.size() - 1; i >= 0; i--) {
			if(assignments.get(i).finished) assignments.remove(i);
		}

		return sortList(assignments);
	}

	// Creates the finished assignments list for the student
	public static List<Assignment> createFinishedAssignmentsList(Student student) {
		setToday();
		List<Assignment> finishedAssignments = Assignment.find.where().eq("FOREIGN_ID", student.id).eq("FINISHED", true).findList();
		return sortList(finishedAssignments);
	}

	// Creates the late assignments list for the given student
	public static List<Assignment> createLateAssignmentsList(Student student) {
		setToday();
		List<Assignment> lateAssignments = Assignment.find.where().eq("FOREIGN_ID", student.id).eq("FINISHED", false).findList();

		for (int i = 0; i < lateAssignments.size(); i++) {
			if (lateAssignments.get(i).total >= today) {
				lateAssignments.remove(i);
				i--;
			}
		}
		return sortList(lateAssignments);
	}

	// TODO FIND A BETTER WAY TO DO THIS CREATION
	public static List<Teacher> createTeachersList(Student student) {
		List<Teacher> teachers = new ArrayList<Teacher>();
		// teachers = Teacher.find.all();
		// // teachers = Teacher.find.where().eq("student.id",
		// // student.id).findList();
		// for (int i = teachers.size() - 1; i >= 0; i--) {
		// if (!student.teacher.email.equals(teachers.get(i).email))
		// teachers.remove(i);
		// }
		return teachers;
	}

	// Create the notes list for the given student
	public static List<Note> createNotesList(Student student) {
		List<Note> notes = Note.find.where().eq("FOREIGN_ID", student.id).findList();
		return notes;
	}

	// Create the children list for the given parent
	public static List<Student> createChildrenList(Parent parent) {
		List<Student> children = Student.find.where().eq("email", parent.email).findList();
		return children;
	}

	// Create the assignment list for the given parent
	public static List<Assignment> createAssignmentsListForParent(Parent parent) {
		List<Assignment> assignments = new ArrayList<Assignment>();
		List<Student> children = createChildrenList(parent);
		for (int i = 0; i < children.size(); i++) {
			assignments.addAll(createAssignmentsList(children.get(i)));
		}
		return assignments;
	}

	// Create the assignment list for the given teacher
	public static List<Assignment> createAssignmentsListForTeacher(Teacher teacher) {
		return sortList(Assignment.find.where().eq("FOREIGN_ID", teacher.id).findList());
	}

	// Create the schoolClasses list for the given teacher
	public static List<SchoolClass> createSchoolClassListForTeacher(Teacher teacher) {
		return SchoolClass.find.where().eq("TEACHER_ID", teacher.id).findList();
	}

	// Sort a list from the oldest date to the newest date
	public static List<Assignment> sortList(List<Assignment> assignments) {
		if (assignments.size() <= 1) return assignments;
		/*
		List<Assignment> beginningAssignments = assignments;
		List<Assignment> returnAssignments = new ArrayList<Assignment>();
		long startTime1 = System.nanoTime();
		returnAssignments.add(assignments.remove(0));

		for (int i = assignments.size() - 1; i >= 0; i--) {
			for (int j = returnAssignments.size() - 1; j >= 0; j--) {
				if (assignments.get(i).total >= returnAssignments.get(j).total) {
					if (j + 1 >= returnAssignments.size()) {
						returnAssignments.add(assignments.remove(i));
						break;
					}
					returnAssignments.add(j + 1, assignments.remove(i));
					break;
				}
				if (j == 0) returnAssignments.add(0, assignments.remove(i));
			}
		}
		long endTime1 = System.nanoTime();
		System.out.println("TEST 1: " + (double) ((endTime1 - startTime1) / 1000000.0));
		*/
		
		//long startTime2 = System.nanoTime();
		
		boolean noOperation;
		do {
			noOperation = true;
			for(int i = 0; i < assignments.size() - 1; i++) {
				if(assignments.get(i).total > assignments.get(i + 1).total) {
					Assignment tempAssignment = assignments.get(i + 1);
					assignments.set(i + 1, assignments.get(i));
					assignments.set(i, tempAssignment);
					noOperation = false;
				}
			}
		}while(!noOperation);
		
		//long endTime2 = System.nanoTime();
		//System.out.println("TEST 2: " + (double) ((endTime2 - startTime2) / 1000000.0));

		
		return assignments;
	}
}
