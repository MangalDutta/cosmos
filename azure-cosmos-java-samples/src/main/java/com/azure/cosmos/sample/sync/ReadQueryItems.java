package com.azure.cosmos.sample.sync;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.sample.common.AccountSettings;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadQueryItems {

    //
    private static final Logger LOGGER = LoggerFactory.getLogger(Simulator429s.class);
    //
    private CosmosClient client;
    //
    private final String databaseName = "NutritionData";
    private final String containerName = "Food";
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
        ReadQueryItems p = new ReadQueryItems();
        try {
            p.initialize();
            //p.readItem();
            p.queryItem();
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

    private void readItem() throws Exception {
        CosmosItemResponse readResponse1 = container.readItem("0001",
                new PartitionKey("fhir"),
                new CosmosItemRequestOptions(),
                Object.class);

        System.out.println("Read Request Charge ===> "+readResponse1.getRequestCharge());

    }

    private void queryItem() throws Exception {

//        String query = String.format("SELECT * from c where c.id = '%s' and c.foodGroup = '%s'", "0001", "fhir");
//        String query = String.format("SELECT * from c where c.id = '%s'", "0001");
        String query = String.format("SELECT * from c where c.id in ('04001', '04013', '04016', '03235')");
//        String query = String.format("SELECT * from c where  c.foodGroup = '%s'",   "fhir");
        CosmosQueryRequestOptions cosmosQueryRequestOptions = new CosmosQueryRequestOptions();

        CosmosPagedIterable<Object> feedResponseIterator1 =
                container.queryItems(query, cosmosQueryRequestOptions, Object.class);
        feedResponseIterator1.forEach(s -> System.out.println(s.hashCode()));
        //System.out.println("Query Request Charge ===> "+readResponse1.getRequestCharge());

    }


}
