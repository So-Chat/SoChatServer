package org.yomirein.sochatserver.persistance.postgresql;

import org.yomirein.sochatserver.persistance.api.DatabaseSetupWizard;


public final class PostgresDatabaseSetupWizard extends DatabaseSetupWizard {

    public PostgresDatabaseSetupWizard() {
        super(new PostgresDatabaseConfigurator());
    }

    @Override
    public void setupDatabase() {
        //getConfigurator().initializeDatabase();
        //getConfigurator().initializeSchema();
    }
}
