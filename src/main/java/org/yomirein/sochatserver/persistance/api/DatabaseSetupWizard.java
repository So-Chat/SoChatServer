package org.yomirein.sochatserver.persistance.api;

import lombok.Getter;

public abstract class DatabaseSetupWizard {

    @Getter private final DatabaseConfigurator<?> configurator;

    protected DatabaseSetupWizard(DatabaseConfigurator<?> configurator) {
        this.configurator = configurator;
    }

    public abstract void setupDatabase();
}
