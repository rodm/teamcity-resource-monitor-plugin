package teamcity.resource;

import jetbrains.buildServer.configuration.ChangeListener;
import jetbrains.buildServer.configuration.FileWatcher;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.*;
import org.jdom.JDOMException;

import java.io.*;
import java.util.List;

public class ResourceMonitorPlugin extends BuildServerAdapter implements ChangeListener {

    private SBuildServer server;

    private ResourceMonitor monitor;

    private ResourceManager resourceManager;

    private String name;

    private FileWatcher fileWatcher;

    public ResourceMonitorPlugin(SBuildServer server, ResourceMonitor monitor, ResourceManager resourceManager) {
        this.server = server;
        this.resourceManager = resourceManager;
        this.name = this.getClass().getSimpleName();
        this.monitor = monitor;
        server.addListener(this);
    }

    @Override
    public void serverStartup() {
        Loggers.SERVER.info(name + " started");

        ProjectManager projectManager = server.getProjectManager();
        List<SBuildType> buildTypes = projectManager.getAllBuildTypes();
        System.out.println(">> BuildTypes: " + buildTypes.size());
        for (SBuildType buildType : buildTypes) {
            System.out.println("    id: " + buildType.getBuildTypeId());
            System.out.println("  name: " + buildType.getName());
            System.out.println("  desc: " + buildType.getDescription());
            System.out.println("paused: " + buildType.isPaused());
            System.out.println("        " + buildType.getPauseComment());
            System.out.println("      : " + buildType.toString());
        }

        loadConfiguration();

        fileWatcher = new FileWatcher(getConfigurationFile());
        fileWatcher.registerListener(this);
        fileWatcher.start();
    }

    @Override
    public void serverShutdown() {
        fileWatcher.stop();
        Loggers.SERVER.info(name + " stopped");
    }

    @Override
    public void buildTypeUnregistered(SBuildType buildType) {
        String buildTypeId = buildType.getBuildTypeId();
        Loggers.SERVER.debug("Unregistering build type: " + buildTypeId);
        resourceManager.unregisterBuild(buildTypeId);
        try {
            saveConfiguration();
        } catch (IOException e) {
            Loggers.SERVER.error("Error saving resources configuration file", e);
        }
    }

    public void changeOccured(String requestor) {
        Loggers.SERVER.debug("Reloading configuration");
        loadConfiguration();
    }

    public void loadConfiguration() {
        try {
            ResourceMonitorConfigProcessor configProcessor = new ResourceMonitorConfigProcessor(resourceManager);
            configProcessor.readFrom(new FileReader(getConfigurationFile()));
            monitor.scheduleMonitor();
        } catch (JDOMException e) {
            Loggers.SERVER.error("Error loading resources configuration file", e);
        } catch (FileNotFoundException e) {
            Loggers.SERVER.warn("Resource configuration file not found");
        } catch (IOException e) {
            Loggers.SERVER.error("Error loading resources configuration file", e);
        }
    }

    public void saveConfiguration() throws IOException {
        ResourceMonitorConfigProcessor configProcessor = new ResourceMonitorConfigProcessor(resourceManager);
        configProcessor.writeTo(new FileWriter(getConfigurationFile()));
    }

    private File getConfigurationFile() {
        return new File(server.getConfigDir(), "resources.xml");
    }
}
