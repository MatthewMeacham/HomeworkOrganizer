package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;

import play.data.validation.Constraints.Required;

import com.avaje.ebean.Model;

@Entity
public class Assignment extends Model {

	@Id
	public Long id;
	@Required(message = "You must specify a due date.")
	public String dueDate;
	@ManyToOne
	public SchoolClass schoolClass;
	@Required
	public String kindOfAssignment;

	public String spanner;
	public String description;

	public boolean finished = false;

	public int month;
	public int day;
	public int year;
	// this is set to year * 366 - (12 - month) * 31 - (31 - day)
	public int total;

	public UUID foreignID;

	public static Finder<Long, Assignment> find = new Finder<Long, Assignment>(Assignment.class);

	public Assignment(String dueDate, SchoolClass schoolClass, String kindOfAssignment, String description, UUID foreignID, int month, int day, int year) {
		this.dueDate = dueDate;
		this.schoolClass = schoolClass;
		this.kindOfAssignment = kindOfAssignment;
		spanner = kindOfAssignment.substring(0, 1);
		this.description = description;
		this.month = month;
		this.day = day;
		this.year = year;
		total = (year * 366) - ((12 - month) * 31) - (31 - day);
		this.foreignID = foreignID;
	}

	public static Assignment create(String dueDate, String schoolClassId, String kindOfAssignment, String description, String foreignID) {
		String month = "";
		String day = "";
		String year = "";

		for (int i = 0; i < dueDate.length(); i++) {
			if (dueDate.charAt(i) == ('-')) continue;
			if (i < 4) year = year + (String.valueOf(dueDate.charAt(i)));
			if (i > 4 && i < 7) month = month + (String.valueOf(dueDate.charAt(i)));
			if (i > 7 && i < dueDate.length()) day = day + (String.valueOf(dueDate.charAt(i)));
		}

		int monthInt = Integer.parseInt(month);
		int dayInt = Integer.parseInt(day);
		int yearInt = Integer.parseInt(year);

		switch (Integer.parseInt(month)) {
		case 1:
			month = "January";
			break;
		case 2:
			month = "Feburary";
			break;
		case 3:
			month = "March";
			break;
		case 4:
			month = "April";
			break;
		case 5:
			month = "May";
			break;
		case 6:
			month = "June";
			break;
		case 7:
			month = "July";
			break;
		case 8:
			month = "August";
			break;
		case 9:
			month = "September";
			break;
		case 10:
			month = "October";
			break;
		case 11:
			month = "November";
			break;
		case 12:
			month = "December";
			break;
		}

		String date = month + " " + day + ", " + year;

		Long schoolId = null;
		try {
			schoolId = Long.valueOf(schoolClassId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (description.length() > 250) description = description.substring(0, 250);
		Assignment assignment = new Assignment(date, SchoolClass.find.ref(schoolId), kindOfAssignment, description, UUID.fromString(foreignID), monthInt, dayInt, yearInt);
		assignment.save();
		return assignment;
	}

	public static Assignment create(Assignment assignment, UUID foreignID) {
		String dueDate = assignment.year + "-";
		dueDate += (assignment.month <= 9) ? '0' + String.valueOf(assignment.month) + "-" : String.valueOf(assignment.month) + "-";
		dueDate += (assignment.day <= 9) ? '0' + String.valueOf(assignment.day) : String.valueOf(assignment.day);
		Assignment returnAssignment = Assignment.create(dueDate, String.valueOf(assignment.schoolClass.id), assignment.kindOfAssignment, assignment.description, foreignID.toString());
		return returnAssignment;
	}

	public static void edit(Long id, SchoolClass schoolClass, String date, String kindOfAssignment, String description) {
		Assignment assignment = find.ref(id);
		assignment.schoolClass = schoolClass;
		String[] array = parseDate(date);
		String dueDate = array[0];
		assignment.dueDate = dueDate;
		int year = Integer.parseInt(array[1]);
		assignment.year = year;
		int month = Integer.parseInt(array[2]);
		assignment.month = month;
		int day = Integer.parseInt(array[3]);
		assignment.day = day;
		assignment.spanner = kindOfAssignment.substring(0, 1);
		assignment.description = description;
		assignment.total = (year * 366) - ((12 - month) * 31) - (31 - day);
		try {
			assignment.save();
		} catch (PersistenceException e) {
			System.err.println("Unable to save an assignment after editing. ID: " + id);
		}
	}

	public static String[] parseDate(String dueDate) {
		String month = "";
		String day = "";
		String year = "";

		for (int i = 0; i < dueDate.length(); i++) {
			if (dueDate.charAt(i) == ('-')) continue;
			if (i < 4) year = year + (String.valueOf(dueDate.charAt(i)));
			if (i > 4 && i < 7) month = month + (String.valueOf(dueDate.charAt(i)));
			if (i > 7 && i < dueDate.length()) day = day + (String.valueOf(dueDate.charAt(i)));
		}

		int monthInt = Integer.parseInt(month);
		int dayInt = Integer.parseInt(day);
		int yearInt = Integer.parseInt(year);

		switch (Integer.parseInt(month)) {
		case 1:
			month = "January";
			break;
		case 2:
			month = "Feburary";
			break;
		case 3:
			month = "March";
			break;
		case 4:
			month = "April";
			break;
		case 5:
			month = "May";
			break;
		case 6:
			month = "June";
			break;
		case 7:
			month = "July";
			break;
		case 8:
			month = "August";
			break;
		case 9:
			month = "September";
			break;
		case 10:
			month = "October";
			break;
		case 11:
			month = "November";
			break;
		case 12:
			month = "December";
			break;
		}

		String date = month + " " + day + ", " + year;
		String[] returningArray = new String[4];
		returningArray[0] = date;
		returningArray[1] = String.valueOf(yearInt);
		returningArray[2] = String.valueOf(monthInt);
		returningArray[3] = String.valueOf(dayInt);
		return returningArray;
	}

	public static boolean same(Assignment assignment1, Assignment assignment2) {
		return assignment1.dueDate.equals(assignment2.dueDate) && assignment1.description.equals(assignment2.description) && assignment1.day == assignment2.day && assignment1.month == assignment2.month && assignment1.year == assignment2.year && assignment1.kindOfAssignment.equals(assignment2.kindOfAssignment);
	}

}
