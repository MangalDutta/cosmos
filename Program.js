
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
    resource: { body: "body", id: "CreateSPUsingCosmosDBMgmtClient2" },
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