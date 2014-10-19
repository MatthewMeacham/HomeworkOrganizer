package models;

import javax.persistence.*;
import play.db.ebean.*;
import com.avaje.ebean.*;
import java.util.Date;

@Entity
public class Test extends Model {

	@Id
	public Long id;
	public Date dateOf;
	public String content;
	@ManyToOne
	public SchoolClass schoolClass;
	
	public static Finder<Long, Test> find = new Finder<Long, Test>(Long.class, Test.class);
	
	public Test(Date dateOf, String content, SchoolClass schoolClass) {
		this.dateOf = dateOf;
		this.content = content;
		this.schoolClass = schoolClass;
	}
	
	public static Test create(Date dateOf, String content, Long schoolClassId) {
		Test test = new Test(dateOf, content, SchoolClass.find.ref(schoolClassId));
		test.save();
		return test;
	}
	
}