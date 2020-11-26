package org.brownie.server.db;

import java.sql.SQLException;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.table.DatabaseTable;


@DatabaseTable(tableName = "users")
public class User {
    
	public enum GROUP {
		ADMIN,
		USER;
	}
	
    @DatabaseField(generatedId = true, 
    		canBeNull = false, 
    		allowGeneratedIdInsert=true,
    		uniqueIndex = true)
    private Integer userId;
    @DatabaseField(canBeNull = false)
    private String name;
    @DatabaseField(canBeNull = false)
    private String password;
    @DatabaseField(canBeNull = false)
    private Integer group;
    
    private GROUP userGroup;
    
    public User() {
        // ORMLite needs a no-arg constructor 
    }
    
    public User(String name, String password, GROUP group) {
    	this.userId = null;
        this.name = name;
        this.password = password;
        this.userGroup = group;
        this.group = this.userGroup.ordinal();
    }
    
    public User(Integer userId, String name, String password, GROUP group) {
    	this.userId = userId;
        this.name = name;
        this.password = password;
        this.userGroup = group;
        this.group = this.userGroup.ordinal();
    }
    
    public String getName() {
        return name;
    }
    
    public User setName(String name) {
        this.name = name;
        return this;
    }
    
    public String getPassword() {
        return password;
    }
    
    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    
    public GROUP getUserGroup() {
    	return this.userGroup;
    }
    
    public User setUserGroup(GROUP group) {
        this.userGroup = group;
        this.group = this.userGroup.ordinal();
    	return this;
    }
    
    public Integer getUserId() {
    	return this.userId;
    }
    
    public void updateUserDBData() throws SQLException {
    	//TODO
    	UpdateBuilder<?, ?> updateBuilder = DBConnectionProvider.getInstance().getOrmDaos().get(User.class.getClass()).updateBuilder();
    	// set the criteria like you would a QueryBuilder
    	updateBuilder.where().eq("userId", this.getUserId());
    	// update the value of your field(s)
    	updateBuilder.updateColumnValue("name", this.getName());
    	updateBuilder.updateColumnValue("password", this.getPassword());
    	updateBuilder.updateColumnValue("group", this.getUserGroup().ordinal());
    	updateBuilder.update();
    }
    
    public void deleteUserFromDB() {
    	//TODO
    }
    
    public static boolean isUserNameFreeToUse(String userName) {
    	try {
			return DBConnectionProvider.getInstance().getOrmDaos().get(User.class.getClass()).queryForEq("name", userName).size() == 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	
    	return false;
    }
}
