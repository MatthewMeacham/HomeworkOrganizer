package models;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Model;

@Entity
public class Note extends Model {

	@Id
	public Long id;
	public String title;
	public String notes;
	@ManyToOne
	public SchoolClass schoolClass;
	
	public UUID foreignID;

	public static Finder<Long, Note> find = new Finder<Long, Note>(Note.class);

	public Note(String title, String notes, UUID foreignID, SchoolClass schoolClass) {
		this.title = title;
		this.notes = notes;
		this.foreignID = foreignID;
		if (schoolClass != null) this.schoolClass = schoolClass;
	}

	public static Note create(String title, String notes, UUID foreignID, Long schoolClassId) {
		Note note = new Note(title, notes, foreignID, SchoolClass.find.ref(schoolClassId));
		note.save();
		return note;
	}

}