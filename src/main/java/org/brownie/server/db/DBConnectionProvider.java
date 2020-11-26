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

public class DBConnectionProvider {
	
	public static final String DB_NAME = "brownieDB.db";
	
	private static DBConnectionProvider provider = null;
	private String connectionString = "";
	
	private ConnectionSource connectionSource;
	private final Map<Class<? extends Object>, Dao<?, ?>> ormDaos = Collections.synchronizedMap(new HashMap<>());

	private DBConnectionProvider() throws SQLException {
		String pathToDB = new File("").getAbsolutePath();
		this.connectionString = "jdbc:sqlite:" + pathToDB + File.separator + DB_NAME;
		
		if(!new File(pathToDB + File.separator + DB_NAME).exists()) {
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
					e.printStackTrace();
				}
			}
		}
		return provider;
	}
	
    public boolean createDataBase() {
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
    		ex.printStackTrace();
    	} finally {
			try {
				if (rs != null) rs.close();
				if (stmt != null) stmt.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
    	}
    	
    	return result;
    }
	
	private void initDataTables() throws SQLException {
        // create a connection source to our database
        this.connectionSource =
            new JdbcConnectionSource(this.connectionString);
        
        // TODO instantiate daos all tables
        getOrmDaos().put(User.class.getClass(), DaoManager.createDao(connectionSource, User.class));

        // TODO init all tables
        TableUtils.createTableIfNotExists(connectionSource, User.class);
	}
	
	public ConnectionSource getConnectionSource() {
		return this.connectionSource;
	}
	
	public Map<Class<? extends Object>, Dao<?, ?>> getOrmDaos() {
		return this.ormDaos;
	}
}
