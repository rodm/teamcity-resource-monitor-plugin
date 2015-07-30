package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class BuildTypeListenerTest {

    private SBuildType buildType;
    private FakeProjectManager projectManager;
    private ResourceManager resourceManager;
    private BuildTypeListener listener;

    @Before
    public void setup() {
        SBuildServer server = mock(SBuildServer.class);
        buildType = mock(SBuildType.class);
        when(buildType.getInternalId()).thenReturn("bt1");

        projectManager = new FakeProjectManager();
        projectManager.addBuildType("bt1", buildType);

        resourceManager = spy(new ResourceManager(projectManager));
        listener = new BuildTypeListener(server, resourceManager, projectManager);
    }

    @Test
    public void forwardBuildTypeRegisteredToResourceManager() {
        listener.buildTypeRegistered(buildType);

        verify(resourceManager).buildTypeRegistered(same(projectManager.findBuildTypeById("bt1")));
    }

    @Test
    public void forwardBuildTypeUnregisteredToResourceManager() {
        listener.buildTypeUnregistered(buildType);

        verify(resourceManager).buildTypeUnregistered(same(projectManager.findBuildTypeById("bt1")));
    }

    @Test
    public void forwardBuildTypePersistedToResourceManager() {
        listener.buildTypePersisted(buildType);

        verify(resourceManager).buildTypePersisted(same(projectManager.findBuildTypeById("bt1")));
    }
}
