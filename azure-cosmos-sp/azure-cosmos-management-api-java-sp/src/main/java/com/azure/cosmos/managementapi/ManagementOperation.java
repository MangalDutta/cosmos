package com.azure.cosmos.managementapi;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.cosmos.fluent.models.*;
import com.azure.resourcemanager.cosmos.models.*;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLineParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ManagementOperation {

    private static Config config = new Config();

    private static CommandLine setupOptions(String[] args) {
        /**
         * Usage:
         * --endpoint <cosmos account>
         * --database <database name>
         * --container <container name>
         * --resourcegroup <resourcegroup>
         */
        //
        Options commandLineOptions = new Options();
        //
        commandLineOptions.addOption(Option.builder().longOpt(Constants.ENDPOINT).required(true).hasArg().desc("Account name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DATABASE).required(true).hasArg().desc("Database name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.CONTAINER).required(true).hasArg().desc("Container name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.RESOURCEGROUP).required(true).hasArg().desc("Resource Group name").build());
        //
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        //
        try {
            commandLine = parser.parse(commandLineOptions, args);
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("Cosmos Management API ", commandLineOptions);
        }
        //
        return commandLine;
    }

    private static Config populateConfig(final CommandLine commandLine) {
        //
        config.setEndpoint(commandLine.getOptionValue(Constants.ENDPOINT));
        config.setContainer(commandLine.getOptionValue(Constants.CONTAINER));
        config.setDatabase(commandLine.getOptionValue(Constants.DATABASE));
        config.setResourceGroup(commandLine.getOptionValue(Constants.RESOURCEGROUP));
        return config;
    }


    public static void createCosmosAccount(final AzureResourceManager azureResourceManager) {
        CosmosDBAccount cosmosDBAccount = azureResourceManager.cosmosDBAccounts()
                .define(config.getEndpoint())
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(config.getResourceGroup())
                .withKind(DatabaseAccountKind.GLOBAL_DOCUMENT_DB)
                .withEventualConsistency()
                .withWriteReplication(Region.US_EAST)
                .withReadReplication(Region.US_SOUTH_CENTRAL)
                .create();
    }

    public static void createCosmosDatabase(final AzureResourceManager azureResourceManager) {
        //
        SqlDatabaseCreateUpdateParameters databaseParams = new SqlDatabaseCreateUpdateParameters()
                .withLocation("West US")
                .withTags(mapOf())
                .withResource(new SqlDatabaseResource().withId(config.getDatabase()))
                .withOptions(new CreateUpdateOptions());
        SqlDatabaseGetResultsInner dbResult = azureResourceManager.cosmosDBAccounts()
                //cosmosDBAccount
                .manager()
                .serviceClient()
                .getSqlResources()
                .createUpdateSqlDatabase(config.getResourceGroup(),
                        config.getEndpoint(),
                        config.getDatabase(),
                        databaseParams,
                        Context.NONE);
        System.out.println("Database Id " + dbResult.id());
    }

    public static void createCosmosCollection(final AzureResourceManager azureResourceManager) {
        //
        IndexingPolicy indexingPolicy = new IndexingPolicy()
                .withAutomatic(true)
                .withIndexingMode(IndexingMode.CONSISTENT)
                .withIncludedPaths(
                        Arrays
                                .asList(
                                        new IncludedPath()
                                                .withPath("/*")
                                                .withIndexes(
                                                        Arrays
                                                                .asList(
                                                                        new Indexes()
                                                                                .withDataType(DataType.STRING)
                                                                                .withPrecision(-1)
                                                                                .withKind(IndexKind.RANGE),
                                                                        new Indexes()
                                                                                .withDataType(DataType.NUMBER)
                                                                                .withPrecision(-1)
                                                                                .withKind(IndexKind.RANGE)))))
                .withExcludedPaths(Arrays.asList());
        //
        ContainerPartitionKey partitionKey = new ContainerPartitionKey()
                .withPaths(Arrays.asList("/partitionKey"))
                .withKind(PartitionKind.HASH);
        //
        SqlContainerResource sqlContainerResource = new SqlContainerResource()
                .withId(config.getContainer())
                .withIndexingPolicy(indexingPolicy)
                .withPartitionKey(partitionKey)
                .withUniqueKeyPolicy(new UniqueKeyPolicy())
                .withConflictResolutionPolicy(new ConflictResolutionPolicy());
        //
        SqlContainerCreateUpdateParameters sqlContainerCreateUpdateParameters = new SqlContainerCreateUpdateParameters()
                .withLocation(Region.US_WEST.label())
                .withTags(mapOf())
                .withResource(sqlContainerResource);
        //
        SqlContainerGetResultsInner containerResult = azureResourceManager.cosmosDBAccounts()
                //cosmosDBAccount
                .manager()
                .serviceClient()
                .getSqlResources()
                .createUpdateSqlContainer(config.getResourceGroup(),
                        config.getEndpoint(),
                        config.getDatabase(),
                        config.getContainer(),
                        sqlContainerCreateUpdateParameters.withOptions(
                                new CreateUpdateOptions().withAutoscaleSettings(
                                        new AutoscaleSettings().withMaxThroughput(1000)
                                )),
                        Context.NONE);
        System.out.println("Container Id " + containerResult.id());
    }

    public static void createStoreProcedure(final AzureResourceManager azureResourceManager) {
        //
        String storedProcedureName = "mgmtApiSP";
        SqlStoredProcedureCreateUpdateParameters storedProdParams = new SqlStoredProcedureCreateUpdateParameters()
                .withResource(new SqlStoredProcedureResource().withId(storedProcedureName).withBody("body2"))
                .withOptions(new CreateUpdateOptions());
        //
        SqlStoredProcedureGetResultsInner spResult = azureResourceManager.cosmosDBAccounts()
                //cosmosDBAccount
                .manager()
                .serviceClient()
                .getSqlResources()
                .createUpdateSqlStoredProcedure(config.getResourceGroup(),
                        config.getEndpoint(),
                        config.getDatabase(),
                        config.getContainer(),
                        storedProcedureName,
                        storedProdParams,
                        Context.NONE);
        System.out.println("SP Id " + spResult.id());
    }

    public static void createUDF(final AzureResourceManager azureResourceManager) {
        //
        String userDefinedFunctionName = "mgmtApiUDF";
        SqlUserDefinedFunctionCreateUpdateParameters udfParams = new SqlUserDefinedFunctionCreateUpdateParameters()
                .withResource(
                        new SqlUserDefinedFunctionResource().withId(userDefinedFunctionName).withBody("body"))
                .withOptions(new CreateUpdateOptions());
        SqlUserDefinedFunctionGetResultsInner udfResult = azureResourceManager.cosmosDBAccounts()
                //cosmosDBAccount
                .manager()
                .serviceClient()
                .getSqlResources()
                .createUpdateSqlUserDefinedFunction(config.getResourceGroup(),
                        config.getEndpoint(),
                        config.getDatabase(),
                        config.getContainer(),
                        userDefinedFunctionName,
                        udfParams,
                        Context.NONE);
        System.out.println("UDF Id " + udfResult.id());
    }

    public static void createTrigger(final AzureResourceManager azureResourceManager) {
        //
        String triggerName = "mgmtApiTrigger";
        SqlTriggerCreateUpdateParameters triggerParams = new SqlTriggerCreateUpdateParameters()
                .withResource(
                        new SqlTriggerResource()
                                .withId(triggerName)
                                .withBody("body")
                                .withTriggerType(TriggerType.fromString("Pre"))
                                .withTriggerOperation(TriggerOperation.fromString("delete")))
                .withOptions(new CreateUpdateOptions());
        SqlTriggerGetResultsInner triggerResult = azureResourceManager.cosmosDBAccounts()
                //cosmosDBAccount
                .manager()
                .serviceClient()
                .getSqlResources()
                .createUpdateSqlTrigger(config.getResourceGroup(),
                        config.getEndpoint(),
                        config.getDatabase(),
                        config.getContainer(),
                        triggerName,
                        triggerParams,
                        Context.NONE);
        System.out.println("Trigger Id " + triggerResult.id());
    }

    public static void main(String... args) throws Exception {
        //
        CommandLine cline = setupOptions(args);
        if (cline != null) {
            populateConfig(cline);
            // Authenticate
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                    .build();
            //
            AzureResourceManager azureResourceManager = AzureResourceManager
                    .configure()
                    .withLogLevel(HttpLogDetailLevel.BASIC)
                    .authenticate(credential, profile)
                    .withDefaultSubscription();
            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());
            // Create CosmosDB Global account
            createCosmosAccount(azureResourceManager);
            createCosmosDatabase(azureResourceManager);
            createCosmosCollection(azureResourceManager);
            createStoreProcedure(azureResourceManager);
            createUDF(azureResourceManager);
            createTrigger(azureResourceManager);
            //
            System.out.println("\n\n <<< Complete creation of the account using management api >>>  \n\n");
        }

    }

    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
