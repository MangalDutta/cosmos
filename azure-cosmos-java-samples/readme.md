## **Code samples for different Cosmos features and APIs**

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
