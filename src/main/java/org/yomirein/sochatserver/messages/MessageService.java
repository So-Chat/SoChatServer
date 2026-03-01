package org.yomirein.sochatserver.messages;


import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.chats.ChatRepository;

import java.util.List;

@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message addMessage(long senderId, long chatId, String content, Long replyMessageId){
        try {
            // TODO: make checking for chat existence
            // TODO: check if user in chat

            Message message = new Message();

            message.setChatId(chatId);
            message.setSenderId(senderId);
            message.setReplyMessageId(replyMessageId);

            message.setContent(content);

            // TODO: make getting chats current Key Version for setting(its 0 for now)
            message.setKeyVersion(0);

            return messageRepository.save(message);

        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    public Message getMessage(long messageId){
        try{
            return messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    public List<Message> getChatMessages(long chatId){
        try{
            return messageRepository.findByChatId(chatId);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Message> getRecentMessages(long chatId, int limit){
        try{
            if (limit == 20) return messageRepository.findTop20ByChatIdOrderByTimestampDesc(chatId);
            return messageRepository.findByChatIdOrderByTimestampDesc(chatId, 0, limit);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Message editMessage(long messageId, String content){
        try{
            Message message = getMessage(messageId);
            message.setContent(content);

            return messageRepository.update(message);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteMessage(long messageId){
        try{
            Message message = getMessage(messageId);
            return messageRepository.deleteById(message.getId());
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
