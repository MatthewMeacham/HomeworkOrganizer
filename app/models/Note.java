package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;

@Entity
public class Note extends Model{
	
	@Id
	public Long id;
	public String title;
	public String notes;
	//TODO this is an optional thing, is there a special annotation for that?
	@ManyToOne
	public Student student;
	//TODO this is an optional thing, is there a special annotation for that?
	@ManyToOne
	public SchoolClass schoolClass;
	
	public static Finder<Long, Note> find = new Finder<Long, Note>(Long.class, Note.class);
	
	public Note(String title, String notes, Student student, SchoolClass schoolClass) {
		this.title = title;
		this.notes = notes;
		if(student != null) this.student = student;
		if(schoolClass != null) this.schoolClass = schoolClass;
	}
	
	public static Note create(String title, String notes, String studentEmail, Long schoolClassId) {
		Note note = new Note(title, notes, Student.find.ref(studentEmail), SchoolClass.find.ref(schoolClassId));
		note.save();
		return note;
	}
	
}