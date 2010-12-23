package teamcity.resource;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.comments.Comment;
import jetbrains.buildServer.users.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ResourceMonitor implements Runnable {

    private static final String PLUGIN_NAME = "ResourceMonitorPlugin";

    private static final Logger log1 = Logger.getInstance(ResourceMonitor.class.getName());
    private static final Logger log = Loggers.SERVER;

    private List<Resource> resources = new ArrayList<Resource>();
    
    private int interval = 30;

    private SBuildServer server;
    private ProjectManager projectManager;
    private ScheduledFuture<?> f;

    public ResourceMonitor() {
        log.info("ResourceMonitor() default constructor");
    }

    public ResourceMonitor(@NotNull SBuildServer server, ProjectManager projectManager) {
        log.info("ResourceMonitor(SBuildServer, ProjectManager) constructor");
        this.server = server;
        this.projectManager = projectManager;
//        server.registerExtension(MainConfigProcessor.class, this.getClass().getSimpleName(), this);
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public void scheduleMonitor() {
        log.info(PLUGIN_NAME + ": monitor check interval set to " + interval + "seconds");
        if (f != null) {
            f.cancel(false);
        }
        ScheduledExecutorService executor = server.getExecutor();
        f = executor.scheduleAtFixedRate(this, 1, interval, TimeUnit.SECONDS);
    }

    public void run() {
        int unavailable = 0;
        for (Resource resource : resources) {
            if (resource.isAvailable()) {
                resourceAvailable(resource);
            } else {
                resourceUnavailable(resource);
                unavailable++;
            }
        }
        log.info("Monitored resources: " + resources.size() + ", unavailable: " + unavailable);
    }

    public void resourceAvailable(Resource resource) {
        List<String> buildTypes = resource.getBuildTypes();
        for (String buildTypeId : buildTypes) {
            SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
            if (buildType != null) {
                if (canActivate(buildType)) {
                    User user = getUser();
                    String comment = "Resource " + resource.getName() + " is available, build activated by " + PLUGIN_NAME;
                    buildType.setPaused(false, user, comment);
                    String message = "Resource " + resource.getName() + " is available, build '" + buildType.getFullName() + "' activated by " + PLUGIN_NAME;
                    log.info(message);
                }
            }
        }
    }

    private boolean canActivate(SBuildType buildType) {
        boolean result = false;
        if (buildType.isPaused()) {
            Comment comment = buildType.getPauseComment();
            if (comment != null) {
                String commentText = comment.getComment();
                result = commentText != null && commentText.contains(PLUGIN_NAME); 
            }
        }
        return result;
    }

    public void resourceUnavailable(Resource resource) {
        List<String> buildTypes = resource.getBuildTypes();
        for (String buildTypeId : buildTypes) {
            SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
            if (buildType != null) {
                if (!buildType.isPaused()) {
                    User user = getUser();
                    String comment = "Resource " + resource.getName() + " is unavailable, build de-activated by " + PLUGIN_NAME;
                    buildType.setPaused(true, user, comment);
                    String message = "Resource " + resource.getName() + " is unavailable, build '" + buildType.getFullName() + "' de-activated by " + PLUGIN_NAME;
                    log.info(message);
                }
            }
        }
    }

    private User getUser() {
        return new ResourceUser();
    }

    public void addResource(Resource resource) {
        resources.add(resource);
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<Resource> getResources() {
        return resources;
    }
}