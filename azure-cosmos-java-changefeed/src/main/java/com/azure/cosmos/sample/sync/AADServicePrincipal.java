package com.azure.cosmos.sample.sync;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

/*
https://github.com/microsoft/sql-server-samples/tree/master/samples/features/security/azure-active-directory-auth/token
https://stackoverflow.com/questions/62513972/onprem-machines-to-azure-active-directory-so-we-can-access-activedirectorymsi-au
https://docs.microsoft.com/en-us/sql/connect/ado-net/sql/azure-active-directory-authentication?view=sql-server-ver15


"C:\Program Files (x86)\Windows Kits\10\bin\10.0.22000.0\x86\MakeCert.exe" -r -pe -n "CN=FC" -ss My -len 2048 sp.cer

Connect-MsolService

$cer = New-Object System.Security.Cryptography.X509Certificates.X509Certificate
$cer.Import("C:\Users\faizchachiya\sp.cer")
$binCert = $cer.GetRawCertData()
$credValue = [System.Convert]::ToBase64String($binCert);
New-MsolServicePrincipalCredential -AppPrincipalId "f12a5d7b-a2b6-4ab7-a2d7-0a4426df7567" -Type asymmetric -Value $credValue -Usage verify

Get-MsolServicePrincipalCredential -ServicePrincipalName "f12a5d7b-a2b6-4ab7-a2d7-0a4426df7567" -ReturnKeyValues 0




 */
public class AADServicePrincipal {
    public static void main(String[] args) throws Exception{
        String principalId = "f12a5d7b-a2b6-4ab7-a2d7-0a4426df7567"; // Replace with your client ID.
        String principalSecret = "IOp7Q~2DRFhoUZusmFSkSJcslrQ5tsXSbEcPV"; // Replace with your client secret.

        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("sqlserverforsec.database.windows.net"); // Replace with your server name
        ds.setDatabaseName("sqldbsec"); // Replace with your database
        //ds.setAuthentication("ActiveDirectoryPassword");
        //ds.setAuthentication("ActiveDirectoryServicePrincipal");
        //ds.setUser(principalId); // setAADSecurePrincipalId for JDBC Driver 9.4 and below
        //ds.setPassword(principalSecret); // setAADSecurePrincipalSecret for JDBC Driver 9.4 and below.
        //
        ds.setAccessToken(ClientCredentialGrant
                .acquireToken(ClientCredentialGrant.getCredentialFromClientCertificate())
//                .acquireToken(ClientCredentialGrant.getCredentialFromClientSecret())
                .accessToken());
        //
        try (Connection connection = ds.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUSER_SNAME()")) {
            if (rs.next()) {
                System.out.println("You have successfully logged on as: " + rs.getString(1));
            }
        }
    }

}