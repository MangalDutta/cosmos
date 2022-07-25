
const { CosmosDBManagementClient } = require("@azure/arm-cosmosdb");
const { DefaultAzureCredential } = require("@azure/identity");


/**
 * This sample code demonstrates how to Create or update an Azure Cosmos DB SQL storedProcedure
 */
async function cosmosDbSqlStoredProcedureCreateUpdate() {
  const config = require("./config");  
  const { subscriptionId, resourceGroupName, accountName, databaseName, containerName, storedProcedureName } = config;
  const createUpdateSqlStoredProcedureParameters = {
    options: {},
    resource: { body: "body", id: "<Provide the SP Name you want to create>" }, //id: Provide the SP Name you want to update\create and same name should be followd in config file. 
  };
  const credential = new DefaultAzureCredential();
  const client = new CosmosDBManagementClient(credential, subscriptionId);
  const result = await client.sqlResources.beginCreateUpdateSqlStoredProcedureAndWait(
    resourceGroupName,
    accountName,
    databaseName,
    containerName,
    storedProcedureName,
    createUpdateSqlStoredProcedureParameters
  );
  console.log(result);
}

cosmosDbSqlStoredProcedureCreateUpdate().catch(console.error);




