const { DefaultAzureCredential } = require("@azure/identity");
const { CosmosDBManagementClient } = require("@azure/arm-cosmosdb");
const config = require("./config");
const { subscriptionId, creds,resourceGroupName,accountName,databaseId,containerId } = config;


// Use 'DefaultAzureCredential' or any other credential of your choice based on https://aka.ms/azsdk/js/identity/examples

const client = new CosmosDBManagementClient(creds, subscriptionId);
client.databaseAccounts.get(resourceGroupName, accountName).then((result) => {
  console.log("The result is:");
  console.log(result);
}).catch((err) => {
  console.log("An error occurred:");
  console.error(err);
});


async function run() {
  const { database } = await client.databases.createIfNotExists({ id: databaseId });

  logStep(`Create container with id : ${containerId}`);
  await database.containers.createIfNotExists({ id: containerId });




