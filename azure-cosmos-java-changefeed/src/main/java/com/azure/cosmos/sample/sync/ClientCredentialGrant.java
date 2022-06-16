package com.azure.cosmos.sample.sync;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Set;

public class ClientCredentialGrant {
    private final static String AUTHORITY = "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/";
    private final static String CLIENT_ID = "f12a5d7b-a2b6-4ab7-a2d7-0a4426df7567";
    private final static String CLIENT_SECRET = "IOp7Q~2DRFhoUZusmFSkSJcslrQ5tsXSbEcPV";
    private final static Set<String> SCOPE = Collections.singleton("https://database.windows.net//.default");
    private final static String ALIAS = "FC";

    public static void main(String args[]) throws Exception {
        IAuthenticationResult result = acquireToken();
        System.out.println("Access token: " + result.accessToken());
        IAuthenticationResult result2 = me("FC");
        System.out.println("Access token: " + result2.accessToken());
    }

    //
    public static IClientCredential getCredentialFromClientSecret() {
        // This is the secret that is created in the Azure portal when registering the application
        IClientCredential credential = ClientCredentialFactory.createFromSecret(CLIENT_SECRET);
        return credential;
    }
    //
    public static IClientCredential getCredentialFromClientCertificate() throws Exception {
        KeyStore ks = KeyStore.getInstance("Windows-MY");
        // Note: When a security manager is installed,
        // the following call requires SecurityPermission
        // "authProvider.SunMSCAPI".
        ks.load(null, null);

        // String alias = "FC";

        PrivateKey privKey = (PrivateKey) ks.getKey(ALIAS, null);
        System.out.println("privKey == null "+(privKey==null));
        X509Certificate cert = (X509Certificate) ks.getCertificate(ALIAS);
        System.out.println("cert == null "+(cert==null) + " cert "+cert.getClass().getCanonicalName());
        //
        System.out.println("Cert "+cert.toString());
        //
        return ClientCredentialFactory.createFromCertificate(privKey, cert);
    }


    public static IAuthenticationResult acquireToken(IClientCredential credential) throws Exception {

        ConfidentialClientApplication cca =
                ConfidentialClientApplication
                        .builder(CLIENT_ID, credential)
                        .authority(AUTHORITY)
                        .build();

        // Client credential requests will by default try to look for a valid token in the
        // in-memory token cache. If found, it will return this token. If a token is not found, or the
        // token is not valid, it will fall back to acquiring a token from the AAD service. Although
        // not recommended unless there is a reason for doing so, you can skip the cache lookup
        // by using .skipCache(true) in ClientCredentialParameters.
        ClientCredentialParameters parameters =
                ClientCredentialParameters
                        .builder(SCOPE)
                        .build();

        return cca.acquireToken(parameters).join();
    }

    public static IAuthenticationResult me(final String alias) throws Exception {
        KeyStore ks = KeyStore.getInstance("Windows-MY");
        // Note: When a security manager is installed,
        // the following call requires SecurityPermission
        // "authProvider.SunMSCAPI".
        ks.load(null, null);

       // String alias = "FC";

        PrivateKey privKey = (PrivateKey) ks.getKey(alias, null);
        System.out.println("privKey == null "+(privKey==null));
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        System.out.println("cert == null "+(cert==null) + " cert "+cert.getClass().getCanonicalName());
        //
        System.out.println("Cert "+cert.toString());

        IClientCredential credential = ClientCredentialFactory.createFromCertificate(privKey, cert);
        ConfidentialClientApplication cca =
                ConfidentialClientApplication
                        .builder(CLIENT_ID, credential)
                        .authority(AUTHORITY)
                        .build();

        // Client credential requests will by default try to look for a valid token in the
        // in-memory token cache. If found, it will return this token. If a token is not found, or the
        // token is not valid, it will fall back to acquiring a token from the AAD service. Although
        // not recommended unless there is a reason for doing so, you can skip the cache lookup
        // by using .skipCache(true) in ClientCredentialParameters.
        ClientCredentialParameters parameters =
                ClientCredentialParameters
                        .builder(SCOPE)
                        .build();

        return cca.acquireToken(parameters).join();

    }

    public static IAuthenticationResult acquireToken() throws Exception {

        // This is the secret that is created in the Azure portal when registering the application
        IClientCredential credential = ClientCredentialFactory.createFromSecret(CLIENT_SECRET);
        ConfidentialClientApplication cca =
                ConfidentialClientApplication
                        .builder(CLIENT_ID, credential)
                        .authority(AUTHORITY)
                        .build();

        // Client credential requests will by default try to look for a valid token in the
        // in-memory token cache. If found, it will return this token. If a token is not found, or the
        // token is not valid, it will fall back to acquiring a token from the AAD service. Although
        // not recommended unless there is a reason for doing so, you can skip the cache lookup
        // by using .skipCache(true) in ClientCredentialParameters.
        ClientCredentialParameters parameters =
                ClientCredentialParameters
                        .builder(SCOPE)
                        .build();

        return cca.acquireToken(parameters).join();
    }
}
