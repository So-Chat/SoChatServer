package org.yomirein.sochatserver.users;


import org.yomirein.sochatserver.utils.JwtService;

import lombok.RequiredArgsConstructor;

import org.yomirein.sochatserver.persistance.api.repositories.UserRepository;

@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

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

    public User getUserByToken(String token) {
        String username = JwtService.extractUsername(token);
        return getUser(username);
    }

    public boolean changeProfile(Long userId, String username, String nickname, String description, String avatarId) {
        try{
            return userRepository.updateUser(userId, username, nickname, description, avatarId);
        }catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }



}
