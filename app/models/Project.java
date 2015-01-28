package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import play.data.validation.Constraints.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
public class Project extends Model {
	
	@Id 
	public Long id;
	@ManyToOne
	public SchoolClass schoolClass;
	
	public String description;
	public String dueDate;
	public int month;
	public int day;
	public int year;
	//this is set to year * 366 - (12 - month) * 31 - (31 - day)
	public int total;
	
	public static Finder<Long, Project> find = new Finder<Long, Project>(Long.class, Project.class);
	
	public Project(String dueDate, SchoolClass schoolClass, String description, int month, int day, int year) {
		this.dueDate = dueDate;
		this.schoolClass = schoolClass;
		this.description = description;
		this.month = month;
		this.day = day;
		this.year = year;
		total = (year * 366) - ((12 - month) * 31) - (31 - day);
	}
	
	public static Project create(String dueDate, String schoolClassId, String description) {
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
		Project project = new Project(date, SchoolClass.find.ref(schoolId), description, monthInt, dayInt, yearInt);
		project.save();
		return project;
	}
	
	public static void edit(Long id, SchoolClass schoolClass, String date, String description) {
		Project project = find.ref(id);
		project.schoolClass = schoolClass;
		String[] array = parseDate(date);
		String dueDate = array[0];
		project.dueDate = dueDate;
		int year = Integer.parseInt(array[1]);
		project.year = year;
		int month = Integer.parseInt(array[2]);
		project.month = month;
		int day = Integer.parseInt(array[3]);
		project.day = day;
		project.description = description;
		Project tempProject = new Project(dueDate, schoolClass, description, month, day, year);
		project.total = tempProject.total;
		project.save();
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