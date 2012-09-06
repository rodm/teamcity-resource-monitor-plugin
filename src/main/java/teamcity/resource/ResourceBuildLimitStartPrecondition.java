package teamcity.resource;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceBuildLimitStartPrecondition extends BuildServerAdapter
        implements StartBuildPrecondition, ResourceManagerListener
{

    private SBuildServer buildServer;

    private ResourceManager manager;

    private Map<String, ResourceBuildCount> resourceBuildCounts = new HashMap<String, ResourceBuildCount>();

    private List<ResourceUsageListener> listeners = new ArrayList<ResourceUsageListener>();

    ResourceBuildLimitStartPrecondition(SBuildServer buildServer, final ResourceManager manager) {
        this.buildServer = buildServer;
        this.manager = manager;
        buildServer.addListener(this);
    }

    public void addListener(ResourceUsageListener listener) {
        listeners.add(listener);
    }

    public WaitReason canStart(@NotNull QueuedBuildInfo queuedBuildInfo,
                               @NotNull Map<QueuedBuildInfo, BuildAgent> canBeStarted,
                               @NotNull BuildDistributorInput buildDistributorInput,
                               boolean emulationMode)
    {
        String buildTypeId = queuedBuildInfo.getBuildConfiguration().getId();
        Loggers.SERVER.debug("Build canStart check for '" + buildTypeId + "'");

        WaitReason waitReason = null;
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());

            int buildLimit = resource.getBuildLimit();
            if (buildLimit > 0) {
                int currentBuilds = resourceBuildCount.getValue();

                if (currentBuilds >= buildLimit) {
                    waitReason = new SimpleWaitReason("Build cannot start until the number of builds using the resource "
                            + resource.getName() + " is below the limit of " + buildLimit);
                    Loggers.SERVER.debug(waitReason.getDescription());
                }
            }
            if (!emulationMode && waitReason == null) {
                resourceBuildCount.increment();
                Loggers.SERVER.info("Running builds using resource " + resource.getName() + " - " + resourceBuildCount.getValue());
                notifyListeners(resource, resourceBuildCount.getValue());
            }
        }
        return waitReason;
    }

    @Override
    public void serverStartup() {
        for (SRunningBuild build : buildServer.getRunningBuilds()) {
            Resource resource = manager.findResourceByBuildTypeId(build.getBuildTypeId());
            if (resource != null) {
                ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
                resourceBuildCount.increment();
                Loggers.SERVER.info("Incremented resource usage for running build: " + build.getFullName());
            }
        }
    }

    @Override
    public void buildFinished(SRunningBuild build) {
        buildCompleted(build);
    }

    @Override
    public void buildInterrupted(SRunningBuild build) {
        buildCompleted(build);
    }

    public void resourceAdded(Resource resource) {
    }

    public void resourceUpdated(Resource resource) {
    }

    public void resourceRemoved(Resource resource) {
        resourceBuildCounts.remove(resource.getId());
    }

    public int getBuildCount(String id) {
        return getResourceBuildCount(id).getValue();
    }

    private void buildCompleted(SRunningBuild build) {
        String buildTypeId = build.getBuildTypeId();
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
            resourceBuildCount.decrement();
            notifyListeners(resource, resourceBuildCount.getValue());
            Loggers.SERVER.info("Running builds using resource " + resource.getName() + " - " + resourceBuildCount.getValue());
        }
    }

    private ResourceBuildCount getResourceBuildCount(String id) {
        ResourceBuildCount buildCount = resourceBuildCounts.get(id);
        if (buildCount == null) {
            buildCount = new ResourceBuildCount();
            resourceBuildCounts.put(id, buildCount);
        }
        return buildCount;
    }

    private void notifyListeners(Resource resource, int count) {
        for (ResourceUsageListener listener : listeners) {
            listener.resourceUsageChanged(resource, count);
        }
    }
}

class ResourceBuildCount {
    private int value = 0;

    public synchronized int getValue() {
        return value;
    }

    public synchronized void increment() {
        value++;
    }

    public synchronized void decrement() {
        if (value > 0) value--;
    }
}
