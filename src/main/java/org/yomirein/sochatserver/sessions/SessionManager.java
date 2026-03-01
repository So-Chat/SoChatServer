package org.yomirein.sochatserver.sessions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.yomirein.sochatserver.users.User;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<Long, Set<Session>> sessionsByUser = new ConcurrentHashMap<>();
    private final Map<ChannelId, Session> sessionByChannel = new ConcurrentHashMap<>();

    public void addSession(User user, Channel channel, Session session) {
        session.setChannel(channel);
        sessionByChannel.put(channel.id(), session);
        sessionsByUser
                .computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    public void removeSession(Channel channel) {
        Session session = sessionByChannel.remove(channel.id());
        if (session != null) {
            Set<Session> set = sessionsByUser.get(session.getUser().getId());
            if (set != null) {
                set.remove(session);
                if (set.isEmpty()) {
                    sessionsByUser.remove(session.getUser().getId());
                }
            }
        }
    }

    public Session getSession(Channel channel) {
        return sessionByChannel.get(channel.id());
    }

    public boolean isAuthenticated(Channel channel) {
        Session s = sessionByChannel.get(channel.id());
        return s != null && s.isAuthenticated();
    }

    public Set<Session> getUserSessions(User user) {
        return sessionsByUser.getOrDefault(user.getId(), Set.of());
    }
}