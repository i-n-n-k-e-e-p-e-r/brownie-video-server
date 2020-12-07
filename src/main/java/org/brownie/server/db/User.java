package org.brownie.server.db;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "users")
public class User {

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public enum GROUP {
		ADMIN,
		USER
	}
	
    @DatabaseField(generatedId = true, 
    		allowGeneratedIdInsert=true,
    		uniqueIndex = true)
    private Integer userId;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String passwordHash;
    @DatabaseField(canBeNull = false)
    private String random;
    @DatabaseField(canBeNull = false)
    private Integer group;
    
    public User() {
        // ORMLite needs a no-arg constructor
    }
    
    public User(String name, String passwordHash, String random, Integer group) {
    	this.userId = null;
        this.name = name;
        this.passwordHash = passwordHash;
        this.random = random;
        this.group = group;
    }
    
    public User(Integer userId, String name, String random, String passwordHash, Integer group) {
    	this.userId = userId;
        this.name = name;
        this.passwordHash = passwordHash;
        this.random = random;
        this.group = group;
    }
    
    public String getName() {
        return name;
    }
    
    public User setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public User setPasswordHash(String password) {
        this.passwordHash = password;
        return this;
    }

    public User setGroup(Integer group) {
        this.group = group;
        return this;
    }

    public Integer getGroup() {
    	return this.group;
    }

    public Integer getUserId() {
    	return this.userId;
    }
    
    @SuppressWarnings("unchecked")
	public void updateUserDBData() throws SQLException {
    	((Dao<User, Integer>)DBConnectionProvider.getInstance().getOrmDaos()
    			.get(User.class)).createOrUpdate(this);
    }
    
    @SuppressWarnings("unchecked")
	public void deleteUserFromDB() {
    	try {
			((Dao<User, Integer>)DBConnectionProvider.getInstance().getOrmDaos()
					.get(User.class)).delete(this);
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    public static boolean isUserNameFreeToUse(String userName) {
    	try {
			return DBConnectionProvider.getInstance().getOrmDaos().get(User.class).queryForEq("name", userName).size() == 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return false;
    }
}
