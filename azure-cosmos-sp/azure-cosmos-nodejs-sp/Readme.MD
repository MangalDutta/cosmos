## **Setting up Service Principal Authentication for accessing data from the Cosmos DB SQL API using nodejs**
Azure Cosmos DB exposes a built-in role-based access control (RBAC) system that lets you:

Authenticate your data requests with an Azure Active Directory (Azure AD) identity.
Authorize your data requests with a fine-grained, role-based permission model.
Concepts
The Azure Cosmos DB data plane RBAC is built on concepts that are commonly found in other RBAC systems like [Azure RBAC](https://github.com/MicrosoftDocs/azure-docs/blob/ace3a1f12faf932e2eeb69c0bfe72041780c582e/articles/role-based-access-control/overview.md):

The [permission model](https://github.com/MicrosoftDocs/azure-docs/blob/ace3a1f12faf932e2eeb69c0bfe72041780c582e/articles/cosmos-db/how-to-setup-rbac.md#permission-model) is composed of a set of actions; each of these actions maps to one or multiple database operations. Some examples of actions include reading an item, writing an item, or executing a query.

Azure Cosmos DB users create [role definitions](https://github.com/MicrosoftDocs/azure-docs/blob/ace3a1f12faf932e2eeb69c0bfe72041780c582e/articles/cosmos-db/how-to-setup-rbac.md#role-definitions) containing a list of allowed actions.

Role definitions get assigned to specific Azure AD identities through [role assignments](https://github.com/MicrosoftDocs/azure-docs/blob/ace3a1f12faf932e2eeb69c0bfe72041780c582e/articles/cosmos-db/how-to-setup-rbac.md#role-assignments). A role assignment also defines the scope that the role definition applies to; currently, three scopes are currently:

An Azure Cosmos DB account,
An Azure Cosmos DB database,
An Azure Cosmos DB container.

**Overview**

1. Create Nodejs project
   https://docs.microsoft.com/en-us/azure/cosmos-db/sql/sql-api-nodejs-get-started
   npm init

2. Install Azure Identity Package
   https://docs.microsoft.com/en-us/javascript/api/@azure/identity/?view=azure-node-latest
   npm install @azure/identity
   
3. Install Azure Identity Package
   https://docs.microsoft.com/en-us/javascript/api/@azure/cosmos/cosmosclient?view=azure-node-latest
   npm install @azure/cosmos