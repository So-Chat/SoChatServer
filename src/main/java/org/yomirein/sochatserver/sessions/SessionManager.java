package org.yomirein.sochatserver.sessions;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import org.yomirein.sochatserver.users.User;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Session manager!
public class SessionManager {

    // Two session maps for easier getting some sessions data
    private final Map<Long, Set<Session>> sessionsByUser = new ConcurrentHashMap<>();
    private final Map<ChannelId, Session> sessionByChannel = new ConcurrentHashMap<>();

    // Adding session, for sessionByChannel just putting session within channel(netty) id
    // for sessionsByUser we put session with userId on key
    public void addSession(User user, Channel channel, Session session) {
        session.setChannel(channel);
        sessionByChannel.put(channel.id(), session);
        sessionsByUser
                .computeIfAbsent(user.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    // For remove session we use our sessionByChannel and sessionByUser for full
    // removing on two maps and many if's brrr
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

    // Easy get session by connected session channel id
    public Session getSession(Channel channel) {
        return sessionByChannel.get(channel.id());
    }

    // Check for authentication, it simply just uses Session.isAuthenticated()
    public boolean isAuthenticated(Channel channel) {
        Session s = sessionByChannel.get(channel.id());
        return s != null && s.isAuthenticated();
    }

    // Getting all Sessions that using one Account
    public Set<Session> getUserSessions(User user) {
        return sessionsByUser.getOrDefault(user.getId(), Set.of());
    }
}