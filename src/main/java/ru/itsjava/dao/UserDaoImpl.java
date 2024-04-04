package ru.itsjava.dao;

import lombok.AllArgsConstructor;
import ru.itsjava.Exception.UserNotFoundException;
import ru.itsjava.Exception.UserRegistrationException;
import ru.itsjava.domain.User;
import ru.itsjava.utils.Props;

import java.sql.*;

@AllArgsConstructor
public class UserDaoImpl implements UserDao {
    private final Props props;


    @Override
    public User findByNameAndPassword(String name, String password) {
        try (Connection connection = DriverManager.getConnection(
                props.getValue( "db.url" ),
                props.getValue( "db.login" ),
                props.getValue( "db.password" ) );) {

            PreparedStatement preparedStatement = connection
                    .prepareStatement( "select count(*) cnt from hw412_schema.users where name = ? and password = ?;" );
            preparedStatement.setString( 1, name );
            preparedStatement.setString( 2, password );

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            int userCount = resultSet.getInt( "cnt" );

            if (userCount == 1) {
                return new User( name, password );
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
//        return new User( null, null );

        throw new UserNotFoundException( "User not found!!!" );
    }

    @Override
    public User addUser(String name, String password) {
        try (Connection connection = DriverManager.getConnection(
                props.getValue( "db.url" ),
                props.getValue( "db.login" ),
                props.getValue( "db.password" ) );) {
            PreparedStatement prepared = connection
                    .prepareStatement( "select count(*) cnt from hw412_schema.users where name = ? and password = ?;" );
            prepared.setString( 1, name );
            prepared.setString( 2, password );

            ResultSet resultSet = prepared.executeQuery();
            resultSet.next();
            int userCount = resultSet.getInt( "cnt" );
            if (userCount != 1) {
                PreparedStatement preparedStatement = connection
                        .prepareStatement( "insert into hw412_schema.users(name, password) value (?, ?);" );
                preparedStatement.setString( 1, name );
                preparedStatement.setString( 2, password );

                preparedStatement.execute();
                return new User( name, password );
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        throw new UserRegistrationException( "This user is registration already!!!" );
    }

}

