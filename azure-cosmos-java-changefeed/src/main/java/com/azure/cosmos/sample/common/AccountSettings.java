// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.sample.common;

import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Contains the account configurations for Sample.
 * 
 * For running tests, you can pass a customized endpoint configuration in one of the following
 * ways:
 * <ul>
 * <li>-DCLIENT_ID="[your-client-id]" -CLIENT_SECRET="[your-client-secret]" -TENANT_ID="[your-tenant-id]"
 * -ACCOUNT_HOST="[your-endpoint]" -ACCOUNT_HOST="[your-endpoint]" -ACCOUNT_HOST="[your-endpoint]"
 * -ACCOUNT_KEY="[access-key]" -WORKER_NAME="[worker-name]"  -DREGION="[region]"
 * as JVM command-line option.</li>
 * <li>You can set all of these as environment variables.</li>
 * </ul>
 * 
 * If none of the above is set, emulator endpoint will be used.
 * Emulator http cert is self-signed. If you are using emulator,
 * make sure emulator https certificate is imported
 * to java trusted cert store:
 * https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates
 */
public class AccountSettings {
    // Replace MASTER_KEY and HOST with values from your Azure Cosmos DB account.
    // The default values are credentials of the local emulator, which are not used in any production environment.
    private static final String defaultDatabaseName = "ToDoList";
    private static final String defaultContainerName = "Items";

    //
    public static String PKEY =
            System.getProperty("PKEY",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("PKEY")), RandomStringUtils.randomAlphabetic(10)));
    //
    public static String LEASE_NAME =
            System.getProperty("LEASE_NAME",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("LEASE_NAME")),"Items-leases"));
    //
    public static String CONTINUATION_TOKEN =
            System.getProperty("TOKEN",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("TOKEN"))));

    //
    public static String REPLAY_BY_TIME =
            System.getProperty("STARTTIME",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("STARTTIME"))));

    //
    public static String LEASE_PREFIX =
            System.getProperty("LEASE_PREFIX",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("LEASE_PREFIX"))));
    //
    public static String CFP_WORKER =
            System.getProperty("WORKER_NAME",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("WORKER_NAME")),"DummyHost"));
    //
    public static String CLIENT_ID =
            System.getProperty("CLIENT_ID",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("CLIENT_ID"))));
    //
    public static String REGION =
            System.getProperty("REGION",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("REGION")),"East US"));
    //
    public static String CLIENT_SECRET =
            System.getProperty("CLIENT_SECRET",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("CLIENT_SECRET"))));
    //
    public static String TENANT_ID =
            System.getProperty("TENANT_ID",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("TENANT_ID"))));
    //
    public static String HOST =
            System.getProperty("ACCOUNT_HOST",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("ACCOUNT_HOST"))));
    //
    public static String KEY =
            System.getProperty("ACCOUNT_KEY",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("ACCOUNT_KEY"))));
    //
    public static String DATABASE =
            System.getProperty("DATABASE",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("DATABASE")), defaultDatabaseName));
    //
    public static String CONTAINER =
            System.getProperty("CONTAINER",
                    StringUtils.defaultString(StringUtils.trimToNull(
                            System.getenv().get("CONTAINER")), defaultContainerName));
}
