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


## Output

On execution of the utility, if the Cosmos Source and Destination accounts have been configured properly then you would see the following messages showing up on the console based on the frequency specified in the parameter

```sh
2022-07-18 16:34:10,646 [pool-6-thread-1] INFO com.azure.cosmos.migrationmonitor.MigrationMonitor - 
	 Percentage Complete : 90.00
	 Total records inserted 1800 out of 2000
	 Total record pending : 0
	 Average Rate : 15.0 
	 Current Rate : 250.00 
	 Estimated Time (sec) : 100
	 Total Time (sec) : 19769
```
`Note` 
- `Average Rate` is calculated based on the total elapsed time so the start time is key for computing this value 
- `Estimated Time` depends on the inserted records and the average time 

## RU Cost
Queries are made to the Source and Destination Cosmos container based on the configured frequency and each request would cost an `1 RU`