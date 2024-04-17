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

    /**
     * Медот сохраняет сообщение в БД от зарегистрированного пользователя
     * По имени пользователя выбираем его id  из БД hw412_schema.users.
     * Дбавляем в БД hw412_schema.messages с параметрами (тект сообщения, id)
     *
     * @param from    имя пользователя
     * @param to_text текст сообщения
     * @return возвращаем сообщение (имя, текст сообщения)
     * Если текст равен Exit, пробрасываем ошибку "User Exit!!!".
     */
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

            if (!to_text.equals( "Exit" ) ) {
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

    /**
     * Метод возвращает коллекцию List<Message> состоящую из последних 10 сообщений БД hw412_schema.messages
     * вида имя/текст сообщения
     *
     * @return ArrayList ( nameFromId.getString( "name" ), idAndMessage.getString( "message" ) );
     */

    @Override
    public List<Message> printLastMessages() {
        try (Connection connection = DriverManager.getConnection(
                props.getValue( "db.url" ),
                props.getValue( "db.login" ),
                props.getValue( "db.password" ) );) {
            PreparedStatement preparedStatement = connection
                    .prepareStatement( "select count(*) cnt from hw412_schema.messages;" );

            ResultSet countMessage = preparedStatement.executeQuery();
            countMessage.next();

            int countMessages = countMessage.getInt( "cnt" ) - 10;

            PreparedStatement preparedStatementMessage = connection
                    .prepareStatement( "SELECT user_id, message FROM hw412_schema.messages ORDER BY id LIMIT 10 offset ?" );
            preparedStatementMessage.setInt( 1, countMessages );
            ResultSet idAndMessage = preparedStatementMessage.executeQuery();

            List<Message> userMessage = new ArrayList<>();
            while (idAndMessage.next()) {
                PreparedStatement preparedStatementUser = connection.prepareStatement( "select name from hw412_schema.users where id = ?" );
                String userId = Integer.toString( idAndMessage.getInt( "user_id" ) );
                preparedStatementUser.setString( 1, userId );
                ResultSet nameFromId = preparedStatementUser.executeQuery();
                nameFromId.next();
                Message message = new Message( nameFromId.getString( "name" ), idAndMessage.getString( "message" ) );
                userMessage.add( message );
            }
            return userMessage;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new SQL_Error( "SQL_Error" );
    }
}
