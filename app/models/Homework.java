package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import java.lang.String;

import play.data.validation.Constraints.*;

@Entity
public class Homework extends Model {
	
	@Id
	public Long id;
	@Required(message = "You must specify a due date.")
	public String dueDate;
	@ManyToOne
	public SchoolClass schoolClass; 
	
	public String description;
	
	public int month;
	public int day;
	public int year;
	//this is set to year * 366 - (12 - month) * 31 - (31 - day)
	public int total;
	
	public static Finder<Long, Homework> find = new Finder<Long, Homework>(Long.class, Homework.class);
	
	public Homework(String dueDate, SchoolClass schoolClass, String description, int month, int day, int year) {
		this.dueDate = dueDate;
		this.schoolClass = schoolClass;
		this.description = description;
		this.month = month;
		this.day = day;
		this.year = year;
		total = (year * 366) - ((12 - month) * 31) - (31 - day);
	}
	
	public static Homework create(String dueDate, String schoolClassId, String description) {
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
		
		Long schoolId = null;
		try {
			schoolId = Long.valueOf(schoolClassId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		Homework homework = new Homework(date, SchoolClass.find.ref(schoolId), description, monthInt, dayInt, yearInt);
		homework.save();
		return homework;
	}
	
	public static void edit(Long id, SchoolClass schoolClass, String date, String description) {
		Homework homework = find.ref(id);
		homework.schoolClass = schoolClass;
		String[] array = parseDate(date);
		String dueDate = array[0];
		homework.dueDate = dueDate;
		int year = Integer.parseInt(array[1]);
		homework.year = year;
		int month = Integer.parseInt(array[2]);
		homework.month = month;
		int day = Integer.parseInt(array[3]);
		homework.day = day;
		homework.description = description;
		Homework tempHomework = new Homework(dueDate, schoolClass, description, month, day, year);
		homework.total = tempHomework.total;
		homework.save();
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
	
//	public static void delete(Long id) {
//		find.ref(id).delete();
//	}
	
	
}