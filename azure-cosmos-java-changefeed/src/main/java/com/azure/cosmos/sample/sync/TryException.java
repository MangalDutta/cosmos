package com.azure.cosmos.sample.sync;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.spring.data.cosmos.exception.CosmosAccessException;

import java.net.URI;

public class TryException {

    public static void main(String... args) throws Exception {

        System.out.println("START");

        getException(new RetryWithException("", new HttpHeaders(), new URI("")));

        System.out.println("STOP");

    }

    public static void getException(Throwable throwable) {
/*
        if (throwable instanceof CosmosException) {
            log(((CosmosException) throwable).getDiagnostics());
        }*/

        if (throwable instanceof GoneException) {
            System.out.println("Logging Diagnostics for GoneException for container :{} => {}" +
                    ((GoneException) throwable).getDiagnostics());
        } else if (throwable instanceof RetryWithException) {
            System.out.println("Logging Diagnostics for RetryWithException for container : {} => {}"
                    + ((RetryWithException) throwable).getDiagnostics());
        } else if (throwable instanceof CosmosAccessException) {
            System.out.println("Logging Diagnostics for CosmosAccessException for container : {} => {}" + ((CosmosAccessException) throwable).getCosmosException());
        }

    }

}
