package teamcity.resource;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.comments.Comment;
import jetbrains.buildServer.users.User;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ResourceMonitor implements Runnable {

    private static final String PLUGIN_NAME = "ResourceMonitorPlugin";

    private static final int DEFAULT_INTERVAL = 30;

    private static final int INITIAL_DELAY = 1;

    private static final Logger log = Loggers.SERVER;

    private Map<String, Resource> resources = new HashMap<String, Resource>();
    
    private int interval = DEFAULT_INTERVAL;

    private SBuildServer server;
    private ProjectManager projectManager;
    private ScheduledFuture<?> future;

    public ResourceMonitor() {
        log.info("ResourceMonitor() default constructor");
    }

    public ResourceMonitor(@NotNull SBuildServer server, ProjectManager projectManager) {
        log.info("ResourceMonitor(SBuildServer, ProjectManager) constructor");
        this.server = server;
        this.projectManager = projectManager;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public void scheduleMonitor() {
        log.info(PLUGIN_NAME + ": monitor check interval set to " + interval + "seconds");
        if (future != null) {
            future.cancel(false);
        }
        ScheduledExecutorService executor = server.getExecutor();
        future = executor.scheduleAtFixedRate(this, INITIAL_DELAY, interval, TimeUnit.SECONDS);
    }

    public void run() {
        int unavailable = 0;
        for (Resource resource : resources.values()) {
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
        if (resources.containsKey(resource.getName())) {
            throw new IllegalArgumentException("resource with name " + resource.getName() + " already exists");
        }
        resources.put(resource.getName(), resource);
    }

    public void setResources(Map<String,Resource> resources) {
        this.resources = resources;
    }

    public Map<String,Resource> getResources() {
        return resources;
    }
}
