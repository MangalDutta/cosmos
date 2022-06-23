package fc.azure.cosmos.loader;

import com.azure.cosmos.ConsistencyLevel;

public class Config {

    private String database;
    private String endpoint;
    private String container;
    private String key;
    private ConsistencyLevel readConsistencyLevel;
    private String leaseContainer;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ConsistencyLevel getReadConsistencyLevel() {
        return readConsistencyLevel;
    }

    public void setReadConsistencyLevel(ConsistencyLevel readConsistencyLevel) {
        this.readConsistencyLevel = readConsistencyLevel;
    }

    public String getLeaseContainer() {
        return leaseContainer;
    }

    public void setLeaseContainer(String leaseContainer) {
        this.leaseContainer = leaseContainer;
    }
}
