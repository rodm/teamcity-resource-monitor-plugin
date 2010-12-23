package teamcity.resource;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.*;

import java.util.List;

public class ResourceMonitorPlugin extends BuildServerAdapter {

    private SBuildServer server;
    private String name;


    public ResourceMonitorPlugin(/* @NotNull */ SBuildServer server) {
        this.server = server;
        this.name = this.getClass().getSimpleName();
        server.addListener(this);
    }

    @Override
    public void serverStartup() {
        Loggers.SERVER.info(name + " started");

        ProjectManager pm = server.getProjectManager();
        List<SBuildType> buildTypes = pm.getAllBuildTypes();
        System.out.println(">> BuildTypes: " + buildTypes.size());
        for (SBuildType buildType : buildTypes) {
            System.out.println("    id: " + buildType.getBuildTypeId());
            System.out.println("  name: " + buildType.getName());
            System.out.println("  desc: " + buildType.getDescription());
            System.out.println("paused: " + buildType.isPaused());
            System.out.println("        " + buildType.getPauseComment());
            System.out.println("      : " + buildType.toString());
        }
    }

    @Override
    public void serverShutdown() {
//        executor.shutdown();
//        server.unregisterExtension(MainConfigProcessor.class, sourceId);
        Loggers.SERVER.info(name + " stopped");
    }

    @Override
    public void serverConfigurationReloaded() {
        Loggers.SERVER.info(name + ": server configuration reloaded");
    }
}
