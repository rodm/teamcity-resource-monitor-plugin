package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class BuildTypeListenerTest {

    private SBuildType buildType;
    private FakeProjectManager projectManager;
    private ResourceManager resourceManager;
    private BuildTypeListener listener;
    private ArgumentCaptor<BuildType> argument;

    @Before
    public void setup() {
        SBuildServer server = mock(SBuildServer.class);
        buildType = mock(SBuildType.class);
        when(buildType.getBuildTypeId()).thenReturn("bt1");

        projectManager = new FakeProjectManager();
        projectManager.addBuildType("bt1", buildType);

        argument = ArgumentCaptor.forClass(BuildType.class);

        resourceManager = spy(new ResourceManager(projectManager));
        listener = new BuildTypeListener(server, resourceManager, projectManager);
    }

    @Test
    public void forwardBuildTypeRegisteredToResourceManager() {
        listener.buildTypeRegistered(buildType);

        verify(resourceManager).buildTypeRegistered(argument.capture());
        assertEquals("bt1", argument.getValue().getBuildTypeId());
    }

    @Test
    public void forwardBuildTypeUnregisteredToResourceManager() {
        listener.buildTypeUnregistered(buildType);

        verify(resourceManager).buildTypeUnregistered(argument.capture());
        assertEquals("bt1", argument.getValue().getBuildTypeId());
    }

    @Test
    public void forwardBuildTypePersistedToResourceManager() {
        listener.buildTypePersisted(buildType);

        verify(resourceManager).buildTypePersisted(argument.capture());
        assertEquals("bt1", argument.getValue().getBuildTypeId());
    }
}
