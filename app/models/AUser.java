package models;

import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import play.data.validation.Constraints.Required;

import com.avaje.ebean.Model;

//Superclass for all models who are actual people using the web application. 
//Because otherwise we had issues with a parent or student or teacher having the same ID
//So we created this class to handle that issue
//Can't be called User because thats a database thing

@MappedSuperclass
public class AUser extends Model {

	@Id
	public UUID id;
	@Required
	public String email;
	@Required 
	public String password;
	@Required
	public String name;
	public String salt;

}
