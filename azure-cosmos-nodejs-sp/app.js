
const CosmosClient = require("@azure/cosmos").CosmosClient;
const ClientSecretCredential = require("@azure/identity").ClientSecretCredential;
const config = require("./config");
const { endpoint, tenantID, clientID,clientSecret,databaseId,containerId } = config;
const servicePrincipal = new ClientSecretCredential(
  tenantID,
  clientID,
  clientSecret);


const client = new CosmosClient({
  endpoint: endpoint,
  aadCredentials: servicePrincipal
});

const dbContext = require("./data/databaseContext");
//  </ImportConfiguration>

//  <DefineNewItemad>
const newItem = {

  type: "Azure AD authentication",
  name: "Cosmos DB Item",
  description: "Complete Cosmos DB Node.js Quickstart ",
};
//  </DefineNewItem>

async function main() {
  
  // <CreateClientObjectDatabaseContainer>
  const { endpoint, databaseId, containerId } = config;

  //const client = new CosmosClient({ endpoint, key });

  const database = client.database(databaseId);
  const container = database.container(containerId);
  await dbContext.create(client, databaseId, containerId);
  try {
    // <QueryItems>
    console.log(`Querying container: Items`);

    // query to return all items
    const querySpec = {
      query: "SELECT * from c "
    };
    
    // read all items in the Items container
    const { resources: items } = await container.items
      .query(querySpec)
      .fetchAll();

    items.forEach(item => {
      console.log(`${item.id} - ${item.description} - ${item.name}`);
    });
    const { resource: createdItem } = await container.items.create(newItem);
    
    console.log(`\r\nCreated new item: ${createdItem.id} - ${createdItem.name}\r\n`);
    const { id, category } = createdItem;
    
  } catch (err) {
    console.log(err.message);
  }
}

main();
