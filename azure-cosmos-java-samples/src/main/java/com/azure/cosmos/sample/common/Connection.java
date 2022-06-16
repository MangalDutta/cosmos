package com.azure.cosmos.sample.common;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;

public class Connection {
    private CosmosClient client;
    private CosmosDatabase database;
    private CosmosContainer container;

    public void setDatabase(CosmosDatabase database) {
        this.database = database;
    }

    public void setClient(CosmosClient client) {
        this.client = client;
    }

    public void setContainer(CosmosContainer container) {
        this.container = container;
    }

    public CosmosClient getClient() {
        return client;
    }

    public CosmosContainer getContainer() {
        return container;
    }

    public CosmosDatabase getDatabase() {
        return database;
    }

    public void close() {
        client.close();
    }
}
