package com.azure.cosmos.migrationmonitor;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MigrationMonitor {
    //
    private CosmosAsyncClient sourceCosmosClient;
    private CosmosAsyncClient destCosmosClient;
    //
    private CosmosAsyncDatabase sourceCosmosDb;
    private CosmosAsyncDatabase destCosmosDb;
    //
    private CosmosAsyncContainer sourceCosmosContainer;
    private CosmosAsyncContainer destCosmosContainer;
    //
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();
    //
    private String userAgentSuffix = "MigrationMonitor";
    //
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationMonitor.class);
    //
    private Config config;
    //
    private long startTimeEpochMs = System.currentTimeMillis();
    private long lastMigrationActivityRecorded = System.currentTimeMillis();
    private ScheduledExecutorService scheduler;

    //
    public void close(final CosmosAsyncClient client) {
        client.close();
    }

    /**
     * Run a Cosmos data migration monitor process application.
     *
     * @param args command line args.
     */
    //  <Main>
    public static void main(String[] args) {
        MigrationMonitor p = new MigrationMonitor();
        CommandLine cline = p.setupOptions(args);
        if (cline != null) {
            try {
                p.populateConfig(cline);
                p.init();
                //
                p.triggerMigrationProgressTask();
            } catch (ParseException e) {
                LOGGER.error("Problem with type of the parameter supplied "+ e.getMessage(), e);
            } catch (Exception e) {
                LOGGER.error("Exception :: "+ e.getMessage(), e);
            }
        }
    }

    private CommandLine setupOptions(String[] args) {
        /**
         * usage: Cosmos Data migration status tools
         *  --monitoredAccount <arg>        Source Cosmos Account Endpoint
         *  --monitoredCollection <arg>     Source Cosmos Container name
         *  --monitoredDatabase <arg>       Source Cosmos Database name
         *  --monitoredKey <arg>            Source Cosmos Access key
         *  --destinationAccount <arg>      Destination Cosmos Account Endpoint
         *  --destinationCollection <arg>   Destination Cosmos Container name
         *  --destinationDatabase <arg>     Destination Cosmos Database name
         *  --destinationKey <arg>          Destination Cosmos Access key
         *  --startTimeEpochMs <arg>        Migration Start Time (milliseconds)
         *  --migrationStatusFreq <arg>     Migration Monitor status frequency in seconds
         */
        Options commandLineOptions = new Options();
        //
        commandLineOptions.addOption(Option.builder().longOpt(Constants.MONITORED_ACCOUNT).required(true).hasArg().desc("Source Cosmos Account Endpoint").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.MONITORED_DATABASE).required(true).hasArg().desc("Source Cosmos Database name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.MONITORED_COLLECTION).required(true).hasArg().desc("Source Cosmos Container name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.MONITORED_KEY).required(true).hasArg().desc("Source Cosmos Access key").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DESTINATION_ACCOUNT).required(true).hasArg().desc("Destination Cosmos Account Endpoint").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DESTINATION_DATABASE).required(true).hasArg().desc("Destination Cosmos Database name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DESTINATION_COLLECTION).required(true).hasArg().desc("Destination Cosmos Container name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DESTINATION_KEY).required(true).hasArg().desc("Destination Cosmos Access key").build());
        commandLineOptions.addOption(Option.builder().type(Long.TYPE).longOpt(Constants.MIGRATIONMONITORFREQUENCYSEC).required(true).hasArg().desc("Migration Monitor status frequency in seconds").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.STARTTIMEEPOCHMS).required(true).hasArg().desc("Migration Start Time (milliseconds)").build());
        Option alpha = new Option("a", "alpha", false, "Activate feature alpha");
        //
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        //
        try {
            commandLine = parser.parse(commandLineOptions, args);
        } catch (ParseException | NumberFormatException e) {
            LOGGER.error("Error while parsing the input parameters",e);
            new HelpFormatter().printHelp("Cosmos Data migration status tools ", commandLineOptions);
        }
        //
        return commandLine;
    }

    private Config populateConfig(final CommandLine commandLine) throws ParseException {
        //
        config = new Config();
        config.setMonitoredAccount(commandLine.getOptionValue(Constants.MONITORED_ACCOUNT));
        config.setMonitoredCollection(commandLine.getOptionValue(Constants.MONITORED_COLLECTION));
        config.setMonitoredDatabase(commandLine.getOptionValue(Constants.MONITORED_DATABASE));
        config.setMonitoredKey(commandLine.getOptionValue(Constants.MONITORED_KEY));
        config.setDestinationAccount(commandLine.getOptionValue(Constants.DESTINATION_ACCOUNT));
        config.setDestinationDatabase(commandLine.getOptionValue(Constants.DESTINATION_DATABASE));
        config.setDestinationCollection(commandLine.getOptionValue(Constants.DESTINATION_COLLECTION));
        config.setDestinationKey(commandLine.getOptionValue(Constants.DESTINATION_KEY));
        config.setMigrationStatusFreq(Long.parseLong(commandLine.getOptionValue(Constants.MIGRATIONMONITORFREQUENCYSEC)));
        config.setStartTimeEpochMs(Long.parseLong(commandLine.getOptionValue(Constants.STARTTIMEEPOCHMS)));
        //
        return config;
    }

    private void init() throws Exception {
        LOGGER.info("Using Azure Cosmos Source DB endpoint: " + config.getMonitoredAccount());
        LOGGER.info("Using Azure Cosmos Destination DB endpoint: " + config.getDestinationAccount());
        //  Create Cosmos client
        sourceCosmosClient = getCosmosClient(config.getMonitoredAccount(), config.getMonitoredKey());
        destCosmosClient = getCosmosClient(config.getDestinationAccount(), config.getDestinationKey());
        //
        sourceCosmosDb = getDatabase(sourceCosmosClient, config.getMonitoredDatabase());
        destCosmosDb = getDatabase(destCosmosClient, config.getDestinationDatabase());
        //
        sourceCosmosContainer = getContainer(sourceCosmosClient, config.getMonitoredDatabase(),
                config.getMonitoredCollection());
        destCosmosContainer = getContainer(destCosmosClient, config.getDestinationDatabase(),
                config.getDestinationCollection());
    }

    public void triggerMigrationProgressTask() {
        //
        startTimeEpochMs = config.getStartTimeEpochMs();
        LOGGER.info("Data Migration Start Time "+ new Date(startTimeEpochMs));
        //
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new MigrationProcessTask(),
                0,
                config.getMigrationStatusFreq(),
                TimeUnit.SECONDS);
    }

    void shutdownAndAwaitTermination(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                LOGGER.info("shutdown again");
                executor.shutdownNow();
                close(sourceCosmosClient);
                close(destCosmosClient);
                System.exit(-1);
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    class MigrationProcessTask implements Runnable {
        public void run() {
            //
            long sourceCollectionCount = getDocumentCount(sourceCosmosContainer);
            long currentDestinationCollectionCount = getDocumentCount(destCosmosContainer);
            LOGGER.info(" Source DB Document Count : " +
                    sourceCollectionCount + " Destination DB Document Count : " +
                    currentDestinationCollectionCount);
            //
            double currentPercentage = sourceCollectionCount == 0 ?
                    100 :
                    currentDestinationCollectionCount * 100.0 / sourceCollectionCount;
            long insertedCount =
                    sourceCollectionCount - currentDestinationCollectionCount;
            //
            long nowEpochMs = System.currentTimeMillis();
            //
            double currentRate = (nowEpochMs != lastMigrationActivityRecorded) && (currentDestinationCollectionCount!=0) ?
                    insertedCount * 1000.0 / (nowEpochMs - lastMigrationActivityRecorded) :
                    0;
            //
            long totalSeconds =
                    (lastMigrationActivityRecorded - startTimeEpochMs) / 1000;
            //
            double averageRate = totalSeconds > 0 ? currentDestinationCollectionCount / totalSeconds : 0;
            //
            long etaMs = averageRate > 0
                    ? (long) ((sourceCollectionCount - currentDestinationCollectionCount) * 1000 / averageRate)
                    : (long) 0;
            long etaSec = etaMs / 1000;
                    //
            String out = String.format("\n Percentage Complete : %.2f\n Total records inserted %s out of %s\n" +
                            " Total record pending : %s\n Average Rate : %s " +
                            "\n Current Rate : %.2f \n Estimated Time (sec) : %s\n Total Time (sec) : %s",
                    currentPercentage, currentDestinationCollectionCount, sourceCollectionCount, insertedCount,
                    averageRate, currentRate, etaSec, totalSeconds);
            LOGGER.info(out);
            //
            lastMigrationActivityRecorded = System.currentTimeMillis();
            //
            if (sourceCollectionCount == currentDestinationCollectionCount) {
                LOGGER.info("Migration complete, shutting down the monitor task.");
                shutdownAndAwaitTermination(scheduler);
            }
        }
    }

    public long getDocumentCount(final CosmosAsyncContainer container) {
        String totalCount = "0";
        try {
            //
            CosmosContainerRequestOptions requestOptions = new CosmosContainerRequestOptions();
            requestOptions.setQuotaInfoEnabled(true);
            //
            CosmosContainerResponse response = container.read(requestOptions).block();
            String[] resoueceUsage = response
                    .getResponseHeaders()
                    .get("x-ms-resource-usage")
                    .split(";");
            for (String quota : resoueceUsage) {
                //
                String[] params = quota.split("=");
                if (params[0].equals("documentsCount")) {
                    totalCount = params[1];
                    break;
                }
            }
        } catch (final CosmosException exception) {
            LOGGER.info("Error while fetching the document count " + exception.getMessage());
        }
        return Long.parseLong(totalCount);
    }

    private CosmosAsyncClient getCosmosClient(final String endpoint,
                                              final String key) {
        //
        LOGGER.info("Get Cosmos Client for " + endpoint);
        //
        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .userAgentSuffix(userAgentSuffix)
                .contentResponseOnWriteEnabled(true)
                .buildAsyncClient();
    }

    private CosmosAsyncDatabase getDatabase(final CosmosAsyncClient client,
                                            final String database) throws Exception {
        LOGGER.info("Get database " + database);
        CosmosAsyncDatabase db = client.getDatabase(database);
        if (db == null)
            throw new Exception("Database " + database + " not found");
        return client.getDatabase(database);
    }

    private CosmosAsyncContainer getContainer(final CosmosAsyncClient client,
                                              final String database,
                                              final String container) throws Exception {
        LOGGER.info("Get container " + container + " for database " + database);
        //
        CosmosAsyncDatabase databaseLink = client.getDatabase(database);
        CosmosAsyncContainer containerLink = databaseLink.getContainer(container);
        //
        try {
            CosmosContainerResponse containerResponse = containerLink.read().block();
        } catch (RuntimeException ex) {
            if (ex instanceof CosmosException) {
                CosmosException cosmosClientException = (CosmosException) ex;

                if (cosmosClientException.getStatusCode() == 404) {
                    throw new Exception("Container "+container+" for Database " + database + " not found", ex);
                }
            } else {
                throw ex;
            }
        }
        return containerLink;
    }

}
