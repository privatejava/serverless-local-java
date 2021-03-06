/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package np.com.ngopal.serverless.local;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import np.com.ngopal.serverless.local.model.Config;
import np.com.ngopal.serverless.local.model.Serverless;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static np.com.ngopal.serverless.local.AWSProfile.processAWSProfile;

/**
 * This class is the main class for handling the commandline arguments to run the local lambda instance using docker.
 *
 * @author ngm
 */
@Slf4j
public class Runner {

    public static String getChar(int occurance, char c) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < occurance; i++) {
            builder.append(c);
        }
        return builder.toString();
    }

    public static void main(String[] args) throws IOException {

        Options options = new Options();
        options.addOption("p", true, "Project root directory");
        options.addOption("P", true, "list entries by columns");
        options.addOption("s", true, "Run as server");
        options.addOption("f", true, "Function to run");
        options.addOption("i", true, "Input json");
        options.addOption("I", true, "Docker Image");
        options.addOption("e", true, "Environment File (Default to .env of project directory)");

        CommandLineParser parser = new DefaultParser();

        String header = "Do something useful with an input file\n\n";
        String footer = "\nPlease report issues at me@ngopal.com.np";

        HelpFormatter formatter = new HelpFormatter();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            Serverless serverless = null;
            File directory = null;
            boolean executed = false;
            if (line.hasOption("p")) {
                Compiler compiler = new Compiler();
                directory = new File(line.getOptionValue("p"));
                Config.WORKING_DIR = line.getOptionValue("p");
                Config.WORKING_DIR_FILE = directory;
                log.debug("Using working dir: {}", directory.getAbsolutePath());
                compiler.executeDependency(directory);
                serverless = YamlParser.parse(line.getOptionValue("p"));
                if(serverless.getProvider().getEnvironment() == null){
                    serverless.getProvider().setEnvironment(new HashMap<>());
                }
                Config.SERVERLESS = serverless;
                Config.PROFILE_NAME = line.getOptionValue("P");
                processAWSProfile();
                File f = new File(directory + "/.env");
                if(line.hasOption("e")){
                    f = new File(line.getOptionValue("e"));
                }

                if (f.exists() && f.isFile()) {
                    String data = new String(Files.readAllBytes(f.toPath()));
                    for (String l : data.split("\n")) {
                        if (l.contains("=")) {
                            int index = l.indexOf("=");
                            String key = l.substring(0, index);
                            String value = l.substring(index + 1);
                            System.out.println("Extracted from .env " + key + ":" + getChar(value.length(), '*'));
                            Config.SERVERLESS.getProvider().getEnvironment().put(key, value);
                        }
                    }

                }


            }


            if (line.hasOption("s")) {
                executed = true;
                Config.SERVER = true;
                Config.PORT = Integer.parseInt(line.getOptionValue("s"));
                Config.IMAGE = line.getOptionValue("I") != null ? line.getOptionValue("I") : Config.IMAGE;
                log.debug("Docker Image: {}", Config.IMAGE);
                ApiServer server = new ApiServer(directory, serverless);
                server.start();

            } else if (line.hasOption("f")) {
                executed = true;

                log.debug("Executing function : {}", line.getOptionValue("f"));
                CompletableFuture<LambdaExecutor.Response> completableFuture = new CompletableFuture<>();
                Map<String, Object> input = null;
                if (line.hasOption("i")) {
                    input = UtilsFactory.get().getMapper().readValue(new File(line.getOptionValue("i")), new TypeReference<Map<String, Object>>() {
                    });
                }
                log.debug("Function: {}", line.getOptionValue("f"));
                log.debug("Input: {}", input);
                log.debug("Dir: {}", directory);
                log.debug("Future+: {}", completableFuture);
                LambdaExecutor.execute(directory, serverless.getFunction(line.getOptionValue("f")), input, completableFuture);

            }
            if (!executed) {
                formatter.printHelp("aws-local-java", header, options, footer, true);
            }


        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }

    }
}
