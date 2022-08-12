package com.azure.cosmos.restapi;

import com.azure.cosmos.cli.*;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestVerb;
import com.azure.cosmos.implementation.ResourceType;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import picocli.CommandLine;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class RestAPI {

    private String endpoint;
    private String key;
    private static final String AUTH_PREFIX = "type=master&ver=1.0&sig=";

    public RestAPI(final String endpoint, final String key) {
        this.endpoint = endpoint;
        this.key = key;
    }

    public static void main(String... args) throws Exception {

        // If no arguments passed then show the help options
        if(args.length==0) {
            CommandLine.usage(new Command(), System.out);
            return;
        }

        CommandLine.ParseResult parseResult = null;
        CommandLine commandLine = null;
        try {
            commandLine = new CommandLine(new Command());
            //execute(commandLine, args);
            parseResult = commandLine.parseArgs(args);
        } catch (CommandLine.ParameterException paramEx) {
            CommandLine.ParameterException ex = paramEx;
            try {
                commandLine.getParameterExceptionHandler().handleParseException(ex, args);
                return;
            } catch (Exception exception) {
            }
        } catch (CommandLine.ExecutionException execEx) {
            CommandLine.ExecutionException ex = execEx;
            try {
                Exception cause = ex.getCause() instanceof Exception ? (Exception)ex.getCause() : ex;
                commandLine.getExecutionExceptionHandler().handleExecutionException((Exception)cause, ex.getCommandLine(), parseResult);
                return;
            } catch (Exception exception) {
            }
        } catch (Exception exception) {
            return;
        }

        String subCommand = parseResult.subcommand().commandSpec().name();

        Config config = (Config) handleParseResult(parseResult);

        RestAPI invocation = new RestAPI(config.getHost(), config.getKey());
        try {
            if(subCommand.equals("dbs")) {
                invocation.getAllDatabases(OperationType.LIST_ALL_DATABASES);
            } else if(subCommand.equals("colls")) {
                invocation.getAllCollections(OperationType.LIST_ALL_COLLECTIONS, ((DatabaseConfig) config).getDatabase() );
            } if(subCommand.equals("pkranges")) {
                invocation.getAllPKRanges(OperationType.LIST_ALL_PKRANGES,
                        ((CollectionConfig) config).getDatabase(),
                        ((CollectionConfig) config).getCollection());
            }
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }

    private static Object handleParseResult(final CommandLine.ParseResult parsed) {
        return parsed.subcommand().commandSpec().userObject();
    }

    public void getAllDatabases(final OperationType operationType) throws Exception {
        String uri = endpoint+"dbs/";
        execute(operationType, uri, "");
    }

    public void getAllCollections(final OperationType operationType, final String databaseRid) throws Exception {
        String uri = endpoint+"dbs/"+databaseRid+"/colls";
        execute(operationType, uri, databaseRid);
    }

    public void getAllPKRanges(final OperationType operationType, final String databaseRid, final String collectionRid) throws Exception {
//        String uri = endpoint+"dbs/"+URLEncoder.encode(databaseRid)+"/colls/"+URLEncoder.encode(collectionRid)+"/pkranges";
        String uri = endpoint+"dbs/"+databaseRid+"/colls/"+collectionRid+"/pkranges";
        String resourceId = "dbs/"+databaseRid+"/colls/"+collectionRid;
        execute(operationType, uri, resourceId);
    }

    public void execute(final OperationType operationType, final String url, final String resourceId) throws Exception {
        //System.out.println("url "+url);
        //System.out.println("ResourceType "+getResourceSegment(operationType.getResourceType()));
        //System.out.println("ResourceId "+resourceId);

        String date = getServerTime();
        String token = generate(operationType.getRequestVerb(), operationType.getResourceType(), resourceId,date);
        JsonNode jsonNode = null;
        try {
            HttpResponse<JsonNode> response = Unirest.get(url)
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("x-ms-date", date)
                    .header("x-ms-version", "2018-12-31")
                    .header("Authorization", token)
                    .asJson();
            jsonNode = response.getBody();
        }
        catch(Exception exp) {
            System.out.println(exp.getMessage());
        }
        System.out.println("result "+jsonNode);
    }




    private String getResourceSegment(ResourceType resourceType) {
        switch (resourceType) {
            case Attachment:
                return Paths.ATTACHMENTS_PATH_SEGMENT;
            case Database:
                return Paths.DATABASES_PATH_SEGMENT;
            case Conflict:
                return Paths.CONFLICTS_PATH_SEGMENT;
            case Document:
                return Paths.DOCUMENTS_PATH_SEGMENT;
            case DocumentCollection:
            case PartitionKey:
                return Paths.COLLECTIONS_PATH_SEGMENT;
            case Offer:
                return Paths.OFFERS_PATH_SEGMENT;
            case Permission:
                return Paths.PERMISSIONS_PATH_SEGMENT;
            case StoredProcedure:
                return Paths.STORED_PROCEDURES_PATH_SEGMENT;
            case Trigger:
                return Paths.TRIGGERS_PATH_SEGMENT;
            case UserDefinedFunction:
                return Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;
            case User:
                return Paths.USERS_PATH_SEGMENT;
            case PartitionKeyRange:
                return Paths.PARTITION_KEY_RANGES_PATH_SEGMENT;
            case Media:
                return Paths.MEDIA_PATH_SEGMENT;
            case DatabaseAccount:
                return "";
            case ClientTelemetry:
                return "";
            case ClientEncryptionKey:
                return Paths.CLIENT_ENCRYPTION_KEY_PATH_SEGMENT;
            default:
                return null;
        }
    }

    public String generate(RequestVerb verb,
                                  ResourceType resourceType,
                                  String resourceId,
                                  String date)
    {
        String authorization = null;
        String payload=verb.toLowerCase()+"\n"
                +getResourceSegment(resourceType).toLowerCase()+"\n"
                +resourceId+"\n"
                +date.toLowerCase()+"\n"
                +""+"\n";
        //System.out.println(payload);
        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(Base64.decode(key), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String signature = Base64.encode(sha256_HMAC.doFinal(payload.getBytes("UTF-8")));
            authorization=URLEncoder.encode(AUTH_PREFIX+signature, "UTF-8");
            //System.out.println(authorization);
        }catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return authorization;
    }

    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

}


