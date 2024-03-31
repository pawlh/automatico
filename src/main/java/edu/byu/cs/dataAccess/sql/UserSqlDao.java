package edu.byu.cs.dataAccess.sql;

import edu.byu.cs.dataAccess.DataAccessException;
import edu.byu.cs.dataAccess.UserDao;
import edu.byu.cs.dataAccess.sql.helpers.ColumnDefinition;
import edu.byu.cs.dataAccess.sql.helpers.SqlReader;
import edu.byu.cs.model.User;
import org.eclipse.jgit.annotations.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class UserSqlDao implements UserDao {
    private static final ColumnDefinition[] COLUMN_DEFINITIONS = {
            new ColumnDefinition<User>("net_id", User::netId),
            new ColumnDefinition<User>("canvas_user_id", User::canvasUserId),
            new ColumnDefinition<User>("first_name", User::firstName),
            new ColumnDefinition<User>("last_name", User::lastName),
            new ColumnDefinition<User>("repo_url", User::repoUrl),
            new ColumnDefinition<User>("role", user -> user.role().toString()),
    };
    private static User readUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("net_id"),
                rs.getInt("canvas_user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("repo_url"),
                User.Role.valueOf(rs.getString("role"))
        );
    }

    private final SqlReader<User> sqlReader = new SqlReader<User>(
            "user", COLUMN_DEFINITIONS, UserSqlDao::readUser);


    @Override
    public void insertUser(User user) {
        sqlReader.insertItem(user);
    }

    @Override
    public User getUser(String netId) {
        var results = sqlReader.executeQuery(
                "WHERE net_id = ?",
                ps -> ps.setString(1, netId));
        return results.isEmpty() ? null : results.iterator().next();
    }

    @Override
    public void setFirstName(String netId, String firstName) {
        setFieldValue(netId, "first_name", firstName);
    }

    @Override
    public void setLastName(String netId, String lastName) {
        setFieldValue(netId, "last_name", lastName);
    }

    @Override
    public void setRepoUrl(String netId, String repoUrl) {
        setFieldValue(netId, "repo_url", repoUrl);
    }

    @Override
    public void setRole(String netId, User.Role role) {
        setFieldValue(netId, "role", role.toString());
    }

    @Override
    public void setCanvasUserId(String netId, int canvasUserId) {
        setFieldValue(netId, "canvas_user_id", canvasUserId);
    }

    private void setFieldValue(@NonNull String netId, @NonNull String columnName, @NonNull Object columnValue) {
        try (var connection = SqlDb.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                    """
                            UPDATE user
                            SET %s = ?
                            WHERE net_id = ?
                            """.formatted(columnName))) {
            sqlReader.setValue(statement, 1, columnValue);
            statement.setString(2, netId);
            statement.executeUpdate();
        } catch (Exception e) {
            throw new DataAccessException("Error updating '%s' field for user".formatted(columnName), e);
        }
    }

    @Override
    public Collection<User> getUsers() {
        return sqlReader.executeQuery("");
    }

    @Override
    public boolean repoUrlClaimed(String repoUrl) {
        var results = sqlReader.executeQuery(
                "WHERE repo_url = ?",
                ps -> ps.setString(1, repoUrl));
        return !results.isEmpty();
    }

}
