package org.yomirein.sochatserver.users;


import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.friendship.FriendshipRepository;
import org.yomirein.sochatserver.common.repos.TrustKeysRepository;

import java.util.List;

@RequiredArgsConstructor
public class UserService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final TrustKeysRepository trustKeysRepository;

    public User getUser(String username) {
        try{
            return userRepository.findByName(username).orElseThrow(() -> new RuntimeException("User not found"));
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUser(Long userId) {
        try{
            return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean changeProfile(Long userId, String username, String nickname, String description) {
        try{
            return userRepository.updateUser(userId, username, nickname, description);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }



}
