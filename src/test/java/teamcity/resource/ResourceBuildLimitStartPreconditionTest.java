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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResourceBuildLimitStartPreconditionTest {

    private static final String RESOURCE_ID = "1";
    private static final boolean EMULATION_MODE_OFF = false;
    private static final boolean EMULATION_MODE_ON = true;

    private Resource resource = new Resource(RESOURCE_ID, "test", "localhost", 1234);

    private SBuildServer buildServer;
    private ResourceManager resourceManager;
    private ResourceBuildLimitStartPrecondition precondition;
    private QueuedBuildInfo queuedBuildInfo = mock(QueuedBuildInfo.class);
    private BuildConfigurationInfo buildConfigurationInfo = mock(BuildConfigurationInfo.class);
    private Map<QueuedBuildInfo, BuildAgent> agentMap = Collections.emptyMap();
    private BuildDistributorInput buildDistributorInput = mock(BuildDistributorInput.class);
    private SRunningBuild build;

    @Before
    public void setup() {
        buildServer = mock(SBuildServer.class);
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
    public void shouldReturnNullWaitReasonWhenBuildIsNotLinkedToResource() {
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt125");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonBuildCountDoesNotExceedResourceBuildLimit() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnWaitReasonWhenBuildCountExceedsResourceBuildLimit() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        // use resource build limit
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNotNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonAfterBuildFinishes() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        // use resource build limit
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);

        precondition.buildFinished(build);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonAfterBuildIsInterrupted() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        // use resource build limit
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);

        precondition.buildInterrupted(build);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonWhenResourceBuildLimitIsZero() {
        resource.setBuildLimit(0);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNull(waitReason);
        waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNull(waitReason);
    }

    @Test
    public void shouldUpdateBuildCountAtStartupForRunningBuilds() {
        List<SRunningBuild> runningBuilds = new ArrayList<SRunningBuild>();
        runningBuilds.add(build);
        when(buildServer.getRunningBuilds()).thenReturn(runningBuilds);

        precondition.serverStartup();

        assertEquals(1, precondition.getBuildCount(RESOURCE_ID));
    }

    @Test
    public void shouldReturnZeroBuildCountAfterRemovingResource() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertEquals(1, precondition.getBuildCount(resource.getId()));

        precondition.resourceRemoved(resource);
        assertEquals(0, precondition.getBuildCount(resource.getId()));
    }

    @Test
    public void shouldSendUsageChangedEventOnBuildAllocationForMonitoredResourceWithLimit() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        ResourceUsageListener listener = mock(ResourceUsageListener.class);
        precondition.addListener(listener);
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);

        verify(listener).resourceUsageChanged(same(resource), eq(1));
    }

    @Test
    public void shouldNotIncrementBuildCountWhenEmulationModeIsOn() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_ON);
        assertEquals(0, precondition.getBuildCount(resource.getId()));
    }

    @Test
    public void shouldNotSendUsageChangedEventOnBuildAllocationWhenEmulationModeIsOn() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        ResourceUsageListener listener = mock(ResourceUsageListener.class);
        precondition.addListener(listener);
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_ON);

        verifyZeroInteractions(listener);
    }

    @Test
    public void shouldSendUsageChangedEventOnBuildAllocationForMonitoredResourceWithoutLimit() {
        resource.setBuildLimit(0);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        ResourceUsageListener listener = mock(ResourceUsageListener.class);
        precondition.addListener(listener);
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);

        verify(listener).resourceUsageChanged(same(resource), eq(1));
    }

    @Test
    public void shouldNotSendUsageChangedEventWhenBuildIsNotLinkedToResource() {
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt125");

        ResourceUsageListener listener = mock(ResourceUsageListener.class);
        precondition.addListener(listener);
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);

        verifyZeroInteractions(listener);
    }

    @Test
    public void shouldSendUsageChangedEventOnBuildFinished() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);

        ResourceUsageListener listener = mock(ResourceUsageListener.class);
        precondition.addListener(listener);
        precondition.buildFinished(build);

        verify(listener).resourceUsageChanged(same(resource), eq(0));
    }

    @Test
    public void shouldSendUsageChangedEventOnBuildInterrupted() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);

        ResourceUsageListener listener = mock(ResourceUsageListener.class);
        precondition.addListener(listener);
        precondition.buildInterrupted(build);

        verify(listener).resourceUsageChanged(same(resource), eq(0));
    }
}
