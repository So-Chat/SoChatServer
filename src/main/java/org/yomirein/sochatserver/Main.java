package org.yomirein.sochatserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.yomirein.sochatserver.persistance.postgresql.PostgresDatabaseSetupWizard;
import org.yomirein.sochatserver.persistance.sqlite.SQLiteDatabaseSetupWizard;
import org.yomirein.sochatserver.utils.ConfigReader;
import org.yomirein.sochatserver.utils.InputReader;

import org.yomirein.sochatserver.persistance.api.Database;

public class Main {

    public static String osName = System.getProperty("os.name");
    public static String osVersion = System.getProperty("os.version");
    public static String osArch = System.getProperty("os.arch");

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        //
        // TODO: Check compliance with the tables
        //

        LOGGER.info("Running on OS: " + osName + " " + osVersion + " " + osArch);



        try {
            Database database = databaseOptionConfigurator();

            SoChat soChat = new SoChat();
            SoTurn soTurn = new SoTurn();

            soTurn.run();
            soChat.run(database);
        } catch (Exception e){
            LOGGER.error("Error starting SoChat", e);
        }
    }

    private static Database databaseOptionConfigurator() {
        Map<String, String> config = ConfigReader.getConfig();
        if (config.containsKey("db.type")){
            String dbType = config.get("db.type");
            if (dbType.equals("sqlite")) {
                return new SQLiteDatabaseSetupWizard().setupDatabase();
            } else if (dbType.equals("postgres")) {
                return new PostgresDatabaseSetupWizard().setupDatabase();
            } else {
                LOGGER.error("Invalid database type specified");
                System.exit(1);
                return null;
            }
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Choose database: \n 1 - sqlite(default) \n 2 - postgresql \nCHOOSE ANYTHING ONLY IF YOU KNOW WHAT YOU ARE DOING");
            String type = InputReader.readLine(in, "1");
            if (type.equals("1")) {
                return new SQLiteDatabaseSetupWizard().setupDatabase();
            } else if (type.equals("2")) {
                return new PostgresDatabaseSetupWizard().setupDatabase();
            } else {
                LOGGER.error("Invalid database type specified");
                System.exit(1);
                return null;
            }
        }
    }
}
