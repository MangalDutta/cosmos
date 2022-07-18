package com.azure.cosmos.migrationmonitor;

import com.azure.cosmos.ConsistencyLevel;

public class Config {

    private String monitoredDatabase;
    private String monitoredAccount;
    private String monitoredCollection;
    private String monitoredKey;
    private String destinationDatabase;
    private String destinationAccount;
    private String destinationCollection;
    private String destinationKey;
    private long startTimeEpochMs;
    private long migrationStatusFreq;

    public String getMonitoredDatabase() {
        return monitoredDatabase;
    }

    public void setMonitoredDatabase(String monitoredDatabase) {
        this.monitoredDatabase = monitoredDatabase;
    }

    public String getMonitoredAccount() {
        return monitoredAccount;
    }

    public void setMonitoredAccount(String monitoredAccount) {
        this.monitoredAccount = monitoredAccount;
    }

    public String getMonitoredCollection() {
        return monitoredCollection;
    }

    public void setMonitoredCollection(String monitoredCollection) {
        this.monitoredCollection = monitoredCollection;
    }

    public String getMonitoredKey() {
        return monitoredKey;
    }

    public void setMonitoredKey(String monitoredKey) {
        this.monitoredKey = monitoredKey;
    }

    public String getDestinationDatabase() {
        return destinationDatabase;
    }

    public void setDestinationDatabase(String destinationDatabase) {
        this.destinationDatabase = destinationDatabase;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public String getDestinationCollection() {
        return destinationCollection;
    }

    public void setDestinationCollection(String destinationCollection) {
        this.destinationCollection = destinationCollection;
    }

    public String getDestinationKey() {
        return destinationKey;
    }

    public void setDestinationKey(String destinationKey) {
        this.destinationKey = destinationKey;
    }

    public void setMigrationStatusFreq(long migrationStatusFreq) {
        this.migrationStatusFreq = migrationStatusFreq;
    }

    public long getMigrationStatusFreq() {
        return migrationStatusFreq;
    }

    public void setStartTimeEpochMs(long startTimeEpochMs) {
        this.startTimeEpochMs = startTimeEpochMs;
    }

    public long getStartTimeEpochMs() {
        return startTimeEpochMs;
    }
}
