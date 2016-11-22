package no.bcdc.cdigenerator.generators;

import java.sql.SQLException;

public class DatabaseException extends Exception {

	private static final long serialVersionUID = 6067277261836868071L;

	public DatabaseException(String message, SQLException e) {
		super(message, e);
	}
	
}
