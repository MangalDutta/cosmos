const config = {
    DefaultAzureCredential: require("@azure/identity"),
    CosmosDBManagementClient: require("@azure/arm-cosmosdb"),
    subscriptionId: "<Provide the Subscription Name>",
    creds: "new DefaultAzureCredential()",
    resourceGroupName: "<Cosmos database Name>",
    accountName: "<Cosmos Account Name>"
    endpoint: "<Cosmos Endpoint>",
    tenantID: "<TenantID>",
    clientID: "SP ApplicationID",
    clientSecret: "<SP SecretID>",
    databaseId: "<Cosmos database Name>",
    containerId: "<Container Name>",
    containerId: "<Container Name>",
    partitionKey: { kind: "Hash", paths: ["/<name>"] }
  };
  
  module.exports = config;