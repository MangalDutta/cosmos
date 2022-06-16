package com.azure.cosmos.sample.sync;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdResponse;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdUUID;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.cosmos.sample.common.Employee;
import com.sergiomartinrubio.bytebuddyclient.Main;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleItemEntry {
    //
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleItemEntry.class);
    //
    private CosmosClient client;
    //
    private final String databaseName = "ToDoList";
    private final String containerName = "Pluto";
    //
    private CosmosDatabase database;
    private CosmosContainer container;

    static CountDownLatch latch = new CountDownLatch(1);


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
        SampleItemEntry p = new SampleItemEntry();
        try {
            //
            InstrumentMe me = new InstrumentMe();
            me.invokeCustomMethod();
            System.out.println("invokefc "+ Main.invokefc());
            System.out.println("invokefcwithparams "+Main.invokefcwithparams("age is ", 230));
            System.out.println("invokefc "+ me.invokefc());
            System.out.println("invokefcwithparams "+me.invokefcwithparams("age is ", 230));
            //
            System.out.println("\n\n------------------Initialize Cosmos Connection............................\n\n");
            p.initialize();
            System.out.println("\n\n------------------Done............................\n\n");
            System.out.println("\n\n------------------Add single entry to the collection............................\n\n");
            p.batchrecord();
            System.out.println("\n\n------------------Done............................\n\n");
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

    public void mockResponse() {
        ByteBuf noContent = Unpooled.wrappedBuffer(new byte[0]);
        String partitionKeyRangeId = "3";
        int lsn = 5;

        new RntbdResponse(
                RntbdUUID.EMPTY,
                429,
                ImmutableMap.of(
                        HttpConstants.HttpHeaders.LSN, Integer.toString(lsn),
                        HttpConstants.HttpHeaders.PARTITION_KEY_RANGE_ID, partitionKeyRangeId,
                        HttpConstants.HttpHeaders.TRANSPORT_REQUEST_ID, Long.toString(16L)
                ),
                noContent);
    }

    private void batchrecord() throws Exception {
        //
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.submit(new SampleItemEntry.CosmosRunnable(Long.toString(100), container));
        //
        try {
            latch.await();
        } catch (InterruptedException E) {
            // handle
        }
        //Thread.sleep(1 * 30 * 1000);
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
            System.out.println("Persist the record with record #" + id);
            //
            //
            Employee employee = new Employee(id+RandomStringUtils.randomAlphabetic(5),
                    RandomStringUtils.randomAlphabetic(10),
                    new Random().nextInt(99),
                    "Engineering",
                    "Dev");
            //
            System.out.println(employee.getPartitionKey());
            //
            CosmosItemResponse response = null;
            try {
                System.out.println(container.getClass().getCanonicalName());
                LOGGER.info(container.getClass().getCanonicalName());
                response = container.createItem(employee,
                        new PartitionKey(employee.getPartitionKey()),
                        new CosmosItemRequestOptions());
                System.out.println("added the records...");
            } catch (Exception exception) {
                System.out.println("added item "+response.getStatusCode());
                exception.printStackTrace();
                if (response.getStatusCode() == 429) {
                    LOGGER.info(StringUtils.printStackTrace(exception.getStackTrace()));
                }
            } finally {
                System.out.println("Status Code id --> " + id + " - statuscode --> " + response.getStatusCode());
                LOGGER.info("Status Code id --> " + id + " - statuscode --> " + response.getStatusCode());
                if (response.getStatusCode() == 429) {
                    LOGGER.info("Status Code id --> " + id + " - diagnostics --> " + response.getDiagnostics());
                }
                System.out.println("increment the latch count...");
                latch.countDown();;

//                System.out.println("Status Code id --> "+id+" - statuscode --> "+response.getStatusCode());
                //
                //System.out.println("Status Code id --> "+id+" - diagnostics --> "+response.getDiagnostics());
            }
            //
            /*try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }


}
