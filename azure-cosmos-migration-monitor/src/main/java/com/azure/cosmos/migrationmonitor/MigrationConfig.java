package com.azure.cosmos.migrationmonitor;

public class MigrationConfig {

    private String sourcePartitionKeys;

    private String targetPartitionKey;

    private String monitoredAccount;

    private String monitoredDbName;

    private String monitoredCollectionName;

    private String destAccount;

    private String destDbName;

    private String destCollectionName;

    private double dataAgeInHours;

    private boolean Completed;

    private boolean OnlyInsertMissingItems;

    private String Id;

    private long StartTimeEpochMs;

    private long MigratedDocumentCount;

    private long ExpectedDurationLeft;

    private double AvgRate;

    private double CurrentRate;

    private long SourceCountSnapshot;

    private long DestinationCountSnapshot;

    private double PercentageCompleted;

    private long StatisticsLastUpdatedEpochMs;

    private long StatisticsLastMigrationActivityRecordedEpochMs;

}
