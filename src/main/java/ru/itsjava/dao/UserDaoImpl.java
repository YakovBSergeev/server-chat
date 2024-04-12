package ru.itsjava.dao;

import lombok.AllArgsConstructor;
import org.apache.log4j.Logger;
import ru.itsjava.Exception.UserNotFoundException;
import ru.itsjava.Exception.UserRegistrationException;
import ru.itsjava.domain.User;
import ru.itsjava.utils.Props;

import java.sql.*;

@AllArgsConstructor
public class UserDaoImpl implements UserDao {

    private final Props props;
    private static final Logger log = Logger.getLogger( UserDaoImpl.class );

    /**
     * Метод ищет пользователя в БД.
     *
     * @param name     имя пользователя
     * @param password пароль пользователя
     * @return возвращает new User( name, password ) или
     * throw new UserNotFoundException( "User not found!!!" )
     */

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
            log.info( userCount );

            if (userCount == 1) {
                return new User( name, password );
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        throw new UserNotFoundException( "User not found!!!" );
    }

    /**
     * Метод проверяет есть ли пользователь в БД
     * и добавляет нового пользователя в БД.     *
     *
     * @param name     имя пользователя
     * @param password пароль пользователя
     * @return возвращает new User( name, password ) или
     * throw new UserRegistrationException( "This user is registration already!!!" )
     */

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

    @Override
    public boolean nameIs(String name) {
        try (Connection connection = DriverManager.getConnection(
                props.getValue( "db.url" ),
                props.getValue( "db.login" ),
                props.getValue( "db.password" ) );) {

            PreparedStatement preparedStatement = connection
                    .prepareStatement( "select count(*) cnt from hw412_schema.users where name = ?;" );
            preparedStatement.setString( 1, name );

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            int userCount = resultSet.getInt( "cnt" );
            log.info( userCount );

            if (userCount == 1) {
                return true;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

}



