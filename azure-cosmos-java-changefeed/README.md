# cosmos change feed processor sample for Push and Pull model

# Setup the push model using the following steps
mvn clean package


java -jar target\cosmosdb-cfp.jar --endpoint https://albaik.documents.azure.com:443/ --database ChangeFeedDemo --container Patient --key masterkey --consistencylevel SESSION --leasecontainer Patient_Lease --host IN1



# Check the CFPullApp.java for Change Feed pull model sample 
