//package controllers;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import models.Assignment;
//import models.Parent;
//import models.SchoolClass;
//import models.Student;
//import models.Teacher;
//
//public class Utilities {
//
//	// Checks to see if the student account with the given studentID is a child
//	// account meaning it was parent created
//	public static boolean childAccount(String studentID) {
//		List<Parent> parents = Parent.find.all();
//		Student student = Student.find.ref(Long.valueOf(studentID));
//		for (Parent parent : parents) {
//			if (student.email.equals(parent.email)) return true;
//		}
//		return false;
//	}
//	
//	// Creates the assignments list for the student
//	//TODO FIND A BETTER WAY TO DO THIS
//	public static List<Assignment> createAssignmentsList(Student student) {
//		setToday();
//		//List<Assignment> assignments = Assignment.find.where().eq("FOREIGN_ID", student.id).eq("FINISHED", false).findList();
//		List<Assignment> allAssignments = Assignment.find.all();
//		List<Assignment> returnAssignments = new ArrayList<Assignment>();
//		for(int i = 0; i < allAssignments.size(); i++) {
//			if(allAssignments.get(i).schoolClass.students.contains(student) && !allAssignments.get(i).finished) returnAssignments.add(allAssignments.get(i));
//		}
//		return sortList(returnAssignments);
//		//return sortList(assignments);
//	}
//	
//	// Create the assignment list for the given parent
//	public static List<Assignment> createAssignmentsListForParent(Parent parent) {
//		List<Assignment> assignments = new ArrayList<Assignment>();
//		List<Student> children = createChildrenList(parent);
//		for (int i = 0; i < children.size(); i++) {
//			assignments.addAll(createAssignmentsList(children.get(i)));
//		}
//		return assignments;
//	}
//		
//	// Create the assignment list for the given teacher
//	public static List<Assignment> createAssignmentsListForTeacher(Teacher teacher) {
//		return sortList(Assignment.find.where().eq("FOREIGN_ID", teacher.id).findList());
//	}
//	
//	// Create the children list for the given parent
//	public static List<Student> createChildrenList(Parent parent) {
//		List<Student> children = Student.find.where().eq("email", parent.email).findList();
//		for (int i = children.size() - 1; i >= 0; i--) {
//			if (children.get(i).parent == null) continue;
//		}
//		return children;
//	}
//	
//	// Creates the finished assignments list for the student
//	public static List<Assignment> createFinishedAssignmentsList(Student student) {
//		setToday();
//		List<Assignment> finishedAssignments = Assignment.find.where().eq("FOREIGN_ID", student.id).eq("FINISHED", true).findList();
//		return sortList(finishedAssignments);
//	}
//	
//	// Creates the late assignments list for the given student
//	public static List<Assignment> createLateAssignmentsList(Student student) {
//		setToday();
//		List<Assignment> lateAssignments = Assignment.find.where().eq("FOREIGN_ID", student.id).eq("FINISHED", false).findList();
//
//		for (int i = 0; i < lateAssignments.size(); i++) {
//			if (lateAssignments.get(i).total >= today) {
//				lateAssignments.remove(i);
//				i--;
//			}
//		}
//		return sortList(lateAssignments);
//	}
//	
//	// Create the notes list for the given student
//	public static List<Note> createNotesList(Student student) {
//		List<Note> notes = Note.find.where().eq("FOREIGN_ID", student.id).findList();
//		return notes;
//	}
//	
//	// Creates the school classes list for the given student
//	//TODO FIND BETTER WAY TO DO THIS
//	public static List<SchoolClass> createSchoolClassesList(Student student) {
//		List<SchoolClass> schoolClasses = SchoolClass.find.all();
//		List<SchoolClass> returnSchoolClasses = new ArrayList<SchoolClass>();
//		for(int i = 0; i < schoolClasses.size(); i++) {
//			if(schoolClasses.get(i).students == null || schoolClasses.get(i).students.size() <= 0) continue; 
//			for(int j = 0; j < schoolClasses.get(i).students.size(); j++) {
//				if(schoolClasses.get(i).students.get(j).id == student.id) {
//					returnSchoolClasses.add(schoolClasses.get(i));
//					break;
//				}
//			}
//		}
//		return returnSchoolClasses;
//		//return SchoolClass.find.where().eq("student_id", student.id).findList();
//	}
//	
//	// Create the schoolClasses list for the given teacher
//	public static List<SchoolClass> createSchoolClassListForTeacher(Teacher teacher) {
//		System.out.println("TEACHER ID: " + teacher.id);
//		return SchoolClass.find.where().eq("TEACHER_ID", teacher.id).findList();
//	}
//	
//	// TODO FIND A BETTER WAY TO DO THIS CREATION
//	public static List<Teacher> createTeachersList(Student student) {
//		List<Teacher> teachers = new ArrayList<Teacher>();
////			teachers = Teacher.find.all();
////			// teachers = Teacher.find.where().eq("student.id",
////			// student.id).findList();
////			for (int i = teachers.size() - 1; i >= 0; i--) {
////				if (!student.teacher.email.equals(teachers.get(i).email)) teachers.remove(i);
////			}
//		return teachers;
//	}
//	
//	// Sets the variable today, the number of days in the date since 0AD
//	public static void setToday() {
//		int day = Calendar.getInstance().get(Calendar.DATE);
//		// have to add one because 0 is January
//		int month = Calendar.getInstance().get(Calendar.MONTH) + 1;
//		int year = Calendar.getInstance().get(Calendar.YEAR);
//		int total = (year * 366) - ((12 - month) * 31) - (31 - day);
//		today = total;
//	}
//	
//	// Create the schoolClasses list for the given teacher
//	public static List<SchoolClass> createSchoolClassListForTeacher(Teacher teacher) {
//		System.out.println("TEACHER ID: " + teacher.id);
//		return SchoolClass.find.where().eq("TEACHER_ID", teacher.id).findList();
//	}
//	
//	// Sort a list from the oldest date to the newest date
//		public static List<Assignment> sortList(List<Assignment> assignments) {
//			if (assignments.size() <= 1) return assignments;
//			List<Assignment> returnAssignments = new ArrayList<Assignment>();
//			returnAssignments.add(assignments.remove(0));
//
//			for (int i = assignments.size() - 1; i >= 0; i--) {
//				for (int j = returnAssignments.size() - 1; j >= 0; j--) {
//					if (assignments.get(i).total >= returnAssignments.get(j).total) {
//						if (j + 1 >= returnAssignments.size()) {
//							returnAssignments.add(assignments.remove(i));
//							break;
//						}
//						returnAssignments.add(j + 1, assignments.remove(i));
//						break;
//					}
//					if (j == 0) returnAssignments.add(0, assignments.remove(i));
//				}
//			}
//			return returnAssignments;
//		}
//}
