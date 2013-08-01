package teamcity.resource;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.serverSide.BuildPromotion;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildDistribution.*;
import jetbrains.buildServer.users.User;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ResourceBuildLimitStartPreconditionTest {

    private static final String RESOURCE_ID = "1";
    private static final long BUILD_ID_1 = 12345L;
    private static final long BUILD_ID_2 = 12346L;
    private static final boolean EMULATION_MODE_OFF = false;
    private static final boolean EMULATION_MODE_ON = true;

    private Resource resource = new Resource(RESOURCE_ID, "test", "localhost", 1234);

    private SBuildServer buildServer;
    private ResourceManager resourceManager;
    private ResourceBuildLimitStartPrecondition precondition;
    private QueuedBuildInfo queuedBuildInfo = mock(QueuedBuildInfo.class);
    private BuildConfigurationInfo buildConfigurationInfo = mock(BuildConfigurationInfo.class);
    private BuildPromotionInfo buildPromotionInfo = mock(BuildPromotionInfo.class);
    private Map<QueuedBuildInfo, BuildAgent> agentMap = Collections.emptyMap();
    private BuildDistributorInput buildDistributorInput = mock(BuildDistributorInput.class);
    private SRunningBuild build = mock(SRunningBuild.class);

    @Before
    public void setup() {
        buildServer = mock(SBuildServer.class);
        resourceManager = new ResourceManager(null);
        precondition = new ResourceBuildLimitStartPrecondition(buildServer, resourceManager);

        resource.addBuildType("bt123");
        resource.addBuildType("bt124");
        resourceManager.addResource(resource);

        when(build.getBuildTypeId()).thenReturn("bt123");
        when(buildPromotionInfo.getId()).thenReturn(BUILD_ID_1);
        when(queuedBuildInfo.getBuildPromotionInfo()).thenReturn(buildPromotionInfo);
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

        QueuedBuildInfo queuedBuildInfo2 = mock(QueuedBuildInfo.class);
        BuildPromotionInfo buildPromotionInfo2 = mock(BuildPromotionInfo.class);
        when(buildPromotionInfo2.getId()).thenReturn(BUILD_ID_2);
        when(queuedBuildInfo2.getBuildPromotionInfo()).thenReturn(buildPromotionInfo2);
        when(queuedBuildInfo2.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo2, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNotNull(waitReason);
    }


    @Test
    public void shouldIncreaseUsageCountForBuildAllocatedToResource() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");
        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);

        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertEquals(1, precondition.getBuildCount(RESOURCE_ID));
    }

    @Test
    public void shouldReturnNullWaitReasonAfterBuildFinishes() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");
        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);

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
        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);

        // use resource build limit
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);

        precondition.buildInterrupted(build);
        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnZeroUsageForResourceAfterBuildRemovedFromQueueByUser() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");
        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);

        SQueuedBuild queuedBuild = mock(SQueuedBuild.class);
        when(queuedBuild.getBuildTypeId()).thenReturn("bt124");
        when(queuedBuild.getBuildPromotion()).thenReturn(buildPromotion);
        User user = mock(User.class);

        // use resource build limit
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);

        precondition.buildRemovedFromQueue(queuedBuild, user, "comment");

        assertEquals(0, precondition.getBuildCount(RESOURCE_ID));
    }

    @Test
    public void shouldNotReduceUsageForResourceAfterBuildRemovedFromQueueByServer() {
        resource.setBuildLimit(1);
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");
        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);

        SQueuedBuild queuedBuild = mock(SQueuedBuild.class);
        when(queuedBuild.getBuildTypeId()).thenReturn("bt124");
        when(queuedBuild.getBuildPromotion()).thenReturn(buildPromotion);
        User nullUser = null;

        // use resource build limit
        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);

        precondition.buildRemovedFromQueue(queuedBuild, nullUser, null);

        assertEquals(1, precondition.getBuildCount(RESOURCE_ID));
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

        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);
        when(buildServer.getRunningBuilds()).thenReturn(runningBuilds);

        precondition.serverStartup();

        assertEquals(1, precondition.getBuildCount(RESOURCE_ID));

        precondition.buildFinished(build);
        assertEquals("finished build should return count to zero", 0, precondition.getBuildCount(RESOURCE_ID));
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
        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);

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
        BuildPromotion buildPromotion = mock(BuildPromotion.class);
        when(buildPromotion.getId()).thenReturn(BUILD_ID_1);
        when(build.getBuildPromotion()).thenReturn(buildPromotion);

        precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, EMULATION_MODE_OFF);

        ResourceUsageListener listener = mock(ResourceUsageListener.class);
        precondition.addListener(listener);
        precondition.buildInterrupted(build);

        verify(listener).resourceUsageChanged(same(resource), eq(0));
    }
}
