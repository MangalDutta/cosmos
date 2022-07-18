# Cosmos DB - Data Migration Monitor utility 

Data Migration utility is tool that can be used to monitor the data migration status between the source and the destination Cosmos collections.

## Features

- Console based utility that would fetch the progress of the data migration activity based on the configured frequency
- Show the approximate estimated time, average and total time for the data migration activity 

## Build

You would need Java JDK 8 and Maven build tool on your local environment for building this utility.

Following command to build the source code 

```sh
cd azure-cosmos-migration-monitor
mvn clean package 
```

Following command to execute the utility 
```sh
java -jar target\cosmos-migration-monitor.jar 
       --monitoredAccount <arg>        Source Cosmos Account Endpoint
       --monitoredCollection <arg>     Source Cosmos Container name
       --monitoredDatabase <arg>       Source Cosmos Database name
       --monitoredKey <arg>            Source Cosmos Access key
       --destinationAccount <arg>      Destination Cosmos Account Endpoint
       --destinationCollection <arg>   Destination Cosmos Container name
       --destinationDatabase <arg>     Destination Cosmos Database name
       --destinationKey <arg>          Destination Cosmos Access key
       --startTimeEpochMs <arg>        Migration Start Time (milliseconds)
       --migrationStatusFreq <arg>     Migration Monitor status frequency in seconds
```
`Note` 
- The `startTimeEpochMs` is the epoch time when the data migration activity was triggered (start time). Make a note of this as this would be important to calculate the average and total time required for the Data Migration activity
- The `migrationStatusFreq` is the status polling frequency in seconds, how frequently one would want to view the status updates
