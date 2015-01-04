package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import java.lang.String;
import java.util.Calendar;

import play.data.validation.Constraints.*;

@Entity
public class Homework extends Model {
	
	@Id
	public Long id;
	@Required
	public String dueDate;
	@ManyToOne
	public SchoolClass schoolClass; 
	
	public static Finder<Long, Homework> find = new Finder<Long, Homework>(Long.class, Homework.class);
	
	public Homework(String dueDate, SchoolClass schoolClass) {
		this.dueDate = dueDate;
		this.schoolClass = schoolClass;
	}
	
	//THIS NEEDS TO BE WORKED ON IN ORDER FOR THE THINGY TO WORK... THE DATE COMES IN AS A STRING AND IT NEEDS TO BE SENT OUT AS A DATE
	//BUT THE STRING IT COMES IN IS NOT PARSEABLE AND SO IT NEEDS TO BE CHANGED TO A WAY THAT IT CAN BE PARSED
	
	public static Homework create(String dueDate, String schoolClassId) {
		String month = "";
		String day = "";
		String year = "";
		
		for(int i = 0; i < dueDate.length(); i++) {
			if(dueDate.charAt(i) == ('-')) continue;
			if(i < 4) year = year + (String.valueOf(dueDate.charAt(i)));
			if(i > 4 && i < 7) month = month + (String.valueOf(dueDate.charAt(i)));
			if(i > 7 && i < dueDate.length()) day = day  + (String.valueOf(dueDate.charAt(i)));
		}
		
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
		Homework homework = new Homework(date, SchoolClass.find.ref(schoolId));
		homework.save();
		return homework;
	}
	
	
}