package org.yomirein.sochatserver.chats;

import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ChatService {

    private final UserService userService;
    private final ChatRepository chatRepository;

    public Chat getChatByUsers(long userId1, long userId2){
        try{
            return chatRepository.findPrivateChatBetween(userId1, userId2).orElseThrow(() -> new RuntimeException("Chat not found"));
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Chat> getUserChats(long userId){
        try {
            return chatRepository.findAllByParticipantId(userId);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Chat getChat(long chatId){
        try{
            return chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Chat createChat(Long fromUserId, Long toUserId, String fromEncryptedKey, String toEncryptedKey) {
        try {
            User from = userService.getUser(fromUserId);
            User to = userService.getUser(toUserId);

            Chat chat = new Chat();
            Map<User, String> chatParticipants = new HashMap<>();
            chatParticipants.put(from, fromEncryptedKey);
            chatParticipants.put(to, toEncryptedKey);
            chat.setParticipantsWithKeys(chatParticipants);

            return chatRepository.save(chat);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteChat(Chat chat){
        try {
            return chatRepository.deleteById(chat.getId());
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
