package com.azure.cosmos.sample.sync;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.models.*;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.cosmos.sample.common.Employee;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Samples for the transactional test
 */
public class Simulator429s {
    //
    private static final Logger LOGGER = LoggerFactory.getLogger(Simulator429s.class);
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
        Simulator429s p = new Simulator429s();
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
        ExecutorService executor = Executors.newFixedThreadPool(1000);
        long max = Long.MAX_VALUE;
        //max = 10;
        for(long index=0; index<=max; index++) {
            executor.submit(new CosmosRunnable(Long.toString(index), container));
        }
        //
        Thread.sleep(100000000);
    }

    public static class CosmosRunnable implements Runnable {
        private String id;
        private CosmosContainer container;

        CosmosRunnable(final String id,
                       final CosmosContainer container) {
            this.id = id;
            this.container = container;
        }

        @Override
        public void run() {
            //
            System.out.println("Persist the record with record #"+id);
            //
            Employee employee = new Employee(id,
                    RandomStringUtils.randomAlphabetic(10),
                    new Random().nextInt(99),
                    "Engineering",
                    "Dev");
            //
            CosmosItemResponse response = null;
            try {

                response = container.upsertItem(employee,
                        new PartitionKey(employee.getPartitionKey()),
                        new CosmosItemRequestOptions());
                System.out.println("added the records...");
            } catch (Exception exception) {
                exception.printStackTrace();
                if(response.getStatusCode()==429) {
                    LOGGER.info(StringUtils.printStackTrace(exception.getStackTrace()));
                }
            } finally {
                LOGGER.info("Status Code id --> "+id+" - statuscode --> "+response.getStatusCode());
                if(response.getStatusCode()==429) {
                  LOGGER.info("Status Code id --> "+id+" - diagnostics --> "+response.getDiagnostics());
                }
//                System.out.println("Status Code id --> "+id+" - statuscode --> "+response.getStatusCode());
                //
                //System.out.println("Status Code id --> "+id+" - diagnostics --> "+response.getDiagnostics());
            }
        }
    }


}
