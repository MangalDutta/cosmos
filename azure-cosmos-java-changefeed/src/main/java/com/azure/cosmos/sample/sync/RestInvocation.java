package com.azure.cosmos.sample.sync;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpRequest;
import com.azure.cosmos.implementation.*;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.asynchttpclient.*;

import static com.azure.cosmos.implementation.HttpConstants.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RestInvocation {

    private static final String AUTH_PREFIX = "type=master&ver=1.0&sig=";

    private static final String baseURL = "https://velocity.documents.azure.com";

    private AzureKeyCredential KEY = new AzureKeyCredential("oXf30U3jU0hoevJLZP4aymx59fOfvg8fl0eLcbzawQGB98BF7HnOQgX98mum0jeeOxJfcN9K6IS84o3SHxy7yA==");

    public static void main(String... args) throws Exception {
        RestInvocation invocation = new RestInvocation();
        invocation.listDatabases();;
    }

    public void listDatabases() throws Exception {
        //
        final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        //
        String date = Utils.nowAsRFC1123();
        builder.put(HttpHeaders.X_DATE, date);
        //
        final String token = new BaseAuthorizationTokenProvider(KEY)
                .generateKeyAuthorizationSignature(RequestVerb.GET,
                        Paths.DATABASES_ROOT,
                        ResourceType.Database,
                        builder.build());
        System.out.println("token "+token);
        System.out.println("date "+date);
        System.out.println("token "+URLEncoder.encode(token));

        //
        Request request = new RequestBuilder(HttpConstants.HttpMethods.GET)
                .setUrl(baseURL+"/dbs")
                .setHeader("Accept", "application/json")
                .setHeader(HttpHeaders.X_DATE, date)
                .setHeader("authorization", token)
                .build();
        //
        final AsyncHttpClient client = new DefaultAsyncHttpClient();
        //
        final int statusCode = client.executeRequest(request, new AsyncCompletionHandler<Response>() {
            @Override
            public Response onCompleted(final Response response) {
                System.out.println(response.getResponseBody());
                return response;
            }
        }).get(10, TimeUnit.SECONDS).getStatusCode();

    }
/*
    private String generateMasterKeyAuthorizationSignature(final AzureKeyCredential credential,
                                                           final RequestVerb verb,
                                                           final String resourceIdOrFullName,
                                                           final String resourceSegment) {
        //
        byte[] masterKeyBytes = credential.getKey().getBytes(StandardCharsets.UTF_8);
        byte[] masterKeyDecodedBytes = Utils.Base64Decoder.decode(masterKeyBytes);
        SecretKey signingKey = new SecretKeySpec(masterKeyDecodedBytes, "HMACSHA256");
        //
        Mac macInstance = null;
        try {
            macInstance = Mac.getInstance("HMACSHA256");
            macInstance.init(signingKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
        //Paths.DATABASE_ACCOUNT_PATH_SEGMENT;
        //ResourceType.DatabaseAccount
        final Map<String, String> builder = new HashMap<>();
        //
        builder.put(HttpHeaders.X_DATE, Utils.nowAsRFC1123());
        //
        String payload = verb.toLowerCase() + "\n";
        // Skipping lower casing of resourceId since it may now contain "ID" of the resource as part of the FullName
        StringBuilder body = new StringBuilder();
        body.append(ModelBridgeInternal.toLower(verb))
                .append('\n')
                .append(resourceSegment)
                .append('\n')
                .append(resourceIdOrFullName)
                .append('\n');
        //
        if (builder.containsKey(HttpConstants.HttpHeaders.X_DATE)) {
            body.append(builder.get(HttpConstants.HttpHeaders.X_DATE).toLowerCase(Locale.ROOT));
        }
        body.append('\n');
        //
        if (builder.containsKey(HttpConstants.HttpHeaders.HTTP_DATE)) {
            body.append(builder.get(HttpConstants.HttpHeaders.HTTP_DATE).toLowerCase(Locale.ROOT));
        }

        body.append('\n');
        //
        byte[] digest = macInstance.doFinal(body.toString().getBytes(StandardCharsets.UTF_8));
        String auth = Utils.encodeBase64String(digest);
        return AUTH_PREFIX + auth;
    }*/

    /*
    string GenerateMasterKeyAuthorizationSignature(HttpMethod verb, ResourceType resourceType, string resourceLink, string date, string key)
{
    var keyType = "master";
    var tokenVersion = "1.0";
    var payload = $"{verb.ToString().ToLowerInvariant()}\n{resourceType.ToString().ToLowerInvariant()}\n{resourceLink}\n{date.ToLowerInvariant()}\n\n";

    var hmacSha256 = new System.Security.Cryptography.HMACSHA256 { Key = Convert.FromBase64String(key) };
    var hashPayload = hmacSha256.ComputeHash(System.Text.Encoding.UTF8.GetBytes(payload));
    var signature = Convert.ToBase64String(hashPayload);
    var authSet = WebUtility.UrlEncode($"type={keyType}&ver={tokenVersion}&sig={signature}");

    return authSet;
}
     */

}
