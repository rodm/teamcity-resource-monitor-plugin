package teamcity.resource;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ResourceBuildLimitStartPrecondition extends BuildServerAdapter implements StartBuildPrecondition {

    private ResourceManager manager;

    private Map<String, Integer> resourceBuildCounts = new HashMap<String, Integer>();

    ResourceBuildLimitStartPrecondition(SBuildServer buildServer, final ResourceManager manager) {
        buildServer.addListener(this);
        this.manager = manager;
    }

    public WaitReason canStart(@NotNull QueuedBuildInfo queuedBuildInfo,
                               @NotNull Map<QueuedBuildInfo, BuildAgent> queuedBuildInfoBuildAgentMap,
                               @NotNull BuildDistributorInput buildDistributorInput,
                               boolean emulationMode)
    {
        String buildTypeId = queuedBuildInfo.getBuildConfiguration().getId();
        Loggers.SERVER.debug("Build canStart check for '" + buildTypeId + "'");

        WaitReason waitReason = null;
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            int buildLimit = resource.getBuildLimit();
            if (buildLimit > 0) {
                int currentBuilds = getBuildCount(resource.getId());

                if (currentBuilds >= buildLimit) {
                    waitReason = new SimpleWaitReason("Build cannot start until the number of builds using the resource "
                            + resource.getName() + " is below the limit of " + buildLimit);
                    Loggers.SERVER.debug(waitReason.getDescription());
                }
            }
        }
        return waitReason;
    }

    @Override
    public void buildStarted(SRunningBuild build) {
        String buildTypeId = build.getBuildTypeId();
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            Integer buildCount = resourceBuildCounts.get(resource.getId());
            if (buildCount == null) {
                buildCount = 1;
            } else {
                buildCount = buildCount + 1;
            }
            resourceBuildCounts.put(resource.getId(), buildCount);
        }
    }

    @Override
    public void buildFinished(SRunningBuild build) {
        String buildTypeId = build.getBuildTypeId();
        Resource resource = manager.findResourceByBuildTypeId(buildTypeId);
        if (resource != null) {
            Integer buildCount = resourceBuildCounts.get(resource.getId());
            if (buildCount == null) {
                buildCount = 0;
            } else {
                buildCount = buildCount - 1;
            }
            resourceBuildCounts.put(resource.getId(), buildCount);
        }
    }

    private int getBuildCount(String id) {
        Integer buildCount = resourceBuildCounts.get(id);
        return (buildCount == null) ? 0 : buildCount;
    }
}
