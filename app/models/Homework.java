package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.lang.String;

import play.data.validation.Constraints.*;

@Entity
public class Homework extends Model {
	
	@Id
	public Long id;
	@Required
	public Date dueDate;
	@ManyToOne
	public SchoolClass schoolClass; 
	
	private static final String DATE_FORMAT = "MM-dd";
	private static DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	
	public static Finder<Long, Homework> find = new Finder<Long, Homework>(Long.class, Homework.class);
	
	public Homework(Date dueDate, SchoolClass schoolClass) {
		this.dueDate = dueDate;
		this.schoolClass = schoolClass;
	}
	
	//THIS NEEDS TO BE WORKED ON IN ORDER FOR THE THINGY TO WORK... THE DATE COMES IN AS A STRING AND IT NEEDS TO BE SENT OUT AS A DATE
	//BUT THE STRING IT COMES IN IS NOT PARSEABLE AND SO IT NEEDS TO BE CHANGED TO A WAY THAT IT CAN BE PARSED
	
	public static Homework create(String dueDate, String schoolClassId) {
		System.out.println("DUEDATE: " + dueDate);
		System.out.println("SCHOOLCLASSID: " + schoolClassId);
		
		String month = "";
		String day = "";
		String year = "";
		
		for(int i = 0; i < dueDate.length(); i++) {
			if(dueDate.charAt(i) == ('-')) continue;
			if(i < 4) year = year + (String.valueOf(dueDate.charAt(i)));
			if(i > 4 && i < 7) month = month + (String.valueOf(dueDate.charAt(i)));
			if(i > 7 && i < dueDate.length()) day = day  + (String.valueOf(dueDate.charAt(i)));
		}
		
		System.out.println("MONTH: " + month);
		System.out.println("DAY: " + day);
		String completeDate = month + " " + day;
		System.out.println("COMPLETE DATE: "+  completeDate);
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat("MMMM d");
		Calender calender = new Calender();
		calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
		calendar.set(Calendar.MONTH, Integer.parseInt(month));
		calender.set(Calender.YEAR, Integer.parseIn(year));
		
		try {
			date = sdf.parse(completeDate);
		} catch(Exception e) {
			e.printStackTrace();
		}

		Long schoolId = null;
		try {
			schoolId = Long.valueOf(schoolClassId);
		} catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("DUEDATE: " + date);
		Homework homework = new Homework(date, SchoolClass.find.ref(schoolId));
		homework.save();
		return homework;
	}
	
	
}