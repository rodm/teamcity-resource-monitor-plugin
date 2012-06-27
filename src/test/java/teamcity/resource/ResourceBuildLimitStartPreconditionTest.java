package teamcity.resource;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildDistribution.BuildConfigurationInfo;
import jetbrains.buildServer.serverSide.buildDistribution.BuildDistributorInput;
import jetbrains.buildServer.serverSide.buildDistribution.QueuedBuildInfo;
import jetbrains.buildServer.serverSide.buildDistribution.WaitReason;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResourceBuildLimitStartPreconditionTest {

    private Resource resource = new Resource("1", "test", "localhost", 1234);
    private ResourceManager resourceManager;
    private ResourceBuildLimitStartPrecondition precondition;
    private QueuedBuildInfo queuedBuildInfo = mock(QueuedBuildInfo.class);
    private BuildConfigurationInfo buildConfigurationInfo = mock(BuildConfigurationInfo.class);
    private Map<QueuedBuildInfo, BuildAgent> agentMap = Collections.emptyMap();
    private BuildDistributorInput buildDistributorInput = mock(BuildDistributorInput.class);
    private SRunningBuild build;

    @Before
    public void setup() {
        SBuildServer buildServer = mock(SBuildServer.class);
        resourceManager = new ResourceManager(null);
        precondition = new ResourceBuildLimitStartPrecondition(buildServer, resourceManager);

        resource.addBuildType("bt123");
        resource.addBuildType("bt124");
        resourceManager.addResource(resource);

        build = mock(SRunningBuild.class);
        when(build.getBuildTypeId()).thenReturn("bt123");
    }

    @Test
    public void shouldRegisterWithBuildServer() {
        SBuildServer buildServer = mock(SBuildServer.class);
        precondition = new ResourceBuildLimitStartPrecondition(buildServer, resourceManager);

        verify(buildServer).addListener(same(precondition));
    }

    @Test
    public void shouldReturnNullWaitReasonWhenBuildIsNotLinkedToResource () {
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt125");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnWaitReasonWhenBuildCountExceedsResourceBuildLimit() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.buildStarted(build);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNotNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReason() {
        resource.setBuildLimit(1);
        Resource resource2 = new Resource("2", "test2", "localhost", 1234);
        resource2.setBuildLimit(1);
        resource2.addBuildType("bt125");
        resourceManager.addResource(resource2);

        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt125");

        precondition.buildStarted(build); // start build linked to first resource

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonWhenResourceBuildLimitIsZero() {
        resource.setBuildLimit(0);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.buildStarted(build);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonAfterBuildFinishes() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.buildStarted(build);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNotNull(waitReason);

        precondition.buildFinished(build);
        waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonAfterBuildIsInterrupted() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.buildStarted(build);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNotNull(waitReason);

        precondition.buildInterrupted(build);
        waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonWhenResourceIsUnavailable() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }
}
