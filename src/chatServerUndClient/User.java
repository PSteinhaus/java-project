package chatServerUndClient;

import java.net.*;
import java.io.*;

public class User implements java.io.Serializable {
	private String name;
	private String password;

	User(String name, String password) {
		this.name 		= name;
		this.password 	= password;
	}

	public String getName() {
		return name;
	}

	public boolean tryPassword(String word) {
		return word.equals(password);
	}
}