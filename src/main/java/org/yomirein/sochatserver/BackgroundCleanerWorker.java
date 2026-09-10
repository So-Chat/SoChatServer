package org.yomirein.sochatserver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.yomirein.sochatserver.media.MediaService;
import org.yomirein.sochatserver.users.UserService;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class BackgroundCleanerWorker {

    private final HikariDataSource dataSource;

    private final UserService userService;
    private final MediaService mediaService;

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
            /*  TODO: NEED TO COUNT AND DELETE:
                    USER FRIENDSHIPS
                    USER MESSAGES(just everything.)
                    USER CHATS(if group - leave, if dm - delete)
                    USER MEDIA

                    MEDIA ITSELF IF IT DOES NOT BELONGS TO SOMETHING
            */
        };
        scheduledExecutorService.scheduleAtFixedRate(task, 1, 20, TimeUnit.MINUTES);

        Runtime.getRuntime().addShutdownHook(new Thread(scheduledExecutorService::shutdown));
    }
}
