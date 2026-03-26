package org.yomirein.sochatserver.messages;


import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.chats.ChatRepository;
import org.yomirein.sochatserver.chats.Participant;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    public Message addMessage(long senderId, long chatId, String content, Long replyMessageId, int keyVersion){
        try {
            // TODO: make checking for chat existence
            // TODO: check if user in chat

            Message message = new Message();

            message.setChatId(chatId);
            message.setSenderId(senderId);
            message.setReplyMessageId(replyMessageId);

            message.setContent(content);

            // TODO: make getting chats current Key Version for setting(its 0 for now)
            message.setKeyVersion(keyVersion);

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

    public Participant setLastReadMessage(Participant participant){
        try {
            return messageRepository.setReadLastMessage(participant);
        } catch (RuntimeException e){
            throw new RuntimeException(e);
        }
    }

    public List<Message> getRecentMessages(long chatId, int offset){
        try{
            return messageRepository.findByChatIdOrderByTimestampDesc(chatId, offset, 20);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public Message getLastMessage(long chatId){
        try {
            Optional<Message> messageOptional = messageRepository.findLastById(chatId);
            return messageOptional.orElse(null);
        } catch (RuntimeException e) {
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
