package teamcity.resource;

import static teamcity.resource.ResourceMonitorPlugin.log;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import jetbrains.buildServer.users.User;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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

        WaitReason waitReason = null;
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            long buildPromotionId = queuedBuildInfo.getBuildPromotionInfo().getId();
            ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());

            if (!resourceBuildCount.contains(buildPromotionId)) {
                int buildLimit = resource.getBuildLimit();
                if (buildLimit > 0) {
                    int currentBuilds = resourceBuildCount.size();

                    if (currentBuilds >= buildLimit) {
                        waitReason = new SimpleWaitReason("Build cannot start until the number of builds using the resource "
                                + resource.getName() + " is below the limit of " + buildLimit);
                        log.trace(waitReason.getDescription());
                    }
                }
                if (!emulationMode && waitReason == null) {
                    resourceBuildCount.allocate(buildPromotionId);
                    log.info("Running builds using resource " + resource.getName() + ": " + resourceBuildCount.size());
                    log.debug("Build " + getBuildTypeFullName(buildTypeId)
                            + " (id: " + buildPromotionId + ") allocated to resource " + resource.getName());
                    notifyListeners(resource, resourceBuildCount.size());
                }
            }
        }
        return waitReason;
    }

    private String getBuildTypeFullName(String buildTypeId) {
        String fullName = "";
        ProjectManager projectManager = buildServer.getProjectManager();
        if (projectManager != null) {
            SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
            if (buildType != null) {
                fullName = buildType.getFullName();
            }
        }
        return fullName;
    }

    @Override
    public void serverStartup() {
        for (SRunningBuild build : buildServer.getRunningBuilds()) {
            Resource resource = manager.findResourceByBuildTypeId(build.getBuildTypeId());
            if (resource != null) {
                ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
                resourceBuildCount.allocate(build.getBuildPromotion().getId());
                log.info("Running builds using resource " + resource.getName() + ": " + resourceBuildCount.size());
            }
        }
    }

    @Override
    public void buildRemovedFromQueue(@NotNull SQueuedBuild queued, User user, String comment) {
        if (user != null) {
            String buildTypeId = queued.getBuildTypeId();
            Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
            if (resource != null) {
                long buildPromotionId = queued.getBuildPromotion().getId();
                ResourceBuildCount resourceBuildCount = getResourceBuildCount(resource.getId());
                resourceBuildCount.release(buildPromotionId);
                notifyListeners(resource, resourceBuildCount.size());
                log.info("Running builds using resource " + resource.getName() + ": " + resourceBuildCount.size());
            }
        }
    }

    @Override
    public void buildStarted(SRunningBuild build) {
        Resource resource = manager.findResourceByBuildTypeId(build.getBuildTypeId());
        if (resource != null) {
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

    private ResourceBuildCount getResourceBuildCount(String id) {
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
