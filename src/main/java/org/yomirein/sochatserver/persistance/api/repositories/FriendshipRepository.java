package org.yomirein.sochatserver.persistance.api.repositories;

import org.yomirein.sochatserver.users.User;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.friendship.Friendship;
import org.yomirein.sochatserver.friendship.FriendshipStatus;

import org.yomirein.sochatserver.persistance.api.Repository;

public abstract class FriendshipRepository extends Repository  {

    protected FriendshipRepository(HikariDataSource dataSource) {
        super(dataSource);
    }

    public abstract List<Friendship> findByUserOrFriend(User user, User friend);

    public abstract Optional<Friendship> findByUserAndFriend(User user, User friend);

    public abstract Optional<Friendship> findById(Long id);

    public abstract List<Friendship> findByUserAndStatus(User user, FriendshipStatus status);

    public abstract List<Friendship> findByFriendAndStatus(User friend, FriendshipStatus status);

    public abstract Friendship saveOrUpdate(Friendship f);

    public abstract boolean deleteById(long friendshipId);
}
