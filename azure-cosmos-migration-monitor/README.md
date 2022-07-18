# cosmos change feed processor sample for Push and Pull model

# Setup the push model using the following steps
mvn clean package


java -jar target\cosmosdb-cfp.jar --endpoint https://albaik.documents.azure.com:443/ --database ChangeFeedDemo --container Patient --key oXf30U3jU0hoevJLZP4aymx59fOfvg8fl0eLcbzawQGB98BF7HnOQgX98mum0jeeOxJfcN9K6IS84o3SHxy7yA== --consistencylevel SESSION --leasecontainer Patient_Lease --host IN1



# Check the CFPullApp.java for Change Feed pull model sample 