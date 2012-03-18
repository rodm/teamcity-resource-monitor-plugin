package teamcity.resource;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public class ResourceStartBuildPrecondition implements StartBuildPrecondition {

    private ResourceManager manager;

    ResourceStartBuildPrecondition(final ResourceManager manager) {
        this.manager = manager;
    }

    public WaitReason canStart(@NotNull QueuedBuildInfo queuedBuildInfo,
                               @NotNull Map<QueuedBuildInfo, BuildAgent> queuedBuildInfoBuildAgentMap,
                               @NotNull BuildDistributorInput buildDistributorInput,
                               boolean emulationMode)
    {
        String buildTypeId = queuedBuildInfo.getBuildConfiguration().getId();
        Loggers.SERVER.info("Build canStart check for '" + buildTypeId + "'");

        WaitReason waitReason = null;
        Collection<Resource> resources = manager.getResources().values();
        for (Resource resource : resources) {
            Loggers.SERVER.info("Resource: '" + resource.getName() + "', enabled: '" + resource.isEnabled() + "', available: '" + resource.isAvailable() + "'");
            if (!resource.isAvailable() || !resource.isEnabled()) {
                if (resource.getBuildTypes().contains(buildTypeId)) {
                    String state = resource.isEnabled() ? "available" : "enabled";
                    waitReason = new SimpleWaitReason("Build cannot start until the required resource " + resource.getName() + " is " + state);
                    Loggers.SERVER.info(waitReason.getDescription());
                }
            }
        }
        return waitReason;
    }
}
