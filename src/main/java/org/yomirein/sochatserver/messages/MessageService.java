package org.yomirein.sochatserver.messages;


import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.chats.Participant;
import org.yomirein.sochatserver.media.MediaService;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    private final MediaService mediaService;

    // Adding message to Database with arguments
    public Message addMessage(long senderId, long chatId, String content, Long replyMessageId, int keyVersion, List<String> mediaIds){
        try {
            Message message = new Message();

            message.setChatId(chatId);
            message.setSenderId(senderId);
            message.setReplyMessageId(replyMessageId);

            message.setContent(content);

            message.setKeyVersion(keyVersion);

            Message newMessage = messageRepository.save(message);

            for (String mediaId : mediaIds){
                mediaService.attachMessage(mediaId, newMessage.getId());
            }

            return newMessage;

        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    // Get messages by id
    public Message getMessage(long messageId){
        try{
            return messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }


    // Get ALL messages by id (didn't used because it will create load on big chats)
    public List<Message> getChatMessages(long chatId){
        try{
            return messageRepository.findByChatId(chatId);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    // Setting last read message by participant
    public Participant setLastReadMessage(Participant participant){
        try {
            return messageRepository.setReadLastMessage(participant);
        } catch (RuntimeException e){
            throw new RuntimeException(e);
        }
    }

    // Get last 20 messages from chat using chat id and it's offset
    public List<Message> getRecentMessages(long chatId, int offset){
        try{
            return messageRepository.findByChatIdOrderByTimestampDesc(chatId, offset, 20);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    // Get last message by just chat id
    public Message getLastMessage(long chatId){
        try {
            Optional<Message> messageOptional = messageRepository.findLastById(chatId);
            return messageOptional.orElse(null);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    // Edit message with new content
    public Message editMessage(long messageId, String content){
        try{
            Message message = getMessage(messageId);
            message.setContent(content);

            return messageRepository.update(message);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    // Delete message by id
    public boolean deleteMessage(long messageId){
        try{
            Message message = getMessage(messageId);
            return messageRepository.deleteById(message.getId());
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

}
