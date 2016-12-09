package no.bcdc.couplingtablebuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Class for manipulating the Coupling Table
 * @author Steve Jones
 *
 */
public class CouplingDB {

	/**
	 * SQL statement to empty the coupling table
	 */
	private static final String EMPTY_TABLE_STATEMENT = "TRUNCATE TABLE coupling";
	
	/**
	 * SQL statement to add a file to the coupling table
	 */
	private static final String ADD_FILE_STATEMENT = "INSERT INTO coupling (local_cdi_id, modus, format, filename) VALUES (?, ?, ?, ?)";
	
	/**
	 * The database connection
	 */
	private Connection dbConnection;
	
	/**
	 * Constructor initialises the database connection
	 * @param config The application configuration
	 * @throws ConfigException If the database connection cannot be established
	 */
	public CouplingDB(Config config) throws ConfigException {
		initDBConnection(config);
	}
	
	/**
	 * Connect to the database
	 */
	private void initDBConnection(Config config) throws ConfigException {

	    StringBuilder connectionString = new StringBuilder();
	    connectionString.append("jdbc:mysql://");
	    connectionString.append(config.getDBServer());
	    connectionString.append(':');
	    connectionString.append(config.getDBPort());
	    connectionString.append('/');
	    connectionString.append(config.getDBName());
	    
	    try {
	    	dbConnection = DriverManager.getConnection(connectionString.toString(), config.getDBUser(), config.getDBPassword());
	    	dbConnection.setAutoCommit(false);
	    } catch (SQLException e) {
	    	throw new ConfigException("Could not connect to database", e);
	    }
	}
	
	/**
	 * Clear all existing records from the coupling table
	 * @throws SQLException
	 */
	public void emptyCouplingTable() throws SQLException {

		PreparedStatement stmt = dbConnection.prepareStatement(EMPTY_TABLE_STATEMENT);
		stmt.execute();
		dbConnection.commit();
		stmt.close();
	}

	/**
	 * Shut down the database connection
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		dbConnection.commit();
		dbConnection.close();
	}
	
	public void addFile(String filename) throws SQLException {
		
		String localCdiId = extractLocalCdiId(filename);
		String format = extractFormat(filename);
		int modus = 3;
		
		PreparedStatement stmt = dbConnection.prepareStatement(ADD_FILE_STATEMENT);
		stmt.setString(1, localCdiId);
		stmt.setInt(2, modus);
		stmt.setString(3, format);
		stmt.setString(4, filename);
		stmt.execute();
		stmt.close();
	}

	/**
	 * Extract the Local CDI ID from a filename
	 * @param filename The filename
	 * @return The Local CDI ID
	 */
	private String extractLocalCdiId(String filename) {
		return filename.substring(0, filename.lastIndexOf('_'));
	}
	
	/**
	 * Extract the data format from the filename
	 * @param filename The filename
	 * @return The data format
	 */
	private String extractFormat(String filename) {
		int lastUnderscore = filename.lastIndexOf('_');
		int dot = filename.lastIndexOf('.');
		return filename.substring(lastUnderscore + 1, dot);
	}
}
