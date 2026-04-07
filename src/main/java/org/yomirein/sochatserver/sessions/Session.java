package org.yomirein.sochatserver.sessions;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.yomirein.sochatserver.users.User;

// Session object
// It has user token, user and their channel

@Getter @Setter
@AllArgsConstructor
public class Session {
    private String token;
    private User user;
    private Channel channel;

    // Check for user authentication(session has user only after authorization)
    public boolean isAuthenticated() {
        return user != null;
    }
}