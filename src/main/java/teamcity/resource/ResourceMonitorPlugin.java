package teamcity.resource;

import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.serverSide.*;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.jdom.JDOMException;

import java.io.*;

public class ResourceMonitorPlugin extends BuildServerAdapter implements ChangeListener {

    public static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ResourceMonitorPlugin.class.getCanonicalName());

    private ResourceMonitor monitor;

    private ResourceManager resourceManager;

    private String name;

    private FileWatcher fileWatcher;

    private String configDir;

    public ResourceMonitorPlugin(SBuildServer server, ResourceMonitor monitor, ResourceManager resourceManager, ServerPaths serverPaths) {
        this.resourceManager = resourceManager;
        this.name = this.getClass().getSimpleName();
        this.monitor = monitor;
        server.addListener(this);

        File logDir = serverPaths.getLogsPath();
        configDir = serverPaths.getConfigDir();
        FileAppender appender = new FileAppender();
        appender.setName("ResourceMonitorLogger");
        appender.setFile(logDir + File.separator + "resource-monitor.log");
        appender.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        appender.setThreshold(Level.ALL);
        appender.setAppend(true);
        appender.activateOptions();
        log.addAppender(appender);
        log.setLevel(Level.DEBUG);
    }

    @Override
    public void serverStartup() {
        log.info(name + " started");
        loadConfiguration();

        fileWatcher = new FileWatcher(getConfigurationFile());
        fileWatcher.registerListener(this);
        fileWatcher.start();
    }

    @Override
    public void serverShutdown() {
        fileWatcher.stop();
        log.info(name + " stopped");
    }

    @Override
    public void buildTypeUnregistered(SBuildType buildType) {
    }

    public void changeOccured(String requestor) {
        log.debug("Reloading configuration");
        loadConfiguration();
    }

    public void loadConfiguration() {
        File configurationFile = getConfigurationFile();
        log.info("Loading configuration from location : " + configurationFile.getAbsolutePath());
        try {
            ResourceMonitorConfigProcessor configProcessor = new ResourceMonitorConfigProcessor(resourceManager);
            configProcessor.readFrom(new FileReader(configurationFile));
            monitor.scheduleMonitor();
        } catch (JDOMException e) {
            log.error("Error loading resources configuration file", e);
        } catch (FileNotFoundException e) {
            log.warn("Resource configuration file not found");
        } catch (IOException e) {
            log.error("Error loading resources configuration file", e);
        }
    }

    public void saveConfiguration() throws IOException {
        File configurationFile = getConfigurationFile();
        log.info("Saving configuration to location : " + configurationFile.getAbsolutePath());
        try {
            fileWatcher.setSkipListenersNotification(true);
            ResourceMonitorConfigProcessor configProcessor = new ResourceMonitorConfigProcessor(resourceManager);
            configProcessor.writeTo(new FileWriter(configurationFile));
        }
        finally {
            fileWatcher.resetChanged();
            fileWatcher.setSkipListenersNotification(false);
        }
    }

    private File getConfigurationFile() {
        return new File(configDir, "resources.xml");
    }
}
