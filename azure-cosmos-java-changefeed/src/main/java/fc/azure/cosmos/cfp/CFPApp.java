// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package fc.azure.cosmos.cfp;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import java.util.Date;
import java.util.List;

public class CFPApp {
    //
    private CosmosAsyncClient client;
    //
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncContainer leaseContainer;
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    //
    private String userAgentSuffix = "CosmosDBServiceCFP_FC";
    //
    private static final Logger LOGGER = LoggerFactory.getLogger(CFPApp.class);
    //
    public void close() {
        client.close();
    }
    //
    private Config config;

    /**
     * Run a Hello CosmosDB console application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main(String[] args) {
        CFPApp p = new CFPApp();
        CommandLine cline = p.setupOptions(args);
        if(cline != null) {
            p.populateConfig(cline);
            System.out.println("Start the ChangeFeed Processor ");
            try {
                p.initChangeFeedProcessor();
            } catch (Exception e) {
                LOGGER.error("ddd", e);
                System.err.println(String.format("Cosmos getStarted failed with %s", e));
            } finally {
                System.out.println("Closing the client");
                p.close();
            }
        }
    }


    private CommandLine setupOptions(String[] args) {

        /**
         * Usage:
         * --endpoint <cosmos account>
         * --database <database name>
         * --container <container name>
         * --key <access key>
         * --consistencylevel SESSION
         * --leasecontainer <lease container name>
         * --changefeedHost <host to be used for change feed>
         * --changefeedPrefix <prefix to be used for change feed>
         *
         * Examples
         * --endpoint <cosmos account> --database demo --container <container name> --key <access key>
         * --consistencylevel <consistency level weaker than the one set at account level> --leasecontainer <container name>
         * --changefeedHost <host to be used for change feed> --changefeedPrefix <prefix to be used for change feed>
         */
        //
        Options commandLineOptions = new Options();
        //
        commandLineOptions.addOption( Option.builder().longOpt(Constants.ENDPOINT).required(true).hasArg().desc("Endpoint").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.DATABASE).required(true).hasArg().desc("Database name").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONTAINER).required(true).hasArg().desc("Container name").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.KEY).required(true).hasArg().desc("Access key").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.CONSISTENCY_LEVEL).required(false).hasArg().desc("Read consistency level (Eventual/CONSISTENT_PREFIX/SESSION/BOUNDED_STALENESS)").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.LEASE_CONTAINER).required(true).hasArg().desc("Lease container name").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.HOST).required(true).hasArg().desc("Change feed host").build()  );
        commandLineOptions.addOption( Option.builder().longOpt(Constants.PREFIX).required(true).hasArg().desc("Change feed prefix").build()  );
        //
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        //
        try {
            commandLine = parser.parse(commandLineOptions, args);
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("Change feed utility options ", commandLineOptions);
        }
        //
        return commandLine;
    }

    private Config populateConfig(final CommandLine commandLine) {
        //
        config = new Config();
        config.setEndpoint(commandLine.getOptionValue(Constants.ENDPOINT));
        config.setContainer(commandLine.getOptionValue(Constants.CONTAINER));
        config.setDatabase(commandLine.getOptionValue(Constants.DATABASE));
        config.setKey(commandLine.getOptionValue(Constants.KEY));
        config.setLeaseContainer(commandLine.getOptionValue(Constants.LEASE_CONTAINER));
        //
        ConsistencyLevel consistencyLevel = ConsistencyLevel.SESSION;
        switch(commandLine.getOptionValue(Constants.CONSISTENCY_LEVEL)) {
            case Constants.CONSISTENCY_LEVEL_STRONG:
                consistencyLevel = ConsistencyLevel.STRONG;
                break;

            case Constants.CONSISTENCY_LEVEL_BOUNDED_STALENESS:
                consistencyLevel = ConsistencyLevel.BOUNDED_STALENESS;
                break;

            case Constants.CONSISTENCY_LEVEL_SESSION:
                consistencyLevel = ConsistencyLevel.SESSION;
                break;

            case Constants.CONSISTENCY_LEVEL_CONSISTENT_PREFIX:
                consistencyLevel = ConsistencyLevel.CONSISTENT_PREFIX;
                break;

            case Constants.CONSISTENCY_LEVEL_EVENTUAL:
                consistencyLevel = ConsistencyLevel.EVENTUAL;
                break;
        }
        config.setReadConsistencyLevel(consistencyLevel);
        //
        return config;
    }


    private void initChangeFeedProcessor() throws Exception {
        System.out.println("Using Azure Cosmos DB endpoint: " + config.getEndpoint());
        //  Create sync client
        client = getCosmosClient();
        //
        createDatabaseIfNotExists();
        createContainerIfNotExists();
        createLeaseContainerIfNotExists();
        System.out.println("\n\n\n\nCreating materialized view...");
        //
        ChangeFeedProcessor changeFeedProcessorInstance = getChangeFeedProcessor("MongoCFP");
        changeFeedProcessorInstance
                .start()
                .subscribeOn(Schedulers.elastic())
                .doOnSuccess(aVoid -> {
                    System.out.println("Successfull");
                    //pass
                })
                .subscribe();
        //
        int counter = 0;
        boolean isRunning = true;
        while (isRunning) {
            List<ChangeFeedProcessorState> states = changeFeedProcessorInstance.getCurrentState().block();
            System.out.println("states size " + states.size());
            for (ChangeFeedProcessorState state : states) {
                System.out.println("ContinuationToken " + state.getContinuationToken() +
                        " LeaseToken " + state.getLeaseToken() +
                        " HostName " + state.getHostName() +
                        " EstimatedLag " + state.getEstimatedLag());
            }
            System.out.println("Sleeping now ");
            Thread.sleep(1000 * 60);
            counter++;
            System.out.println("Awake ");
            if (counter == 500) {
                isRunning = false;
                System.out.println("Exiting ");

            }
        }
    }
