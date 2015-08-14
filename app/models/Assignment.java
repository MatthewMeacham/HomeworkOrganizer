package models;

import java.util.UUID;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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
	// The total number of days in the date
	public int total;

	public UUID foreignID;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");

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
		total = calculateTotal(day, month, year);
		this.foreignID = foreignID;
	}

	public static Assignment create(String dueDate, String schoolClassId, String kindOfAssignment, String description, String foreignID) throws PersistenceException {
		String month = "";
		String day = "";
		String year = "";

		String[] split = dueDate.split("-");
		year = split[0];
		month = split[1];
		day = split[2];

		int monthInt = Integer.parseInt(month);
		int dayInt = Integer.parseInt(day);
		int yearInt = Integer.parseInt(year);

		switch (monthInt) {
		case 1:
			month = "January";
			break;
		case 2:
			month = "February";
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

		Long schoolId = Long.valueOf(schoolClassId);

		if (description.length() > 250) description = description.substring(0, 250);
		Assignment assignment = new Assignment(date, SchoolClass.find.where().eq("ID", schoolId).findUnique(), kindOfAssignment, description, UUID.fromString(foreignID), monthInt, dayInt, yearInt);
		assignment.save();
		return assignment;
	}

	public static Assignment create(Assignment assignment, UUID foreignID) throws PersistenceException {
		String dueDate = assignment.year + "-";
		dueDate += (assignment.month <= 9) ? '0' + String.valueOf(assignment.month) + "-" : String.valueOf(assignment.month) + "-";
		dueDate += (assignment.day <= 9) ? '0' + String.valueOf(assignment.day) : String.valueOf(assignment.day);
		Assignment returnAssignment = Assignment.create(dueDate, String.valueOf(assignment.schoolClass.id), assignment.kindOfAssignment, assignment.description, foreignID.toString());
		return returnAssignment;
	}

	public static void edit(Long id, SchoolClass schoolClass, String date, String kindOfAssignment, String description) throws PersistenceException {
		Assignment assignment = find.where().eq("ID", id).findUnique();
		if (assignment == null) throw new PersistenceException("Unable to find assignment with that ID.");
		assignment.schoolClass = schoolClass;
		String[] array = parseDate(date);
		String dueDate = array[0];
		assignment.dueDate = dueDate;
		assignment.year = Integer.parseInt(array[1]);
		assignment.month = Integer.parseInt(array[2]);
		assignment.day = Integer.parseInt(array[3]);
		assignment.spanner = kindOfAssignment.substring(0, 1);
		assignment.description = description;
		assignment.total = calculateTotal(assignment.day, assignment.month, assignment.year);
		assignment.save();
	}

	public static String[] parseDate(String dueDate) {
		String month = "";
		String day = "";
		String year = "";

		String[] split = dueDate.split("-");
		year = split[0];
		month = split[1];
		day = split[2];

		int monthInt = Integer.parseInt(month);
		int dayInt = Integer.parseInt(day);
		int yearInt = Integer.parseInt(year);

		switch (Integer.parseInt(month)) {
		case 1:
			month = "January";
			break;
		case 2:
			month = "February";
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

	public static String getMonthString(int month) {
		return month < 10 ? "0" + month : "" + month;
	}

	public static String getDayString(int day) {
		return day < 10 ? "0" + day : "" + day;
	}

	public static int calculateTotal(int day, int month, int year) {
		String dateString = getDayString(day) + " " + getMonthString(month) + " " + year;
		try {
			Date date = dateFormat.parse(dateString);
			long time = date.getTime();
			return (int) TimeUnit.DAYS.convert(time, TimeUnit.MILLISECONDS);
		} catch (ParseException e) {
			//Just in case the above errors, we resort to the old method
			return (year * 366) - ((12 - month) * 31) - (31 - day);
		}
	}

	public static boolean same(Assignment assignment1, Assignment assignment2) {
		return assignment1.schoolClass.id == assignment2.schoolClass.id && assignment1.description.equals(assignment2.description) && assignment1.day == assignment2.day && assignment1.month == assignment2.month && assignment1.year == assignment2.year && assignment1.kindOfAssignment.equals(assignment2.kindOfAssignment);
	}

}
