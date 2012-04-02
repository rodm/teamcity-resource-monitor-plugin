package teamcity.resource;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.serverSide.buildDistribution.*;

import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ResourceStartBuildPreconditionTest {

    private ResourceManager resourceManager;
    private ResourceStartBuildPrecondition precondition;
    private QueuedBuildInfo queuedBuildInfo = mock(QueuedBuildInfo.class);
    private BuildConfigurationInfo buildConfigurationInfo = mock(BuildConfigurationInfo.class);
    private Map<QueuedBuildInfo, BuildAgent> agentMap = Collections.emptyMap();
    private BuildDistributorInput buildDistributorInput = mock(BuildDistributorInput.class);

    @Before
    public void setup() {
        ResourceMonitor monitor = mock(ResourceMonitor.class);
        resourceManager = new ResourceManager(null);
        precondition = new ResourceStartBuildPrecondition(resourceManager, monitor);
    }

    @Test
    public void shouldReturnNullWaitReasonWhenBuildIsNotMonitored() {
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt123");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnNullWaitReasonWhenBuildIsMonitoredAndResourceIsAvailable() {
        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt124");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnWaitReasonWhenResourceIsUnavailable() {
        Resource resource = spy(new Resource("1", "test", "localhost", 1234));
        resource.addBuildType("bt123");
        resourceManager.addResource(resource);
        precondition.resourceUnavailable(resource);

        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt123");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNotNull(waitReason);
        assertThat(waitReason.getDescription(), containsString(resource.getName()));
        assertThat(waitReason.getDescription(), containsString("available"));
    }

    @Test
    public void shouldReturnNullWaitReasonWhenUnavailableResourceBecomesAvailable() {
        Resource resource = spy(new Resource("1", "test", "localhost", 1234));
        resource.addBuildType("bt123");
        resourceManager.addResource(resource);
        precondition.resourceUnavailable(resource);

        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt123");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNotNull(waitReason);

        precondition.resourceAvailable(resource);
        waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }

    @Test
    public void shouldReturnWaitReasonWhenResourceIsDisabled() {
        Resource resource = spy(new Resource("1", "test", "localhost", 1234));
        resource.addBuildType("bt123");
        resourceManager.addResource(resource);
        precondition.resourceDisabled(resource);

        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt123");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNotNull(waitReason);
        assertThat(waitReason.getDescription(), containsString(resource.getName()));
        assertThat(waitReason.getDescription(), containsString("enabled"));
    }

    @Test
    public void shouldReturnNullWaitReasonWhenDisabledResourceIsEnabled() {
        Resource resource = spy(new Resource("1", "test", "localhost", 1234));
        resource.addBuildType("bt123");
        resourceManager.addResource(resource);
        precondition.resourceDisabled(resource);

        when(queuedBuildInfo.getBuildConfiguration()).thenReturn(buildConfigurationInfo);
        when(buildConfigurationInfo.getId()).thenReturn("bt123");

        WaitReason waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNotNull(waitReason);

        precondition.resourceEnabled(resource);
        waitReason = precondition.canStart(queuedBuildInfo, agentMap, buildDistributorInput, false);
        assertNull(waitReason);
    }
}
