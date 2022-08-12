package com.azure.cosmos.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "colls")
public class DatabaseConfig extends Config {
    @CommandLine.Option(names = {"-d", "--database"}, description = "Cosmos Database Name", required = true, paramLabel = "<Database Name>")
    private String database;

    public String getDatabase() {
        return database;
    }

}
