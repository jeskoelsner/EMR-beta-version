package org.zlwima.emurgency.backend.model;

import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@XmlRootElement(name = "user")
public class User extends EmrUser {

	@Id
	private String id;

	public void setId( String id ) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return super.toString() + " with ID: " + id;
	}

}
