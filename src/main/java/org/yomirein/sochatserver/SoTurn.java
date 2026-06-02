package org.yomirein.sochatserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yomirein.sochatserver.utils.JwtService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class SoTurn {

    public static String osName = System.getProperty("os.name");
    public static String osVersion = System.getProperty("os.version");
    public static String osArch = System.getProperty("os.arch");

    Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static Process turnProcess;

    public void run() throws IOException, InterruptedException {
        String executableName = checkTurn();
        Thread thread = configureTurnThread(executableName);

        LOGGER.info("Starting SoTurn...");

        thread.start();
    }

    public String checkTurn() throws IOException, InterruptedException {
        String soTurnName = "soturn-" + osName.toLowerCase().split(" ")[0] + "-" + "x86-64";

        if (osName.contains("Windows")) { soTurnName += ".exe"; }

        if (Paths.get(soTurnName).toFile().exists()) {
            LOGGER.info("SoTurn exists, continuing...");
        } else {
            LOGGER.info("No SoTurn found, downloading...");

            String fileUrl = "https://github.com/So-Chat/SoTurn/releases/download/v0.1-MVP/" + soTurnName;
            Path savePath = Paths.get(soTurnName).toAbsolutePath();

            HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fileUrl))
                    .header("Accept", "application/octet-stream")
                    .build();

            HttpResponse<Path> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofFile(savePath)
            );

            LOGGER.info("SoTurn download complete!");
        }
        return soTurnName;
    }

    // TODO: NEEDS TO MAKE IT TAKING ARGS FROM CONFIGS
    public Thread configureTurnThread(String executableName) throws IOException, InterruptedException {
        LOGGER.info("SoTurn configuring thread");
        Thread turnThread = new Thread(() -> {
            ProcessBuilder pb = new ProcessBuilder(
                    Paths.get(executableName).toAbsolutePath().toString(),
                    // TODO: MOVE TO THE CONFIG
                    "--public-ip", "0.0.0.0",
                    "--realm", "0.0.0.0",
                    "--jwt", JwtService.SECRET
            );
            pb.redirectErrorStream(true);

            Logger turnThreadLogger = LoggerFactory.getLogger(pb.getClass());

            try {
                turnProcess = pb.start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(turnProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        turnThreadLogger.info(line);
                    }
                }
                turnProcess.waitFor();

            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (turnProcess != null && turnProcess.isAlive()) {
                turnProcess.destroy();

                try {
                    if (!turnProcess.waitFor(5, TimeUnit.SECONDS)) {
                        turnProcess.destroyForcibly();
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }));

        return turnThread;
    }


}
