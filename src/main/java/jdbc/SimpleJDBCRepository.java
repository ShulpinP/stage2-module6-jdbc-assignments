package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {
    private final CustomDataSource dataSource = CustomDataSource.getInstance();

    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final String CREATE_USER_SQL = "insert into myusers(firstname, lastname, age) values (?, ?, ?);";
    private static final String UPDATE_USER_SQL = "update myusers set firstname=?, lastname=?, age=? where id = ? ";
    private static final String DELETE_USER = "delete from myusers where id = ?";
    private static final String FIND_USER_BY_ID_SQL = "select * from myusers where id = ?";
    private static final String FIND_USER_BY_NAME_SQL = "select * from myusers where firstname like CONCAT('%', ?, '%')";
    private static final String FIND_ALL_USER_SQL = "select * from myusers";


    public Long createUser(User user) {
        Long id = null;
        try (var conn = dataSource.getConnection();
             var statement = conn.prepareStatement(CREATE_USER_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setObject(1, user.getFirstName());
            statement.setObject(2, user.getLastName());
            statement.setObject(3, user.getAge());
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                id = generatedKeys.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return id;
    }

    public User findUserById(Long userId) {
        User user = null;
        try (var conn = dataSource.getConnection();
             var statement = conn.prepareStatement(FIND_USER_BY_ID_SQL)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = map(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public User findUserByName(String userName) {
        User user = null;
        try (var conn = dataSource.getConnection();
             var statement = conn.prepareStatement(FIND_USER_BY_NAME_SQL)) {
            statement.setString(1, userName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                user = map(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    public List<User> findAllUser() {
        List<User> users = new ArrayList<>();
        try (var conn = dataSource.getConnection();
             var statement = conn.prepareStatement(FIND_ALL_USER_SQL)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(map(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public User updateUser(User user) {
        try (var conn = dataSource.getConnection();
             var statement = conn.prepareStatement(UPDATE_USER_SQL)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setInt(3, user.getAge());
            statement.setLong(4, user.getId());
            if (statement.executeUpdate() != 0) {
                return findUserById(user.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void deleteUser(Long userId) {
        try (var conn = dataSource.getConnection();
             var statement = conn.prepareStatement(DELETE_USER)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("firstname"))
                .lastName(rs.getString("lastname"))
                .age(rs.getInt("age"))
                .build();
    }
}