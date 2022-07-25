# Create\Update stored procedure using the Azure Management API
## Setting up Service Principal for accessing data from the Cosmos account using SQL API
### Prerequisites

1. Create one CosmosDB account with one dummy database and collection.

2. Setup Service Principal using this [here](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal). 

### Define roles for the service principal. 

1. Built-in role - Azure Cosmos DB exposes 2 built-in role definitions:

i. Cosmos DB Built-in Data Reader
ii. Cosmos DB Built-in Data Contributor

The details for which are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#built-in-role-definitions)

2. Custom-roles - Azure Cosmos DB allows custom roles for real-only or read-write or other combination. The details for the same are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#role-definitions)

#### NOTE - This permission model covers only database operations that involve reading and writing data. It does not cover any kind of management operations on management resources, for example:
3. Find the sample code [here](https://github.com/faizc/cosmos/tree/main/azure-cosmos-sp/azure-cosmos-nodejs-sp) for Setting up Service Principal Authentication for accessing data from the Cosmos DB SQL API using nodejs. 
#
Create/Replace/Delete Database
Create/Replace/Delete Container
Replace Container Throughput
Create/Replace/Delete/Read Stored Procedures
Create/Replace/Delete/Read Triggers
Create/Replace/Delete/Read User Defined Functions
You would need to use the Azure RBAC, details are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#permission-model)

### Azure CosmosDBManagement client library for JavaScript helps you to perform the Cosmos DB Managemnt operation mentioned [here](https://docs.microsoft.com/en-us/javascript/api/@azure/arm-cosmosdb/?view=azure-node-preview)

# Prerequisites

You need [an Azure subscription](https://azure.microsoft.com/en-us/free/) to run these sample programs.

Samples retrieve credentials to access the service endpoint from environment variables. Alternatively, edit the source code to include the appropriate credentials. See each individual sample for details on which environment variables/credentials it requires to function.

Adapting the samples to run in the browser may require some additional consideration. For details, please see the [package README](https://github.com/Azure/azure-sdk-for-js/blob/main/sdk/cosmosdb/arm-cosmosdb/README.md).

# Setup

To run the samples using the published version of the package:

Install the dependencies using npm:


## Deployment


```bash
  npm install @azure/identity

  npm install @azure/arm-cosmosdb
```

# Next Steps
Take a look at our [API Documentation](https://docs.microsoft.com/en-us/javascript/api/@azure/arm-cosmosdb/?view=azure-node-preview) for more information about the APIs that are available in the clients.
