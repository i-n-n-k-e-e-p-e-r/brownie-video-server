package org.brownie.server.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.brownie.server.Application;

public class DBConnectionProvider {

	public static final String DB_NAME = "brownieDB.db";
	
	private static DBConnectionProvider provider = null;
	private final String connectionString;
	
	private ConnectionSource connectionSource;
	private final Map<Class<?>, Dao<?, ?>> ormDaos = Collections.synchronizedMap(new HashMap<>());

	private DBConnectionProvider() throws SQLException {
		File dbFile = new File(Application.BASE_PATH + File.separator + DB_NAME);
		this.connectionString = "jdbc:sqlite:" + dbFile.getAbsolutePath();
		Application.LOGGER.log(System.Logger.Level.INFO,
				"DB connection string '" + this.connectionString + "'");

		if(!dbFile.exists()) {
			Application.LOGGER.log(System.Logger.Level.WARNING,
					"Can't locate sqlite DB file " + dbFile.getAbsolutePath());
			createDataBase();
		}
		
		initDataTables();
	}
	
	public static DBConnectionProvider getInstance() {
		synchronized(DBConnectionProvider.class) {
			if (provider == null) {
				try {
					provider = new DBConnectionProvider();
				} catch (SQLException e) {
					Application.LOGGER.log(System.Logger.Level.ERROR,
							"Error while creating DBConnectionProvider", e);
					e.printStackTrace();
				}
			}
		}
		return provider;
	}
	
    public boolean createDataBase() {
		String pathToDB = Application.BASE_PATH + File.separator + DB_NAME;

    	boolean result = false;
    	
    	Connection connection = null;
    	Statement stmt = null;
    	ResultSet rs = null;
    	try {
    		connection = DriverManager.getConnection(this.connectionString);
    		stmt = connection.createStatement();
    		rs = stmt.executeQuery("select 1");
    		
    		if (rs.next()) result = true;
    		
    	} catch (Exception ex) {
			Application.LOGGER.log(System.Logger.Level.ERROR,
					"Error while creating sqlite DB '" + pathToDB + "'", ex);
    		ex.printStackTrace();
    	} finally {
			try {
				if (rs != null) rs.close();
				if (stmt != null) stmt.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				Application.LOGGER.log(System.Logger.Level.ERROR,
						"Error while closing statements", e);
				e.printStackTrace();
			}
    	}
		Application.LOGGER.log(System.Logger.Level.INFO,
				"Created sqlite DB '" + pathToDB + "'");
    	return result;
    }
	
	private void initDataTables() throws SQLException {
        // create a connection source to our database
        this.connectionSource =
            new JdbcConnectionSource(this.connectionString);
        
        // TODO instantiate daos all tables
        getOrmDaos().put(User.class, DaoManager.createDao(connectionSource, User.class));

        // TODO init all tables
        TableUtils.createTableIfNotExists(connectionSource, User.class);
	}
	
	public ConnectionSource getConnectionSource() {
		return this.connectionSource;
	}
	
	public Map<Class<?>, Dao<?, ?>> getOrmDaos() {
		return this.ormDaos;
	}
}
