## **Setting up Service Principal for accessing data from the Cosmos account using SQL API**

**Prerequisites** 
1. Create one CosmosDB account with one dummy database and collection
2. Setup Service Principal using this [link](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal)
3. Make sure Java 8 and Maven build are configured on your system 

**Define roles for the service principal**

1. Built-in role - Azure Cosmos DB exposes 2 built-in role definitions:
   1. Cosmos DB Built-in Data Reader
   2. Cosmos DB Built-in Data Contributor

    The details for which are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#built-in-role-definitions) 

2. Custom-roles - Azure Cosmos DB allows custom roles for real-only or read-write or other combination. The details for the same are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#role-definitions)

__NOTE__ - This permission model covers only database operations that involve reading and writing data. It does not cover any kind of management operations on management resources, for example:
* Create/Replace/Delete Database
* Create/Replace/Delete Container
* Replace Container Throughput
* Create/Replace/Delete/Read Stored Procedures
* Create/Replace/Delete/Read Triggers
* Create/Replace/Delete/Read User Defined Functions

You would need to use the Azure RBAC, details are available [here](https://docs.microsoft.com/en-us/azure/cosmos-db/how-to-setup-rbac#permission-model)

**Overview**

The role-based access control with Azure Active Directory for your Azure Cosmos DB account basically requires the [Azure Identity library](https://docs.microsoft.com/en-us/java/api/overview/azure/identity-readme?view=azure-java-stable) to support Azure Active Directory (AAD) token authentication. 

The AAD token authentication feature is supported for azure cosmos Java libraries **4.6.0** and above. 

This is sample code to get you started on integrating, configure and use the AAD token authentication for your SQL Cosmos Accounts.

**Build**

This source code use maven for build management, use the following command for building the source code

`mvn clean package`

**Build**

Execute the command using the below command

`java -DCLIENT_ID=$CLIENT_ID -DCLIENT_SECRET=$CLIENT_SECRET -DTENANT_ID=$TENANT_ID -DACCOUNT_HOST=$HOST -DDATABASE=$DATABASE -DCONTAINER=$CONTAINER -jar target\cosmosdb-sp-uber.jar`

where,
* CLIENT_ID is the client-id for the service principal
* CLIENT_SECRET is the client-secret for the service principal
* TENANT_ID is the tenant identifier for the Azure subscription
* ACCOUNT_HOST is the fully-qualified host details that you can get from the Azure portal
* DATABASE is the Cosmos Database 
* CONTAINER is the Cosmos Container
