package fc.azure.cosmos.cfp;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.models.*;
import org.apache.commons.cli.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BulkUploadSample {
    //
    private Config appConfig;
    //
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncClient client;

    public static void main(String... args) throws Exception {
        //
        BulkUploadSample app = new BulkUploadSample();
        //
        CommandLine cline = app.setupOptions(args);
        if (cline != null) {
            //
            app.populateConfig(cline);
            //
            System.out.println("Execute batch operations ");
            //
            app.initialize();
            //
            app.executeStoredProcedure();
            //app.executeCosmosBatch();
            //
            System.out.println("Finished the execution");
        }
        System.exit(-1);
    }

    public void initialize() throws Exception {
        client = CosmosUtil.getCosmosClient(appConfig, "Execute_SP");
        database = CosmosUtil.createDatabaseIfNotExists(appConfig, client);
        container = CosmosUtil.createContainerIfNotExists(appConfig, client, database);
    }


    private CommandLine setupOptions(final String[] args) {

        /**
         * Usage:
         * --endpoint <cosmos account>
         * --database <database name>
         * --container <container name>
         * --key <access key>
         *
         * Examples
         * --endpoint <cosmos account> --database demo --container <container name> --key <access key>
         */
        //
        Options commandLineOptions = new Options();
        //
        commandLineOptions.addOption(Option.builder().longOpt(Constants.ENDPOINT).required(true).hasArg().desc("Endpoint").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DATABASE).required(true).hasArg().desc("Database name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.CONTAINER).required(true).hasArg().desc("Container name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.KEY).required(true).hasArg().desc("Access key").build());
        //
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        //
        try {
            commandLine = parser.parse(commandLineOptions, args);
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("Data Loader utility options ", commandLineOptions);
        }
        //
        return commandLine;
    }


    private Config populateConfig(final CommandLine commandLine) {
        //
        appConfig = new Config();
        appConfig.setEndpoint(commandLine.getOptionValue(Constants.ENDPOINT));
        appConfig.setContainer(commandLine.getOptionValue(Constants.CONTAINER));
        appConfig.setDatabase(commandLine.getOptionValue(Constants.DATABASE));
        appConfig.setKey(commandLine.getOptionValue(Constants.KEY));
        //
        return appConfig;
    }

    public void executeStoredProcedure() throws Exception {
        //
        String spName = "bulkUpload";
        System.out.println(String.format("Executing stored procedure '%s'\n\n", spName));

        String PKEY = UUID.randomUUID().toString();
        String partitionValue = PKEY;
        CosmosStoredProcedureRequestOptions options = new CosmosStoredProcedureRequestOptions();
        options.setPartitionKey(new PartitionKey(partitionValue));
        options.setScriptLoggingEnabled(true);

        ArrayList<Patient> patients = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            final Patient patient = new Patient();
            patient.setFirstname(RandomStringUtils.randomAlphabetic(12));
            patient.setLastname(RandomStringUtils.randomAlphabetic(12));
            patient.setId(UUID.randomUUID().toString());
            patient.setPartitionKey(PKEY);
            //
            patients.add(patient);
        }
        //
        List<Object> sproc_args = new ArrayList<>();
        sproc_args.add(patients);

        CosmosStoredProcedureResponse executeResponse = container.getScripts()
                .getStoredProcedure(spName)
                .execute(sproc_args, options).block();

        //System.out.println("\n\n------------LOGS = " + executeResponse.getScriptLog() + "\n\n");
        //
        System.out.println(String.format("Stored procedure %s returned %s (HTTP %d), at cost %.3f RU.\n",
                spName,
                executeResponse.getResponseAsString(),
                executeResponse.getStatusCode(),
                executeResponse.getRequestCharge()));
    }

    public void executeCosmosBatch() throws Exception {
        //
        String PKEY = UUID.randomUUID().toString();
        System.out.println(String.format("Executing Cosmos Batch operation"));
        //
        CosmosBatch batch = CosmosBatch.createCosmosBatch(new PartitionKey(PKEY));
        //
        for (int i = 1; i <= 3; i++) {
            //
            final Patient patient = new Patient();
            patient.setFirstname(RandomStringUtils.randomAlphabetic(12));
            patient.setLastname(RandomStringUtils.randomAlphabetic(12));
            patient.setId(UUID.randomUUID().toString());
            patient.setPartitionKey(PKEY);
            //
            batch.createItemOperation(patient);
        }
        //
        CosmosBatchResponse response = container.executeCosmosBatch(batch).block();
        //
        System.out.println("CosmosBatchResponse SuccessStatusCode "+response.isSuccessStatusCode());
        if (response.isSuccessStatusCode()) {
            List<CosmosBatchOperationResult> results = response.getResults();
        }
        //
        System.out.println(String.format("Cosmos Batch operation returned %s (HTTP %d), at cost %.3f RU.\n",
                response.getResults().size(),
                response.getStatusCode(),
                response.getRequestCharge()));
    }
}
