package org.yomirein.sochatserver.friendship;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.yomirein.sochatserver.users.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    private Long id;
    private User user;
    private User friend;
    private FriendshipStatus status;
}

