package teamcity.resource;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ResourceMonitor implements Runnable {

    private static final String PLUGIN_NAME = "ResourceMonitorPlugin";

    private static final int INITIAL_DELAY = 1;

    private static final Logger log = Loggers.SERVER;

    private SBuildServer server;

    private ResourceManager resourceManager;

    private AvailabilityChecker checker;

    private ScheduledFuture<?> future;

    private List<ResourceMonitorListener> listeners = new ArrayList<ResourceMonitorListener>();

    private Set<String> unavailableResources = new HashSet<String>();

    private Set<String> disabledResources = new HashSet<String>();

    private enum ResourceEvent { Available, Unavailable, Enabled, Disabled }

    public ResourceMonitor(@NotNull SBuildServer server, ResourceManager resourceManager, AvailabilityChecker checker) {
        log.info("ResourceMonitor(SBuildServer, ProjectManager, ResourceManager) constructor");
        this.server = server;
        this.resourceManager = resourceManager;
        this.checker = checker;
    }

    public void scheduleMonitor() {
        int interval = resourceManager.getInterval();
        log.info(PLUGIN_NAME + ": monitor check interval set to " + interval + "seconds");
        if (future != null) {
            future.cancel(false);
        }
        ScheduledExecutorService executor = server.getExecutor();
        future = executor.scheduleAtFixedRate(this, INITIAL_DELAY, interval, TimeUnit.SECONDS);
    }

    public void addListener(ResourceMonitorListener listener) {
        listeners.add(listener);
    }

    public void enableResource(Resource resource) {
        if (disabledResources.remove(resource.getId())) {
            notifyListeners(ResourceEvent.Enabled, resource);
        }
    }

    public void disableResource(Resource resource) {
        if (disabledResources.add(resource.getId())) {
            notifyListeners(ResourceEvent.Disabled, resource);
        }
    }

    public void run() {
        int enabled = 0;
        int available = 0;
        for (Resource resource : getResources()) {
            if (isEnabled(resource)) {
                enabled++;
            }
            if (checker.isAvailable(resource)) {
                resourceAvailable(resource);
                available++;
            } else {
                resourceUnavailable(resource);
            }
        }
        log.info("Monitored resources: " + getResources().size() + ", enabled: " + enabled + ", available: " + available);
    }

    public boolean isAvailable(Resource resource) {
        return !unavailableResources.contains(resource.getId());
    }

    public boolean isEnabled(Resource resource) {
        return !disabledResources.contains(resource.getId());
    }

    private void resourceAvailable(Resource resource) {
        if (unavailableResources.remove(resource.getId())) {
            notifyListeners(ResourceEvent.Available, resource);
        }
    }

    private void resourceUnavailable(Resource resource) {
        if (unavailableResources.add(resource.getId())) {
            notifyListeners(ResourceEvent.Unavailable, resource);
        }
    }

    private void notifyListeners(ResourceEvent event, Resource resource) {
        for (ResourceMonitorListener listener : listeners) {
            switch (event) {
                case Available:
                    listener.resourceAvailable(resource);
                    break;
                case Unavailable:
                    listener.resourceUnavailable(resource);
                    break;
                case Enabled:
                    listener.resourceEnabled(resource);
                    break;
                case Disabled:
                    listener.resourceDisabled(resource);
                    break;
            }
        }
    }

    private Collection<Resource> getResources() {
        return resourceManager.getResources().values();
    }
}
