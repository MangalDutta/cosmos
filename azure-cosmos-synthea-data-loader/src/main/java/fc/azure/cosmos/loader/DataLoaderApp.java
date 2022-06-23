package fc.azure.cosmos.loader;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.apachecommons.lang.RandomStringUtils;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.PartitionKey;
import com.bazaarvoice.jolt.Shiftr;
import org.apache.commons.cli.*;
import org.mitre.synthea.helpers.Config;

import java.io.*;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.bazaarvoice.jolt.JsonUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DataLoaderApp {
    //
    private static Shiftr shiftr = null;
    //
    public static String PATIENT_COUNT = "10000";
    public static long ITERATIONS = 10000;
    public static String SEQUENCE = "805691";
    //
    private fc.azure.cosmos.loader.Config appConfig;
    //
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private CosmosAsyncClient client;

    static {
        //
        String spec = null;
        try {
            spec = new BufferedReader(
                    new InputStreamReader(DataLoaderApp.class.getResourceAsStream("/jolt/patientspec.json"),
                            StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));;
            //URL url = DataLoaderApp.class.getResource("/jolt/patientspec.json");
            //spec = new String(Files.readAllBytes(Paths.get(url.toURI())));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        //
        shiftr = new Shiftr(JsonUtils.jsonToObject(spec));
    }

    public static void main(final String... args) throws Exception {
        //
        DataLoaderApp app = new DataLoaderApp();
        //
        CommandLine cline = app.setupOptions(args);
        if (cline != null) {
            //
            app.populateConfig(cline);
            //
            System.out.println("Started the data loader");
            //
            app.initialize();
            //
            BigInteger counter = new BigInteger(SEQUENCE);
            for (int i = 1; i <= ITERATIONS; i++) {
                System.out.println("Started loading the batch #" + i + " with " + PATIENT_COUNT + " records");
                //
                //app.loadData();
                //
                System.out.println("Transform the patient data for the batch #" + i );
                List<Patient> patients = app.transformPatient();
                //
                for (Patient patient : patients) {
                    //patient.setId(UUID.randomUUID().toString());
                    //patient.setPartitionKey("pid-" + counter.toString());
                    patient.setId(counter.toString());
                    patient.setPartitionKey("pid-" + counter.toString());
                    patient.setFirstname(RandomStringUtils.randomAlphabetic(10));
                    patient.setLastname(RandomStringUtils.randomAlphabetic(10));
                    counter = counter.add(BigInteger.ONE);
                }
                System.out.println("Finished transformation of the patient data for the batch #" + i );
                System.out.println("Start bulk load of the patient data for the batch #" + i );
                //
                app.bulkLoad(patients);
                //
                //app.deleteFolder();
                //
                System.out.println("Finished loading the batch #" + i + " with " + PATIENT_COUNT + " records");
            }
        }
        //
        System.out.println("Stopping the data loader");
        //
        System.exit(-1);
    }

    public void initialize() throws Exception {
        client = CosmosUtil.getCosmosClient(appConfig, "DataLoaderFC");
        database = CosmosUtil.createDatabaseIfNotExists(appConfig, client);
        container = CosmosUtil.createContainerIfNotExists(appConfig, client, database);
    }


    public void bulkLoad(List<Patient> patients) throws Exception {
        //
        Flux<Patient> lstPatients = Mono.just(patients).flatMapMany(Flux::fromIterable);
        //Flux<Patient> lstPatients = Flux.fromIterable(patients);
        //
        Flux<CosmosItemOperation> cosmosItemOperations = lstPatients.map(
                patient -> {
                    //System.out.println("Patient Nae "+patient.getFirstname() + " Id "+patient.getId());
                    return CosmosBulkOperations.getCreateItemOperation(patient,
                            new PartitionKey(patient.getPartitionKey()));
                });
        container.executeBulkOperations(cosmosItemOperations).blockLast();
    }

    public void deleteFolder() throws Exception {
        File index = new File("./output/fhir");
        String[]entries = index.list();
        for(String s: entries){
            //System.out.println(s);
            File currentFile = new File(index.getPath(),s);
            currentFile.delete();
        }
        index.delete();
    }

    public List<Patient> transformPatient() throws Exception {
        //
        List<Patient> patients = new ArrayList<>();
        //
        try (Stream<String> stream = Files.lines(Paths.get("./output/fhir/Patient.ndjson"))) {
            //
            stream.forEach(patient -> {
                Object transformedOutput = shiftr.transform(JsonUtils.jsonToObject(patient));
                Patient object = JsonUtils.stringToType(JsonUtils.toJsonString(transformedOutput), Patient.class);
                /*System.out.println( JsonUtils.toJsonString( transformedOutput ) );
                System.out.println("object "+object==null);
                System.out.println("object "+object.getClass().getCanonicalName());*/
                //System.out.println("Patient Name " + object.getFirstname());
                patients.add(object);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        //
        return patients;
    }

    public void loadData() throws Exception {
        String testStateDefault = Config.get("test_state.default", "Massachusetts");
        String testTownDefault = Config.get("test_town.default", "Bedford");
        String[] sargs = {"-p", PATIENT_COUNT, testStateDefault, testTownDefault, "--exporter.fhir.bulk_data=true"};
        final PrintStream original = System.out;
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream print = new PrintStream(out, true);
        //System.setOut(print);
        Class<?> cls = Class.forName("App");
        Method meth = cls.getMethod("main", String[].class);
        String[] params = null; // init params accordingly
        meth.invoke(null, (Object) sargs); // static method doesn't have an instance
        //App.main(args);
        //System.out.println("Invoked the main class ");
        //out.flush();
        //String output = out.toString();
        //System.out.println("Output "+output);
    }

    private CommandLine setupOptions(final String[] args) {

        /**
         * Usage:
         * --endpoint <cosmos account>
         * --database <database name>
         * --container <container name>
         * --key <access key>
         *
         * Examples
         * --endpoint <cosmos account> --database demo --container <container name> --key <access key>
         */
        //
        Options commandLineOptions = new Options();
        //
        commandLineOptions.addOption(Option.builder().longOpt(Constants.ENDPOINT).required(true).hasArg().desc("Endpoint").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.DATABASE).required(true).hasArg().desc("Database name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.CONTAINER).required(true).hasArg().desc("Container name").build());
        commandLineOptions.addOption(Option.builder().longOpt(Constants.KEY).required(true).hasArg().desc("Access key").build());
        //
        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = null;
        //
        try {
            commandLine = parser.parse(commandLineOptions, args);
        } catch (ParseException | NumberFormatException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("Data Loader utility options ", commandLineOptions);
        }
        //
        return commandLine;
    }

    private fc.azure.cosmos.loader.Config populateConfig(final CommandLine commandLine) {
        //
        appConfig = new fc.azure.cosmos.loader.Config();
        appConfig.setEndpoint(commandLine.getOptionValue(Constants.ENDPOINT));
        appConfig.setContainer(commandLine.getOptionValue(Constants.CONTAINER));
        appConfig.setDatabase(commandLine.getOptionValue(Constants.DATABASE));
        appConfig.setKey(commandLine.getOptionValue(Constants.KEY));
        //
        return appConfig;
    }

}
