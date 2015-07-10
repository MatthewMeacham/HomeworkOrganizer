package models;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import com.avaje.ebean.Model;

//Super class for all models who are actual people using the web application. 
//Because otherwise we had issues with a parent or student or teacher having the same ID
//So we created this class to handle that issue
@MappedSuperclass
public class User extends Model {
	private static final long serialVersionUID = 1L;
	
	@Id
	public long id;

	public User() {

	}

}
