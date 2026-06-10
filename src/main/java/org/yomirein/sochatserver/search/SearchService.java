package org.yomirein.sochatserver.search;

import lombok.RequiredArgsConstructor;
import org.yomirein.sochatserver.users.User;
import org.yomirein.sochatserver.users.UserRepository;

import java.util.List;

@RequiredArgsConstructor
public class SearchService  {

    private final UserRepository userRepository;

    public List<User> findByUsername(String username, int offset) {
        return userRepository.searchByUsername(username, offset, offset+20);
    }

}
