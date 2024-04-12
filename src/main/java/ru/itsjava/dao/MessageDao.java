package ru.itsjava.dao;

import ru.itsjava.domain.Message;

import java.util.List;

public interface MessageDao {
    Message saveMessage(String from, String to_text);

    List<Message> printLastMessages();
}
