package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;

@Entity
public class Note extends Model {
	private static final long serialVersionUID = 1L;

	@Id
	public Long id;
	public String title;
	public String notes;
	@ManyToOne
	public Student student;
	// TODO this is an optional thing, is there a special annotation for that?
	@ManyToOne
	public SchoolClass schoolClass;

	public static Finder<Long, Note> find = new Finder<Long, Note>(Long.class, Note.class);

	public Note(String title, String notes, Student student, SchoolClass schoolClass) {
		this.title = title;
		this.notes = notes;
		if (student != null) this.student = student;
		if (schoolClass != null) this.schoolClass = schoolClass;
	}

	public static Note create(String title, String notes, Long studentId, Long schoolClassId) {
		Note note = new Note(title, notes, Student.find.ref(studentId), SchoolClass.find.ref(schoolClassId));
		note.save();
		return note;
	}

}