package ru.itsjava.dao;

import lombok.AllArgsConstructor;
import ru.itsjava.Exception.SQL_Error;
import ru.itsjava.Exception.UserExit;
import ru.itsjava.Exception.UserNotFoundException;
import ru.itsjava.domain.Message;
import ru.itsjava.utils.Props;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class MessageDaoImpl implements MessageDao {

    private final Props props;

    @Override
    public Message saveMessage(String from, String to_text) {
        try (Connection connection = DriverManager.getConnection(
                props.getValue( "db.url" ),
                props.getValue( "db.login" ),
                props.getValue( "db.password" ) );) {
            PreparedStatement preparedStatement = connection
                    .prepareStatement( "select id cnt from hw412_schema.users where name = ?;" );
            preparedStatement.setString( 1, from );

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            String userId = resultSet.getString( "cnt" );

            if (!to_text.equals( "Exit" )) {
                PreparedStatement preparedStatement01 = connection
                        .prepareStatement( "insert into hw412_schema.messages(message, user_id) value (?, ?);" );
                preparedStatement01.setString( 1, to_text );
                preparedStatement01.setString( 2, userId );

                preparedStatement01.execute();
                return new Message( from, to_text );
            }
            throw new UserExit( "User Exit!!!" );

        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new UserNotFoundException( "User not found!!!" );
    }

    @Override
    public List<Message> printLastMessages() {
        try (Connection connection = DriverManager.getConnection(
                props.getValue( "db.url" ),
                props.getValue( "db.login" ),
                props.getValue( "db.password" ) );) {
            PreparedStatement preparedStatement = connection
                    .prepareStatement( "select count(*) cnt from hw412_schema.messages;" );

            ResultSet resultSet = preparedStatement.executeQuery();
            resultSet.next();

            int countMessages = resultSet.getInt( "cnt" ) - 10;
            String id = Integer.toString( countMessages );

            PreparedStatement preparedStatementMessage = connection
                    .prepareStatement( "SELECT user_id, message FROM hw412_schema.messages ORDER BY id LIMIT 10 offset ?" );
            preparedStatementMessage.setInt( 1, countMessages );
            ResultSet resultSet01 = preparedStatementMessage.executeQuery();

            List<Message> userMessage = new ArrayList<>();
            while (resultSet01.next()) {
                PreparedStatement preparedStatementUser = connection.prepareStatement( "select name from hw412_schema.users where id = ?" );
                String userId = Integer.toString( resultSet01.getInt( "user_id" ) );
                preparedStatementUser.setString( 1, userId );
                ResultSet resultSet02 = preparedStatementUser.executeQuery();
                resultSet02.next();
                Message message = new Message( resultSet02.getString( "name" ), resultSet01.getString( "message" ) );
                userMessage.add( message );
            }
            return userMessage;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new SQL_Error( "SQL_Error" );
    }
}
