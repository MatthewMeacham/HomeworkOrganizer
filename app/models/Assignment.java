package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class Assignment extends Model {
	private static final long serialVersionUID = 1L;

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

	public static Finder<Long, Assignment> find = new Finder<Long, Assignment>(Long.class, Assignment.class);

	public Assignment(String dueDate, SchoolClass schoolClass, String kindOfAssignment, String description, int month, int day, int year) {
		this.dueDate = dueDate;
		this.schoolClass = schoolClass;
		this.kindOfAssignment = kindOfAssignment;
		spanner = kindOfAssignment.substring(0, 1);
		this.description = description;
		this.month = month;
		this.day = day;
		this.year = year;
		total = (year * 366) - ((12 - month) * 31) - (31 - day);
	}

	public static Assignment create(String dueDate, String schoolClassId, String kindOfAssignment, String description) {
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
		Assignment assignment = new Assignment(date, SchoolClass.find.ref(schoolId), kindOfAssignment, description, monthInt, dayInt, yearInt);
		assignment.save();
		return assignment;
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
		Assignment tempAssignment = new Assignment(dueDate, schoolClass, kindOfAssignment, description, month, day, year);
		assignment.total = tempAssignment.total;
		assignment.save();
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

}
