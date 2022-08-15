using Azure.Identity;
using Azure.ResourceManager.CosmosDB;
using Azure.ResourceManager.CosmosDB.Models;
using Microsoft.Azure.Cosmos;
using System;
using System.Threading.Tasks;
using Microsoft.Azure.Management.CosmosDB;

namespace CosmosDBMgmtTest
{
    class Program
    {
        static async Task Main(string[] args)
        {
            var subscriptionId = "Your subscription ID";
            var resourceGroupName = "You resource group";
            var accountName = "Cosmos DB Account name";
            var databaseName = "Provide the Database Name";
            var containerName = "Provide the container name";

 var tokenCredential = new DefaultAzureCredential();

            // create the management clientSS
            var managementClient = new Azure.ResourceManager.CosmosDB.CosmosDBManagementClient(subscriptionId, tokenCredential);


            // create the data client if you want to create new items and read the same. 
            //var dataClient = new CosmosClient("https://[Account].documents.azure.com:443/", tokenCredential);

          // create a new database 
            var createDatabaseOperation = await managementClient.SqlResources.StartCreateUpdateSqlDatabaseAsync(resourceGroupName, accountName, databaseName,
            new SqlDatabaseCreateUpdateParameters(new SqlDatabaseResource(databaseName), new CreateUpdateOptions()));
            await createDatabaseOperation.WaitForCompletionAsync();

            // create a new container
            var createContainerOperation = await managementClient.SqlResources.StartCreateUpdateSqlContainerAsync(resourceGroupName, accountName, databaseName, containerName,
            new SqlContainerCreateUpdateParameters(new SqlContainerResource(containerName), new CreateUpdateOptions()));
            await createContainerOperation.WaitForCompletionAsync();

        
        }
    }
}
