package org.yomirein.sochatserver.persistance.api.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.chats.Participant;
import org.yomirein.sochatserver.chats.SenderKey;
import org.yomirein.sochatserver.persistance.api.Repository;

public abstract class ChatRepository extends Repository {

    protected ChatRepository(HikariDataSource dataSource) {
        super(dataSource);
    }

    public abstract Optional<Chat> findById(Long id);

    public abstract Optional<Chat> findChatByContainingMessageId(long messageId);

    public abstract Optional<Chat> findByIdWithParticipants(Long chatId);

    public abstract List<Chat> findAllByParticipantId(Long userId);

    public abstract Optional<Chat> findPrivateChatBetween(Long userId1, Long userId2);

    public abstract List<Participant> loadParticipants(Long chatId);

    public abstract boolean addParticipant(Participant participant);

    public abstract boolean removeParticipant(long chat_id, long user_id);

    public abstract boolean removeSenderKeys(long chatId, long userId);

    public abstract Optional<Participant> getParticipantByUserIdAndChatId(Long userId, Long chatId);

    public abstract List<User> getUsersByChatId(Long chatId);

    public abstract boolean addSenderKey(SenderKey senderKey);

    public abstract int getCurrentKeyVersion(long chatId);

    public abstract Optional<SenderKey> findLastSenderKeyByChatAndUser(long chatId, long userId);

    public abstract List<SenderKey> findAllSenderKeyByChatAndId(long chatId, long userId);

    public abstract List<SenderKey> findAllSenderKeyByChat(long chatId);

    public abstract Chat save(Chat chat);

    public abstract boolean deleteById(long chatId);
}
