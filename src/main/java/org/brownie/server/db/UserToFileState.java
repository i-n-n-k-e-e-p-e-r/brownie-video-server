package org.brownie.server.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.brownie.server.Application;

import javax.validation.constraints.NotNull;
import java.sql.SQLException;
import java.util.*;

@DatabaseTable(tableName = "user_to_file_state")
public class UserToFileState {

    @DatabaseField(generatedId = true,
            allowGeneratedIdInsert=true,
            uniqueIndex = true)
    private Long entryId;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, uniqueCombo = true)
    private User user;
    @DatabaseField(uniqueCombo = true)
    private String fileName;
    @DatabaseField()
    private Integer pausedAt;

    UserToFileState() {
        // ORMLite needs a no-arg constructor
    }

    public UserToFileState(@NotNull User user, @NotNull String fileName) {
        this.entryId = null;
        this.user = user;
        this.fileName = fileName;

        // TODO continue playing
        this.pausedAt = 0;
    }

    @SuppressWarnings("unchecked")
    public static List<UserToFileState> getEntry(DBConnectionProvider provider,
                                                  String fileName, User user) {
        List<UserToFileState> result = new ArrayList<>();
        
        Map<String, Object> fields = new HashMap<>();
        fields.put("fileName", fileName);
        fields.put("user_id", user.getUserId());

        try {
            result = ((Dao<UserToFileState, Integer>)provider.getOrmDaos()
                    .get(UserToFileState.class)).queryForFieldValuesArgs(fields);
        } catch (SQLException e) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    "Exception while getting entry from table 'UserToFileState' in getEntryForFile",
                    e);
            e.printStackTrace();
        } finally {
            fields.clear();
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<UserToFileState> getEntriesForUser(DBConnectionProvider provider, User user) {
        List<UserToFileState> result = new ArrayList<>();

        Map<String, Object> fields = new HashMap<>();
        fields.put("user_id", user.getUserId());

        try {
            result = ((Dao<UserToFileState, Integer>)provider.getOrmDaos()
                    .get(UserToFileState.class)).queryForFieldValuesArgs(fields);
        } catch (SQLException e) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    "Exception while getting entry from table 'UserToFileState' in getEntryForFile",
                    e);
            e.printStackTrace();
        } finally {
            fields.clear();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<UserToFileState> getEntriesForFile(DBConnectionProvider provider, String fileName) {
        List<UserToFileState> result = new ArrayList<>();

        Map<String, Object> fields = new HashMap<>();
        fields.put("fileName", fileName);

        try {
            result = ((Dao<UserToFileState, Integer>)provider.getOrmDaos()
                    .get(UserToFileState.class)).queryForFieldValuesArgs(fields);
        } catch (SQLException e) {
            Application.LOGGER.log(System.Logger.Level.ERROR,
                    "Exception while getting entry from table 'UserToFileState' in getEntryForFile",
                    e);
            e.printStackTrace();
        } finally {
            fields.clear();
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public void updateEntry(DBConnectionProvider provider) throws SQLException {
        ((Dao<UserToFileState, Integer>)provider.getOrmDaos()
                .get(UserToFileState.class)).createOrUpdate(this);
    }

    @SuppressWarnings("unchecked")
    public void deleteEntry(DBConnectionProvider provider) {
        try {
            ((Dao<UserToFileState, Integer>)provider.getOrmDaos()
                    .get(UserToFileState.class)).delete(this);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPausedAt() {
        return pausedAt;
    }

    public void setPausedAt(Integer pausedAt) {
        this.pausedAt = pausedAt;
    }

    public Long getEntryId() {
        return entryId;
    }
}
