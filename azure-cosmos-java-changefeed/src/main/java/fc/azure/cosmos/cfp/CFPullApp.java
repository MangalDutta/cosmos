// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package fc.azure.cosmos.cfp;

import com.azure.cosmos.*;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

public class CFPullApp {
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CFPullApp.class);
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
        CFPullApp p = new CFPullApp();
        CommandLine cline = p.setupOptions(args);
        if (cline != null) {
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
         *
         * Examples
         * --endpoint <cosmos account> --database demo --container <container name> --key <access key>
         * --consistencylevel <consistency level weaker than the one set at account level>
         */
        //
        Options commandLineOptions = new Options();
        //
        commandLineOptions.addOption(Option.builder().longOpt(Constants.ENDPOINT).required(true).hasArg().desc("Endpoint").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DATABASE).required(true).hasArg().desc("Database name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.CONTAINER).required(true).hasArg().desc("Container name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.KEY).required(true).hasArg().desc("Access key").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.CONSISTENCY_LEVEL).required(false).hasArg().desc("Read consistency level (Eventual/CONSISTENT_PREFIX/SESSION/BOUNDED_STALENESS)").build());
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
        //
        ConsistencyLevel consistencyLevel = ConsistencyLevel.SESSION;
        switch (commandLine.getOptionValue(Constants.CONSISTENCY_LEVEL)) {
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
        //
        System.out.println("Pull Model Started");
        // Pull data from beginning for all full range of partitions
        CosmosChangeFeedRequestOptions options1 = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forFullRange());
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions
                .createForProcessingFromBeginning(FeedRange.forLogicalPartition(
                        new PartitionKey("1")
                ));
        // Pull data from beginning for logical partition
        CosmosChangeFeedRequestOptions options2 = CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(FeedRange.forFullRange());
        Map<Integer, String> continuations = new HashMap<>();

        List<ObjectNode> results = container
                .queryChangeFeed(options, ObjectNode.class)
                // NOTE - in real app you would need delaying persisting the
                // continuation until you retrieve the next one
                .handle((r) -> continuations.put(0, r.getContinuationToken()))
                .collectList()
                .block();

        System.out.println(
                String.format(
                        "Continuation AFTER: %s, records retrieved: %d",
                        new String(
                                Base64.getUrlDecoder().decode(continuations.get(0)),
                                StandardCharsets.UTF_8),
                        results.size()));

        System.out.println("Pull Model Finished");

    }

    private CosmosAsyncClient getCosmosClient() {
        //
        //DirectConnectionConfig dconfig = new DirectConnectionConfig();
        //dconfig.setMaxConnectionsPerEndpoint(10);
        //dconfig.setMaxRequestsPerConnection(10);
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
        CosmosAsyncDatabase databaseLink = client.getDatabase(config.getDatabase());
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
        //containerSettings.setChangeFeedPolicy(policy);
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
        CosmosAsyncDatabase databaseLink = client.getDatabase(config.getDatabase());
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
