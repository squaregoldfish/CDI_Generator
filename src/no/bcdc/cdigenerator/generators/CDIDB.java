package no.bcdc.cdigenerator.generators;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import no.bcdc.cdigenerator.Config;
import no.bcdc.cdigenerator.ConfigException;
import no.bcdc.cdigenerator.importers.ImporterException;

/**
 * Database calls for the CDI Database
 * @author Steve Jones
 *
 */
public class CDIDB {

	/**
	 * Statement to clear the CDI summary table
	 */
	private static final String CLEAR_CDI_STATEMENT = "TRUNCATE TABLE cdi_summary";
	
	/**
	 * Query for identifying a platform ID
	 */
	private static final String GET_PLATFORM_ID_QUERY = "SELECT id FROM cdi_platforms WHERE platform_code = ? AND start_date <= ? LIMIT 1";

	/**
	 * Query for inserting a CDI Summary
	 */
	private static final String STORE_CDI_SUMMARY_STATEMENT = "INSERT INTO cdi_summary ("
			+ "local_cdi_id, platform_id, dataset_name, dataset_id, doi, doi_url, "
			+ "abstract, cruise_name, cruise_start_date, west_longitude, east_longitude, "
			+ "south_latitude, north_latitude, start_date, end_date, distribution_data_size, "
		    + "curves_description, curves_name, curves_coordinates, csr_reference) "
			+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	/**
	 * The database connection
	 */
	private Connection dbConnection;
	
	/**
	 * Constructor initialises the database connection
	 * @param config The application configuration
	 * @throws ConfigException If the database connection cannot be established
	 */
	public CDIDB(Config config) throws ConfigException {
		initDBConnection(config);
	}
	
	/**
	 * Empty the CDI Summary table ready for a new data set to be added
	 * @throws DatabaseException If an error occurs
	 */
	public void clearCdiSummary() throws DatabaseException {
		
		PreparedStatement stmt = null;
		
		try {
			stmt = dbConnection.prepareStatement(CLEAR_CDI_STATEMENT);
			stmt.execute();
		} catch (SQLException e) {
			throw new DatabaseException("Error while clearing cdi_summary table", e);
		} finally {
			closeStatements(stmt);
		}
	}
	
	/**
	 * Get the database platform ID for a given platform and start date.
	 * This is used in MIKADO to look for things like the platform type and operator's EDMO code
	 * 
	 * Just using the platform code isn't enough, because platforms can change hands over time.
	 * So we use the start date of the data set to determine who 'owned' the platform at that particular time.
	 * 
	 * @param platformCode The platform code. E.g. the 4-letter NODC ship code
	 * @param startDate The start date for the data set.
	 * @return The platform ID
	 * @throws DatabaseException If a database error occurred 
	 * @throws MissingDatabaseDataException If the platform ID cannot be determined for the given data. This means information must be added to the database.
	 */
	public long getPlatformId(String platformCode, Date startDate) throws DatabaseException, MissingDatabaseDataException {
		
		PreparedStatement stmt = null;
		ResultSet records = null;
		
		try {
			stmt = dbConnection.prepareStatement(GET_PLATFORM_ID_QUERY);
			stmt.setString(1, platformCode);
			stmt.setDate(2, new java.sql.Date(startDate.getTime()));
			
			records = stmt.executeQuery();
			if (!records.next()) {
				StringBuilder message = new StringBuilder("The platform ID for platform ");
				message.append(platformCode);
				message.append(" and start date ");
				message.append(new SimpleDateFormat("yyy-MM-dd").format(startDate));
				message.append(" is not in the database");
				throw new MissingDatabaseDataException(message.toString());
			} else {
				return records.getInt(1);
			}
			
		} catch (SQLException e) {
			throw new DatabaseException("Error while retrieving platform ID", e);
		} finally {
			closeResultSets(records);
			closeStatements(stmt);
		}
	}
	
	public void storeCdiSummary(CDISummary summary) throws DatabaseException, ImporterException, MissingDatabaseDataException {
		
		PreparedStatement stmt = null;
		
		try {
			stmt = dbConnection.prepareStatement(STORE_CDI_SUMMARY_STATEMENT);
			
			stmt.setString(1, summary.getLocalCdiId());
			stmt.setLong(2, summary.getPlatformId());
			stmt.setString(3, summary.getDataSetName());
			stmt.setString(4, summary.getDataSetId());
			stmt.setString(5, summary.getDoi());
			stmt.setString(6, summary.getDoiUrl());
			stmt.setString(7, summary.getAbstract());
			stmt.setString(8, summary.getCruiseName());
			stmt.setDate(9, new java.sql.Date(summary.getStartDate().getTime()));
			stmt.setDouble(10, summary.getWestLongitude());
			stmt.setDouble(11, summary.getEastLongitude());
			stmt.setDouble(12, summary.getSouthLatitude());
			stmt.setDouble(13, summary.getNorthLatitude());
			stmt.setLong(14, javaMstoMySqlMs(summary.getStartDateTime()));
			stmt.setLong(15, javaMstoMySqlMs(summary.getEndDateTime()));
			stmt.setString(16, summary.getDistributionDataSize());
			stmt.setString(17, summary.getCurvesDescription());
			stmt.setString(18, summary.getCurvesName());
			stmt.setString(19, summary.getCurvesCoordinates());
			stmt.setString(20, summary.getCsrReference());
			
			stmt.execute();
			
		} catch (SQLException e) {
			throw new DatabaseException("Error while storing CDI Summary", e);
		} finally {
			closeStatements(stmt);
		}
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
	    } catch (SQLException e) {
	    	throw new ConfigException("Could not connect to database", e);
	    }
	}
	
	/**
	 * Close a set of PreparedStatement objects, ignoring any errors
	 * @param statements The statements
	 */
	private void closeStatements(PreparedStatement... statements) {
		for (PreparedStatement stmt : statements) {
			if (null != stmt) {
				try {
					stmt.close();
				} catch (SQLException e) {
					// Do nothing
				}
			}
		}
	}
	
	/**
	 * Close a set of ResultSet objects, ignoring any errors
	 * @param results The ResultSets
	 */
	private void closeResultSets(ResultSet... results) {
		for (ResultSet result : results) {
			if (null != result) {
				try {
					result.close();
				} catch(SQLException e) {
					// Do nothing
				}
			}
		}			
	}
	
	/**
	 * Convert a Jave date numeric value to a MySQL date numeric value
	 * @param javaMs The Java value
	 * @return The MySQL value
	 */
	private long javaMstoMySqlMs(long javaMs) {
		return javaMs / 1000;
	}
}
