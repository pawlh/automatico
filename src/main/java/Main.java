import edu.byu.cs.autograder.GradingException;
import edu.byu.cs.server.endpointprovider.EndpointProvider;
import edu.byu.cs.server.endpointprovider.EndpointProviderImpl;
import edu.byu.cs.dataAccess.DaoService;
import edu.byu.cs.dataAccess.DataAccessException;
import edu.byu.cs.properties.ApplicationProperties;
import edu.byu.cs.server.Server;
import edu.byu.cs.service.SubmissionService;
import edu.byu.cs.util.ResourceUtils;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class Main {
    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static EndpointProvider endpointProvider = new EndpointProviderImpl();

    public static void main(String[] args) {
        ResourceUtils.copyResourceFiles("phases", new File(""));
        setupProperties(args);

        try {
            DaoService.initializeSqlDAOs();
        } catch (DataAccessException e) {
            LOGGER.error("Error setting up database", e);
            throw new RuntimeException(e);
        }

        new Server(endpointProvider).start();

        try {
            SubmissionService.reRunSubmissionsInQueue();
        } catch (IOException | DataAccessException | GradingException e) {
            LOGGER.error("Error rerunning submissions already in queue", e);
        }
    }

    private static void setupProperties(String[] args) {
        Options options = getOptions();

        Properties properties = new Properties();

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("db-host")) {
                properties.setProperty("db-host", cmd.getOptionValue("db-host"));
            }
            if (cmd.hasOption("db-port")) {
                properties.setProperty("db-port", cmd.getOptionValue("db-port"));
            }
            if (cmd.hasOption("db-name")) {
                properties.setProperty("db-name", cmd.getOptionValue("db-name"));
            }
            if (cmd.hasOption("db-user")) {
                properties.setProperty("db-user", cmd.getOptionValue("db-user"));
            }
            if (cmd.hasOption("db-pass")) {
                properties.setProperty("db-pass", cmd.getOptionValue("db-pass"));
            }
            if (cmd.hasOption("frontend-url")) {
                properties.setProperty("frontend-url", cmd.getOptionValue("frontend-url"));
            }
            if (cmd.hasOption("cas-callback-url")) {
                properties.setProperty("cas-callback-url", cmd.getOptionValue("cas-callback-url"));
            }
            if (cmd.hasOption("canvas-token")) {
                properties.setProperty("canvas-token", cmd.getOptionValue("canvas-token"));
            }
            if (cmd.hasOption("use-canvas")) {
                properties.setProperty("use-canvas", cmd.getOptionValue("use-canvas"));
            }
            if (cmd.hasOption("disable-compilation")) {
                properties.setProperty("run-compilation", "false");
            }
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing command line arguments", e);
        }

        ApplicationProperties.loadProperties(properties);
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption(null, "db-host", true, "Database Host");
        options.addOption(null, "db-port", true, "Database Port");
        options.addOption(null, "db-name", true, "Database Name");
        options.addOption(null, "db-user", true, "Database User");
        options.addOption(null, "db-pass", true, "Database Password");
        options.addOption(null, "frontend-url", true, "Frontend URL");
        options.addOption(null, "cas-callback-url", true, "CAS Callback URL");
        options.addOption(null, "canvas-token", true, "Canvas Token");
        options.addOption(null, "use-canvas", true, "Using Canvas");
        options.addOption(null, "disable-compilation", false, "Turn off student code compilation");
        return options;
    }

}