/*
    public static ChangeFeedProcessor getChangeFeedProcessor(final String hostName,
                                                             final CosmosAsyncContainer container,
                                                             final CosmosAsyncContainer leaseContainer) {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();
        options.setStartFromBeginning(true);
        //options.setStartTime(Instant.now().minusSeconds(60*60));
        // If you want multiple consumers to receive same message
        options.setLeasePrefix(RandomStringUtils.randomAlphabetic(5));

        return new ChangeFeedProcessorBuilder()
                .hostName(hostName) // hostName would help you distribute the data across partitions
                .options(options) // options
                .feedContainer(container) // monitored container
                .leaseContainer(leaseContainer) // lease container
                .handleChanges((List<JsonNode> docs) -> {
                    System.out.println("--->setHandleChanges() START");
                    for (JsonNode document : docs) {
                        // Modify the data or persist it as per your need
                    }
                    System.out.println("--->handleChanges() END");
                })
                .buildChangeFeedProcessor();
    }*/

    public static int CHANGE_FEED_COUNTER = 0;

    private ChangeFeedProcessor getChangeFeedProcessor(String hostName) {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();

        return new ChangeFeedProcessorBuilder()
                .hostName(hostName)
                .options(options)
                .feedContainer(container)
                .leaseContainer(leaseContainer)
                .handleChanges((List<JsonNode> docs) -> {
                    System.out.println("\n----------\n----------New changes received "+new Date());
                    //
                    for (JsonNode document : docs) {
                        try {
                            CHANGE_FEED_COUNTER++;
                            System.out.println("DOCUMENT RECEIVED: " + OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                                    .writeValueAsString(document) + " MESSAGE_COUNT " + CHANGE_FEED_COUNTER);
                        } catch (JsonProcessingException e) {
                            LOGGER.error("JsonProcessingException ", e);
                            e.printStackTrace();
                        }
                    }
                    System.out.println("Finished processing changes \n----------\n----------");
                })
                .buildChangeFeedProcessor();
    }


    private CosmosAsyncClient getCosmosClient() {
        //
        return new CosmosClientBuilder()
                .endpoint(config.getEndpoint())
                .key(config.getKey())
                .userAgentSuffix(userAgentSuffix)
                .consistencyLevel(config.getReadConsistencyLevel())
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    private void createDatabaseIfNotExists() throws Exception {
        System.out.println("Create database " + config.getDatabase() + " if not exists.");
        //  Create database if not exists
        client.createDatabaseIfNotExists(config.getDatabase()).block();
        database = client.getDatabase(config.getDatabase());
        //
        System.out.println("Checking database " + database.getId() + " completed!\n");
    }

    private void createContainerIfNotExists() throws Exception {
        System.out.println("Create container " + config.getContainer() + " if not exists.");
        //
        CosmosAsyncDatabase databaseLink = client.getDatabase(config.getContainer());
        CosmosAsyncContainer collectionLink = databaseLink.getContainer(config.getContainer());
        CosmosContainerResponse containerResponse = null;

        try {
            containerResponse = collectionLink.read().block();

            if (containerResponse != null) {
                //throw new IllegalArgumentException(String.format("Collection %s already exists in database %s.", collectionName, databaseName));
                container = databaseLink.getContainer(config.getContainer());
                return;
            }
        } catch (RuntimeException ex) {
            if (ex instanceof CosmosException) {
                CosmosException cosmosClientException = (CosmosException) ex;

                if (cosmosClientException.getStatusCode() != 404) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
        //
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(config.getContainer(),
                "/partitionKey");
        containerSettings.setDefaultTimeToLiveInSeconds(-1);
        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
        //ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(20000);
        containerResponse = databaseLink.createContainer(containerSettings, requestOptions).block();
        //
        if (containerResponse == null) {
            throw new RuntimeException(String.format("Failed to create collection %s in database %s.",
                    config.getContainer(),
                    config.getDatabase()));
        }
        //
        container = databaseLink.getContainer(config.getContainer());
        //
        System.out.println("Checking container " + container.getId() + " completed!\n");
    }

    private void createLeaseContainerIfNotExists() throws Exception {
        System.out.println("Create container " + config.getLeaseContainer() + " if not exists.");
//
        CosmosAsyncDatabase databaseLink = client.getDatabase(config.getLeaseContainer());
        CosmosAsyncContainer collectionLink = databaseLink.getContainer(config.getLeaseContainer());

        CosmosContainerResponse containerResponse = null;

        try {
            containerResponse = collectionLink.read().block();

            if (containerResponse != null) {
                //
                leaseContainer = databaseLink.getContainer(config.getLeaseContainer());
                return;
            }
        } catch (RuntimeException ex) {
            if (ex instanceof CosmosException) {
                CosmosException cosmosClientException = (CosmosException) ex;

                if (cosmosClientException.getStatusCode() != 404) {
                    throw ex;
                }
            } else {
                throw ex;
            }
        }
        //
        CosmosContainerProperties containerSettings = new CosmosContainerProperties(
                config.getLeaseContainer(),
                "/id");
        containerSettings.setDefaultTimeToLiveInSeconds(-1);
        CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
        //ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(20000);
        containerResponse = databaseLink.createContainer(containerSettings, requestOptions).block();
        //
        if (containerResponse == null) {
            throw new RuntimeException(String.format("Failed to create collection %s in database %s.",
                    config.getLeaseContainer(),
                    config.getDatabase()));
        }
        //
        leaseContainer = databaseLink.getContainer(config.getLeaseContainer());
        //
        System.out.println("Checking lease container " + container.getId() + " completed!\n");
    }

}
