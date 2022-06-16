package com.azure.cosmos.sample.sync;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.cosmos.sample.common.Employee;

import java.util.List;

/**
 * Samples for the transactional test
 */
public class TransactionalBatch {
    //
    private CosmosClient client;
    //
    private final String databaseName = "ToDoList";
    private final String containerName = "Pluto";
    //
    private CosmosDatabase database;
    private CosmosContainer container;

    public void close() {
        client.close();
    }

    /**
     * Run a Hello CosmosDB console application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main(String[] args) {
        TransactionalBatch p = new TransactionalBatch();
        try {
            p.initialize();
            p.batchrecord();
            System.out.println("Demo complete, please hold while resources are released");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(String.format("Cosmos getStarted failed with %s", e));
        } finally {
            System.out.println("Closing the client");
            p.close();
        }
        System.exit(0);
    }

    private void initialize() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + AccountSettings.HOST);
        //
        client = Utils.getClient();
        database = Utils.createDatabaseIfNotExists(client, databaseName);
        container = Utils.createContainerIfNotExists(containerName, database);
    }

    private void batchrecord() throws Exception {
        //
        Employee emp1 = new Employee("0001", "John", 30, "Engineering", "Dev");
        Employee emp2 = new Employee("0002","Adam", 24, "Engineering", "Dev");
        Employee emp1update = new Employee("0001", "Johny", 30, "Engineering", "Dev");
        //
        PartitionKey partitionKey = new PartitionKey(emp1.getPartitionKey());
		CosmosBatch batch = CosmosBatch.createCosmosBatch(partitionKey);
        batch.createItemOperation(emp1);
        batch.createItemOperation(emp2);
        batch.replaceItemOperation(emp1update.getId(), emp1update);
        //  Setup family items to create
        CosmosBatchResponse batchResponse = container.executeCosmosBatch(batch);
        StringUtils.printStackTrace(Thread.currentThread().getStackTrace());
        //
        List<CosmosItemOperation> batchOperations = batch.getOperations();
        for (int index = 0; index < batchOperations.size(); index++) {
            System.out.println("Response Operation "+ batchResponse.getResults().get(index).getOperation());
            System.out.println("Batch Operation "+ batchOperations.get(index));
        }
        System.out.println(batchResponse.getDiagnostics());
        System.out.println("emp1.partitionKey() "+emp1.getPartitionKey());

        /*CosmosItemResponse response = container.createItem(emp1, partitionKey, new CosmosItemRequestOptions());
//        CosmosItemResponse response = container.createItem(emp1, new PartitionKey(emp1.getPartitionKey()), new CosmosItemRequestOptions());
        System.out.println(response.getDiagnostics());*/

    }


}
