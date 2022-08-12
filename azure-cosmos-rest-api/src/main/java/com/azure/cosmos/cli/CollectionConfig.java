package com.azure.cosmos.cli;

import picocli.CommandLine;

@CommandLine.Command(
        name = "pkranges")
public class CollectionConfig extends Config {
    @CommandLine.Option(names = {"-d", "--database"}, description = "Cosmos Database Name", required = true, paramLabel = "<Database Name>")
    private String database;
    @CommandLine.Option(names = {"-c", "--collection"}, description = "Cosmos collection name", required = true, paramLabel = "<Collection Name>")
    private String collection;

    public String getDatabase() {
        return database;
    }
    public String getCollection() {
        return collection;
    }

}
