package org.yomirein.sochatserver.sessions;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.yomirein.sochatserver.users.User;

@Getter @Setter
@AllArgsConstructor
public class Session {
    private String token;
    private User user;
    private Channel channel;

    public boolean isAuthenticated() {
        return user != null;
    }
}