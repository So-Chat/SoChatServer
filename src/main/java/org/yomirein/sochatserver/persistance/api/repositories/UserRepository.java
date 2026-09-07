package org.yomirein.sochatserver.persistance.api.repositories;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.persistance.api.Repository;
import org.yomirein.sochatserver.users.User;

public abstract class UserRepository extends Repository  {

    protected UserRepository(Connection connection) {
        super(connection);
    }

    public abstract User saveUser(User user);

    public abstract Optional<User> findByName(String username);

    public abstract Optional<User> findById(Long id);

    public abstract List<User> searchByUsername(String username, int offset, int limit);

    public abstract boolean updateUser(Long id, String username, String nickname, String description);
}
