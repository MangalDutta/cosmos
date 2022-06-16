package com.azure.cosmos.sample.common;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;

public class ConnectionAsync {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;

    public void setDatabase(CosmosAsyncDatabase database) {
        this.database = database;
    }

    public void setClient(CosmosAsyncClient client) {
        this.client = client;
    }

    public void setContainer(CosmosAsyncContainer container) {
        this.container = container;
    }

    public CosmosAsyncClient getClient() {
        return client;
    }

    public CosmosAsyncContainer getContainer() {
        return container;
    }

    public CosmosAsyncDatabase getDatabase() {
        return database;
    }

    public void close() {
        client.close();
    }
}
