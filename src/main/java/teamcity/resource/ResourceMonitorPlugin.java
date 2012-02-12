package teamcity.resource;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.*;
import org.jdom.JDOMException;

import java.io.*;
import java.util.List;

public class ResourceMonitorPlugin extends BuildServerAdapter {

    private SBuildServer server;

    private ResourceMonitor monitor;

    private ResourceManager resourceManager;

    private String name;

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
    }

    @Override
    public void serverShutdown() {
        Loggers.SERVER.info(name + " stopped");
    }

    @Override
    public void serverConfigurationReloaded() {
        Loggers.SERVER.info(name + ": server configuration reloaded");
    }

    public void loadConfiguration() {
        try {
            ResourceMonitorConfigProcessor configProcessor = new ResourceMonitorConfigProcessor(resourceManager);
            configProcessor.readFrom(new FileReader(getConfigDirFile()));
            monitor.scheduleMonitor();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveConfiguration() {
        try {
            ResourceMonitorConfigProcessor configProcessor = new ResourceMonitorConfigProcessor(resourceManager);
            configProcessor.writeTo(new FileWriter(getConfigDirFile()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getConfigDirFile() {
        return new File(server.getConfigDir(), "resources.xml");
    }
}
