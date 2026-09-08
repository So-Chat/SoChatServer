package org.yomirein.sochatserver.persistance.api.repositories;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.persistance.api.Repository;

public abstract class TrustKeysRepository extends Repository  {

    protected TrustKeysRepository(HikariDataSource dataSource) {
        super(dataSource);
    }

    public abstract List<String> getEncryptedKeysByUserId(long userId);

    public abstract Optional<String> getEncryptedKeyByUserIds(long fnOwnerId, long userId);

    public abstract boolean addKeyToUser(long userId, long ownerId, String key);

    public abstract boolean removeKeyFromUser(long userId, long ownerId);

}
