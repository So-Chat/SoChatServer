package org.yomirein.sochatserver.messages;


import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelHandlerContext;
import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.chats.Chat;
import org.yomirein.sochatserver.chats.ChatService;
import org.yomirein.sochatserver.common.models.MessagePacket;
import org.yomirein.sochatserver.sessions.SessionManager;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.utils.JsonConfig;

import java.util.List;

import static org.yomirein.sochatserver.utils.MessageSender.notifyUser;
import static org.yomirein.sochatserver.utils.MessageSender.sendError;

@RequiredArgsConstructor
public class MessageHandler {

    private final MessageService messageService;
    private final ChatService chatService;

    private final MessageRepository messageRepository;

    private final SessionManager sessionManager;


    public void sendMessage(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            String content = messagePacket.getPayload().get("content").asText();
            long chatId = messagePacket.getPayload().get("chatId").asLong();
            JsonNode replyNode = messagePacket.getPayload().get("replyMessageId");

            Long replyMessageId = null;

            if (replyNode != null && !replyNode.isNull()) {
                replyMessageId = replyNode.asLong();
            }


            List<User> chatMembers = new java.util.ArrayList<>(chatService.getChat(chatId).
                    getParticipantsWithKeys().keySet().stream().toList());
            chatMembers.removeIf(user -> user.getId() == userId);


            Message message = messageService.addMessage(userId, chatId, content, replyMessageId);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Message sent successfully")
                    .put("message", JsonConfig.MAPPER.writeValueAsString(message))
                    .build();

            MessagePacket requestInformation = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("server_message", "You got message")
                    .put("message", JsonConfig.MAPPER.writeValueAsString(message))
                    .build();

            channelHandlerContext.channel().writeAndFlush(answerPacket);
            for (User member : chatMembers) {
                notifyUser(member, requestInformation, sessionManager);
            }
        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void getMessage(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId) {
        try {
            long messageId = messagePacket.getPayload().get("messageId").asLong();
            Message message = messageService.getMessage(messageId);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Got messages successfully")
                    .put("message", JsonConfig.MAPPER.writeValueAsString(message))
                    .build();

            channelHandlerContext.writeAndFlush(answerPacket);

        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void getRecentMessages(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId){
        try {
            int limit = messagePacket.getPayload().get("limit").asInt();
            long chatId = messagePacket.getPayload().get("chatId").asLong();

            List<Message> messages = messageService.getRecentMessages(chatId, limit);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Got messages successfully")
                    .put("messages", JsonConfig.MAPPER.writeValueAsString(messages))
                    .build();


            channelHandlerContext.channel().writeAndFlush(answerPacket);
        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void editMessage(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId){
        try {
            String content = messagePacket.getPayload().get("content").asText();
            long messageId = messagePacket.getPayload().get("id").asLong();



            Message oldMessage = messageService.getMessage(messageId);

            List<User> chatMembers = chatService.getChat(oldMessage.getChatId()).
                    getParticipantsWithKeys().keySet().stream().toList();
            chatMembers.removeIf(user -> user.getId() == userId);

            Message message = messageService.editMessage(messageId, content);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Message edited successfully")
                    .put("message", JsonConfig.MAPPER.writeValueAsString(message))
                    .build();

            MessagePacket requestInformation = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("server_message", "User edited message")
                    .put("message", JsonConfig.MAPPER.writeValueAsString(message))
                    .build();

            channelHandlerContext.writeAndFlush(answerPacket);
            for (User member : chatMembers) {
                notifyUser(member, requestInformation, sessionManager);
            }
        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(ChannelHandlerContext channelHandlerContext, MessagePacket messagePacket, Long userId){
        try {
            long messageId = messagePacket.getPayload().get("id").asLong();
            Message message = messageService.getMessage(messageId);

            List<User> chatMembers = chatService.getChat(message.getChatId()).
                    getParticipantsWithKeys().keySet().stream().toList();
            chatMembers.removeIf(user -> user.getId() == userId);


            boolean result = messageService.deleteMessage(messageId);

            MessagePacket answerPacket = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("success", true)
                    .put("requestId", messagePacket.getPayload().get("requestId").asText())
                    .put("server_message", "Message deleted successfully")
                    .put("message", JsonConfig.MAPPER.writeValueAsString(message))
                    .build();

            MessagePacket requestInformation = new MessagePacket.Builder()
                    .type(messagePacket.getType())
                    .put("server_message", "User deleted message")
                    .put("message", JsonConfig.MAPPER.writeValueAsString(message))
                    .build();

            channelHandlerContext.writeAndFlush(answerPacket);
            for (User member : chatMembers) {
                notifyUser(member, requestInformation, sessionManager);
            }
        } catch (Exception e) {
            System.out.println(e);
            sendError(channelHandlerContext, messagePacket, e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
