# Create Database and Container using the Azure Management API
## Setting up Service Principal for accessing data from the Cosmos account using SQL API
### Prerequisites

1. Create one CosmosDB account. 

2. Setup Service Principal using this [here](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal). 

### Define roles for the service principal. 

1. Built-in role - Azure Cosmos DB exposes 2 built-in role definitions:

i. Cosmos DB Built-in Data Reader
ii. Cosmos DB Built-in Data Contributor

The details for which are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#built-in-role-definitions)

2. Custom-roles - Azure Cosmos DB allows custom roles for real-only or read-write or other combination. The details for the same are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#role-definitions)

#### NOTE - This permission model covers only database operations that involve reading and writing data. It does not cover any kind of management operations on management resources, for example:
3. Find the sample code [here](https://github.com/faizc/cosmos/tree/main/azure-cosmos-sp/azure-cosmos-dotnet-sp) for Setting up Service Principal Authentication for accessing data from the Cosmos DB SQL API using dotnet. 
#
Create/Replace/Delete Database
Create/Replace/Delete Container
Replace Container Throughput
Create/Replace/Delete/Read Stored Procedures
Create/Replace/Delete/Read Triggers
Create/Replace/Delete/Read User Defined Functions
You would need to use the Azure RBAC, details are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#permission-model)

### Azure CosmosDBManagement client library for dotnet helps you to perform the Cosmos DB Managemnt operation mentioned [here](https://azuresdkdocs.blob.core.windows.net/$web/dotnet/Azure.ResourceManager.CosmosDB/1.0.0-preview.1/api/Azure.ResourceManager.CosmosDB/Azure.ResourceManager.CosmosDB.CosmosDBManagementClient.html)

# Prerequisites

You need [an Azure subscription](https://azure.microsoft.com/en-us/free/) to run these sample programs.

Samples retrieve credentials to access the service endpoint from environment variables. Alternatively, edit the source code to include the appropriate credentials. See each individual sample for details on which environment variables/credentials it requires to function.


# Setup

To run the samples using the published version of the package:


Install the below NuGet package dependencies using dotnet:

Azure.Identity

Microsoft.Azure.Cosmos

Microsoft.Azure.Management.CosmosDB 

In addition, you also need to Include prerelease and then add Azure.ResourceManager.CosmosDB. Ensure, to check the csproj file. 


## Deployment


```dotnet
  dotnet add package Azure.Identity --version 1.7.0-beta.1

  dotnet add package Microsoft.Azure.Cosmos --version 3.29.0
  
  dotnet add package Microsoft.Azure.Management.CosmosDB --version 3.8.0-preview
  
  
```
