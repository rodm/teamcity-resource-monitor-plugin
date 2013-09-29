package teamcity.resource;

import static teamcity.resource.ResourceMonitorPlugin.log;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ResourceBuildLimitStartPrecondition extends BuildServerAdapter
        implements StartBuildPrecondition, ResourceManagerListener
{

    private ResourceManager manager;

    private Map<String, ResourceBuildCount> resourceBuildCounts = new HashMap<String, ResourceBuildCount>();

    private List<ResourceUsageListener> listeners = new ArrayList<ResourceUsageListener>();

    ResourceBuildLimitStartPrecondition(SBuildServer buildServer, final ResourceManager manager) {
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

        WaitReason waitReason = null;
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            int allocatedBuilds = calculateAllocatedBuilds(resource.getId(), canBeStarted);
            ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());

            int buildLimit = resource.getBuildLimit();
            if (buildLimit > 0) {
                int currentBuilds = resourceBuildCount.size();

                if (currentBuilds + allocatedBuilds >= buildLimit) {
                    waitReason = new SimpleWaitReason("Build cannot start until the number of builds using or "
                            + "allocated to the resource " + resource.getName()
                            + " is below the limit of " + buildLimit);
                    log.trace(waitReason.getDescription());
                }
            }
        }
        return waitReason;
    }

    int calculateAllocatedBuilds(String resourceId, Map<QueuedBuildInfo, BuildAgent> canBeStarted) {
        int allocated = 0;
        for (Map.Entry<QueuedBuildInfo, BuildAgent> entry : canBeStarted.entrySet()) {
            String buildTypeId = entry.getKey().getBuildConfiguration().getId();
            Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
            if (resource != null && resourceId.equals(resource.getId())) {
                allocated++;
            }
        }
        return allocated;
    }

    @Override
    public void agentRegistered(SBuildAgent agent, long currentlyRunningBuildId) {
        SRunningBuild build = agent.getRunningBuild();
        if (build != null) {
            Resource resource = manager.findResourceByBuildTypeId(build.getBuildTypeId());
            if (resource != null) {
                ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
                resourceBuildCount.allocate(build.getBuildPromotion().getId());
                log.info("Running builds using resource " + resource.getName() + ": " + resourceBuildCount.size());
            }
        }
    }

    @Override
    public void beforeAgentUnregistered(SBuildAgent agent) {
        SRunningBuild build = agent.getRunningBuild();
        if (build != null) {
            Resource resource = manager.findResourceByBuildTypeId(build.getBuildTypeId());
            if (resource != null) {
                ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
                resourceBuildCount.release(build.getBuildPromotion().getId());
                log.info("Running builds using resource " + resource.getName() + ": " + resourceBuildCount.size());
            }
        }
    }

    @Override
    public void buildStarted(SRunningBuild build) {
        Resource resource = manager.findResourceByBuildTypeId(build.getBuildTypeId());
        if (resource != null) {
            long buildPromotionId = build.getBuildPromotion().getId();
            ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
            resourceBuildCount.allocate(buildPromotionId);
            notifyListeners(resource, resourceBuildCount.size());
            log.info("Running builds using resource " + resource.getName() + ": " + resourceBuildCount.size());
            log.debug("Build " + build.getFullName() + " #" + build.getBuildNumber()
                    + " (id: " + build.getBuildPromotion().getId() + ") started using resource " + resource.getName());
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

    private void buildCompleted(SRunningBuild build) {
        String buildTypeId = build.getBuildTypeId();
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            long buildPromotionId = build.getBuildPromotion().getId();
            ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
            boolean removed = resourceBuildCount.release(buildPromotionId);
            if (!removed) {
                log.warn("Failed to release build " + buildPromotionId + " from using resource " + resource.getName());
            }
            notifyListeners(resource, resourceBuildCount.size());
            log.info("Running builds using resource " + resource.getName() + ": " + resourceBuildCount.size());
            log.debug("Build " + build.getFullName() + " #" + build.getBuildNumber()
                    + " (id: " + buildPromotionId + ") finished using resource " + resource.getName());
        }
    }

    public void resourceAdded(Resource resource) {
    }

    public void resourceUpdated(Resource resource) {
    }

    public void resourceRemoved(Resource resource) {
        synchronized (resourceBuildCounts) {
            resourceBuildCounts.remove(resource.getId());
        }
    }

    public int getBuildCount(String id) {
        return getResourceBuildCount(id).size();
    }

    ResourceBuildCount getResourceBuildCount(String id) {
        synchronized (resourceBuildCounts) {
            ResourceBuildCount buildCount = resourceBuildCounts.get(id);
            if (buildCount == null) {
                buildCount = new ResourceBuildCount();
                resourceBuildCounts.put(id, buildCount);
            }
            return buildCount;
        }
    }

    private void notifyListeners(Resource resource, int count) {
        for (ResourceUsageListener listener : listeners) {
            listener.resourceUsageChanged(resource, count);
        }
    }
}

class ResourceBuildCount {

    private Set<Long> builds = new HashSet<Long>();

    public int size() {
        return builds.size();
    }

    public boolean contains(long buildId) {
        return builds.contains(buildId);
    }

    public void allocate(long buildId) {
        builds.add(buildId);
    }

    public boolean release(long buildId) {
        return builds.remove(buildId);
    }
}
