package org.yomirein.sochatserver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
            /*  TODO: NEED TO COUNT AND DELETE:
                    USER FRIENDSHIPS WITH TRUSTKEYS - done using FK
                    USER MESSAGES(just everything.) - done
                    USER CHATS(if group - leave, if dm - delete) - done
                    USER MEDIA - deleting tables but not cleaning files, not complete fully
            */
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 1, 5, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduledExecutorService::shutdown));
    }
}
