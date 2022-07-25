

const config = {
  subscriptionId: "<Subscription Id>",
  resourceGroupName: "<Cosmos resource group Name>",
  accountName: "<Cosmos Account Name>",
  databaseName: "<Cosmos database Name>",
  containerName: "<Cosmos container Name>",
  storedProcedureName: "<Provide the SP Name you want to create>",
  partitionKey: { kind: "Hash", paths: ["/<name>"] }
};

module.exports = config;
