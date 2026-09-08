package org.yomirein.sochatserver.persistance.api.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.chats.Participant;
import org.yomirein.sochatserver.messages.Message;

import org.yomirein.sochatserver.persistance.api.Repository;

public abstract class MessageRepository extends Repository  {

    protected MessageRepository(HikariDataSource dataSource) {
        super(dataSource);
    }

    public abstract Optional<Message> findById(Long id);

    public abstract Optional<Message> findLastById(Long id);

    public abstract List<Message> findByChatId(Long chatId);

    public abstract boolean deleteById(Long messageId);

    public abstract List<Message> findTop20ByChatIdOrderByTimestampDesc(Long chatId);

    public abstract List<Message> findByChatIdOrderByTimestampDesc(Long chatId, int offset, int limit);

    public abstract List<Message> findUnreadByChatIdOrderByTimestampDesc(Long chatId);

    public abstract Participant setReadLastMessage(Participant participant);

    public abstract Message save(Message m);

    public abstract Message update(Message m);
}
