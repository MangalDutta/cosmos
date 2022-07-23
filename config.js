const config = {
  subscriptionId: "cb3a7a37-4fd6-4038-9622-1fd3eaa97bcf",
  resourceGroupName: "SQLOnWindowBox",
  accountName: "cosmosusecasetestingaccount",
  databaseName: "AutoScaleTest",
  containerName: "testcontainer",
  storedProcedureName: "CreateSPUsingCosmosDBMgmtClient2",
  partitionKey: { kind: "Hash", paths: ["/<name>"] }
};

module.exports = config;