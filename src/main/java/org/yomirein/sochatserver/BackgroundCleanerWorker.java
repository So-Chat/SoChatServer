package org.yomirein.sochatserver;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.media.MediaService;
import org.yomirein.sochatserver.persistance.api.repositories.ChatRepository;
import org.yomirein.sochatserver.persistance.api.repositories.MediaRepository;
import org.yomirein.sochatserver.persistance.api.repositories.MessageRepository;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BackgroundCleanerWorker {

    private final HikariDataSource dataSource;

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final MediaRepository mediaRepository;

    private final MediaService mediaService;

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundCleanerWorker.class);

    private boolean isDatabaseBusy() {
        HikariPoolMXBean pool = dataSource.getHikariPoolMXBean();
        return pool.getThreadsAwaitingConnection() > 0;
    }

    public void run() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);

        Runnable task = () -> {
            if (isDatabaseBusy()) {
                return;
            }
            chatRepository.deleteOrphaned();
            messageRepository.deleteOrphaned();
            mediaRepository.deleteOrphaned();
            // Cleaner does not clean IO media files
            try {
                mediaService.cleanIoOprphanedMediaFiles();
            } catch (IOException e) {
                LOGGER.error("Error while cleaning IO media files: ", e);
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 1, 5, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduledExecutorService::shutdown));
    }

}
