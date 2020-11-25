package org.brownie.server.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnectionProvider {
	
	public static final String DB_NAME = "brownieDB.db";
	
	private static DBConnectionProvider provider = null;
	private static Connection connection = null;
	private String connectionString = "";

	private DBConnectionProvider() {
		String pathToDB = new File("").getAbsolutePath();
		this.connectionString = "jdbc:sqlite:" + pathToDB + File.separator + DB_NAME;
		try {
			connection = DriverManager.getConnection(connectionString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private DBConnectionProvider(String connectionString) {
		this.connectionString = connectionString;
		try {
			connection = DriverManager.getConnection(connectionString);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static DBConnectionProvider getInstance() {
		synchronized(DBConnectionProvider.class) {
			if (provider == null) {
				provider = new DBConnectionProvider();
			}
		}
		return provider;
	}
	
	public static DBConnectionProvider getInstance(String connectionString) {
		synchronized(DBConnectionProvider.class) {
			if (provider == null) {
				provider = new DBConnectionProvider(connectionString);
			}
		}
		return provider;
	}
	
    public boolean testConnection() {
    	return false;
    	
    	//TODO
//    	Statement stmt = null;
//    	ResultSet rs = null;
//    	try {
//    		String pathToDB = new File("").getAbsolutePath();
//    		String connectionString = "jdbc:sqlite:" + pathToDB + "/" + DB_NAME;
//    		System.out.println(connectionString);
//    		conn = DriverManager.getConnection(connectionString);
//    		stmt = conn.createStatement();
//    		rs = stmt.executeQuery("select datetime('now')");
//    		if(rs.next()) {
//    			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    			System.out.print("from db " + dateFormat.parse(rs.getString(1)));
//    		}
//    	} catch (Exception ex) {
//    		ex.printStackTrace();
//    	} finally {
//    		if (rs != null) {
//    			try {
//					rs.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    		}
//    		if(stmt != null) {
//    			try {
//					stmt.close();
//				} catch (SQLException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//    		}
//    	}
    }

	public static Connection getConnection() {
		return connection;
	}

	public String getConnectionString() {
		return connectionString;
	}
}
