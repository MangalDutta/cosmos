## Program arguments

Execute Cosmos Benchmark utility using two different modes now, one with SPN based authentication and other with Master key based authentication
```
Usage: restapi [COMMAND]
Commands:
  colls
  pkranges
  dbs
```

Usage command for 'dbs'
```
Usage: restapi dbs -e=https://<ENDPOINT>.documents.azure.com:443/ -k=<Access
                   Key>
  -e, --endpoint=https://<ENDPOINT>.documents.azure.com:443/
                           Cosmos Service endpoint
  -k, --key=<Access Key>   Access key
```

Usage command for 'colls'
```
Usage: restapi colls -d=<Database Name> -e=https://<ENDPOINT>.documents.azure.
                     com:443/ -k=<Access Key>
  -d, --database=<Database Name>
                           Cosmos Database Name
  -e, --endpoint=https://<ENDPOINT>.documents.azure.com:443/
                           Cosmos Service endpoint
  -k, --key=<Access Key>   Access key
```

Usage command for 'pkranges'
```
Usage: restapi pkranges -c=<Collection Name> -d=<Database Name> -e=https:
                        //<ENDPOINT>.documents.azure.com:443/ -k=<Access Key>
  -c, --collection=<Collection Name>
                           Cosmos collection name
  -d, --database=<Database Name>
                           Cosmos Database Name
  -e, --endpoint=https://<ENDPOINT>.documents.azure.com:443/
                           Cosmos Service endpoint
  -k, --key=<Access Key>   Access key
```

## Building and running

````
mvn clean package
````

### Executing using the 'pkranges' option
````
mvn exec:java -Dexec.mainClass="com.azure.cosmos.restapi.RestAPI" -Dexec.cleanupDaemonThreads=false -Dexec.args="pkranges --endpoint=<cosmos-endpoint> --database=<database-name> --collection=<container-name> --key=<access key for the account>"
````