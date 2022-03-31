package com.cosmos.aad.sync;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import com.azure.spring.data.cosmos.repository.config.EnableReactiveCosmosRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.lang.Nullable;

@Configuration
@EnableConfigurationProperties(CosmosProperties.class)
@EnableCosmosRepositories
@EnableReactiveCosmosRepositories
@PropertySource("classpath:application.properties")
public class SampleAppConfiguration extends AbstractCosmosConfiguration {
    @Autowired
    private CosmosProperties properties;

    @Bean
    public CosmosClientBuilder cosmosClientBuilder() {
        //
        DirectConnectionConfig directConnectionConfig = DirectConnectionConfig.getDefaultConfig();
        //
        if (properties.isAadBasedAuth()) {
            //
            ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                    .clientId(properties.getClientId())
                    .clientSecret(properties.getClientSecret())
                    .tenantId(properties.getTenantId())
                    .build();
            //
            return new CosmosClientBuilder()
                    .endpoint(properties.getUri())
                    .credential(clientSecretCredential)
                    .directMode(directConnectionConfig);

        } else {
            return new CosmosClientBuilder()
                    .endpoint(properties.getUri())
                    .key(properties.getKey())
                    .directMode(directConnectionConfig);

        }
    }

    @Bean
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                .enableQueryMetrics(properties.isQueryMetricsEnabled())
                .build();
    }

    @Override
    protected String getDatabaseName() {
        return properties.getDatabase();
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            System.out.println("Response Diagnostics {}" + responseDiagnostics);
        }
    }
}
