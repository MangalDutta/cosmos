package com.azure.cosmos.cli;

import picocli.CommandLine;
import picocli.CommandLine.Option;

public class Config {
    @Option(names = {"-e", "--endpoint"}, description = "Cosmos Service endpoint", required = true, paramLabel = "https://<ENDPOINT>.documents.azure.com:443/")
    private String host;
    @CommandLine.Option(names = {"-k", "--key"}, description = "Access key", required = true, paramLabel = "<Access Key>")
    private String key;


    public String getHost() {
        return host;
    }

    public String getKey() {
        return key;
    }


}
