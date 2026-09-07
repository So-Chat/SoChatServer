package org.yomirein.sochatserver.persistance.api.repositories;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

import org.yomirein.sochatserver.media.Media;

import org.yomirein.sochatserver.persistance.api.Repository;

public abstract class MediaRepository extends Repository  {

    protected MediaRepository(Connection connection) {
        super(connection);
    }

    public abstract Optional<Media> findById(String id);

    public abstract boolean deleteById(String id);

    public abstract List<Media> findAttachedMessage(long messageId);

    public abstract boolean update(String mediaId, Long message_id, Integer width, Integer height, Integer length);

    public abstract Media save(String mediaId, long userId, String mimeType, String fileName, long fileSize, String nonce);
}
