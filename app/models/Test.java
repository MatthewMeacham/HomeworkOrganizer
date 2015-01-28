package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import play.data.validation.Constraints.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Test extends Model {

	@Id
	public Long id;
	public String dateOf;
	public String description;
	@ManyToOne
	public SchoolClass schoolClass;
	
	public int month;
	public int day;
	public int year;
	//this is set to year * 366 - (12 - month) * 31 - (31 - day)
	public int total;
	
	public static Finder<Long, Test> find = new Finder<Long, Test>(Long.class, Test.class);
	
	public Test(String dateOf, String description, SchoolClass schoolClass, int month, int day, int year) {
		this.dateOf = dateOf;
		this.description = description;
		this.schoolClass = schoolClass;
		this.month = month;
		this.day = day;
		this.year = year;
		total = (year * 366) - ((12 - month) * 31) - (31 - day);
	}
	
	public static Test create(String dateOf, String schoolClassId, String description) {
		String month = "";
		String day = "";
		String year = "";
		
		for(int i = 0; i < dateOf.length(); i++) {
			if(dateOf.charAt(i) == ('-')) continue;
			if(i < 4) year = year + (String.valueOf(dateOf.charAt(i)));
			if(i > 4 && i < 7) month = month + (String.valueOf(dateOf.charAt(i)));
			if(i > 7 && i < dateOf.length()) day = day  + (String.valueOf(dateOf.charAt(i)));
		}
				
		int monthInt = Integer.parseInt(month);
		int dayInt = Integer.parseInt(day);
		int yearInt = Integer.parseInt(year);
		
		switch(Integer.parseInt(month)) {
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
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Test test = new Test(date, description, SchoolClass.find.ref(schoolId), monthInt, dayInt, yearInt);
		test.save();
		return test;
	}
	
	public static void edit(Long id, SchoolClass schoolClass, String date, String description) {
		Test test = find.ref(id);
		test.schoolClass = schoolClass;
		String[] array = parseDate(date);
		String dateOf = array[0];
		test.dateOf = dateOf;
		int year = Integer.parseInt(array[1]);
		test.year = year;
		int month = Integer.parseInt(array[2]);
		test.month = month;
		int day = Integer.parseInt(array[3]);
		test.day = day;
		test.description = description;
		Test tempTest = new Test(dateOf, description, schoolClass, month, day, year);
		test.total = tempTest.total;
		test.save();
	}
	
	public static String[] parseDate(String dueDate) {
		String month = "";
		String day = "";
		String year = "";
		
		for(int i = 0; i < dueDate.length(); i++) {
			if(dueDate.charAt(i) == ('-')) continue;
			if(i < 4) year = year + (String.valueOf(dueDate.charAt(i)));
			if(i > 4 && i < 7) month = month + (String.valueOf(dueDate.charAt(i)));
			if(i > 7 && i < dueDate.length()) day = day  + (String.valueOf(dueDate.charAt(i)));
		}
				
		int monthInt = Integer.parseInt(month);
		int dayInt = Integer.parseInt(day);
		int yearInt = Integer.parseInt(year);
		
		switch(Integer.parseInt(month)) {
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
	
	public static void delete(Long id) {
		find.ref(id).delete();
	}
	
}