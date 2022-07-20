const config = {
    endpoint: "<Cosmos Endpoint>",
    tenantID: "<TenantID>",
    clientID: "SP ApplicationID",
    clientSecret: "<SP SecretID>",
    databaseId: "<Cosmos database Name>",
    containerId: "<Container Name>",
    partitionKey: { kind: "Hash", paths: ["/<name>"] }
  };
  
  module.exports = config;