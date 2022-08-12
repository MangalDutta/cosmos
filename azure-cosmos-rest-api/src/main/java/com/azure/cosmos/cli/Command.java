package com.azure.cosmos.cli;

import picocli.CommandLine;

@CommandLine.Command(name = "restapi", subcommands = {
        DatabaseConfig.class, CollectionConfig.class, AccountConfig.class
})
public class Command {
}
