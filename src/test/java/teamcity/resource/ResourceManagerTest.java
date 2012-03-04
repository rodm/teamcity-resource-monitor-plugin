package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class ResourceManagerTest {

    private static final String ID = "1";
    private static final String NAME = "Test Resource";
    private static final String HOST = "test";
    private static final int PORT = 1234;
    private static final String BUILD_TYPE_ID = "bt123";

    private ResourceManager manager;

    private ProjectManager mockProjectManager;

    @Before
    public void setup() {
        mockProjectManager = mock(ProjectManager.class);
        manager = new ResourceManager(mockProjectManager);
    }

    @Test
    public void newResourceManagerHasNoResources() {
        Map<String, Resource> resources = manager.getResources();
        assertEquals(0, resources.size());
    }

    @Test
    public void addResource() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        assertEquals(1, manager.getResources().size());
    }

    @Test
    public void addingResources() {
        manager.addResource(new Resource(ID, NAME + "1", HOST, PORT));
        manager.addResource(new Resource("2", NAME + "2", HOST, PORT));
        assertEquals(2, manager.getResources().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddResourceWithSameId() {
        manager.addResource(new Resource(ID, NAME + "1", HOST, PORT));
        manager.addResource(new Resource(ID, NAME + "2", HOST, PORT));
    }

    @Test
    public void canReAddResourceWithSameId() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        manager.removeResource(NAME);
        manager.addResource(resource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddResourceWithSameName() {
        manager.addResource(new Resource(ID, NAME, HOST, PORT));
        manager.addResource(new Resource(ID, NAME, HOST, PORT));
    }

    @Test
    public void updateResource() {
        String newHost = "newhost";
        int newPort = 4321;
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        manager.updateResource(NAME, newHost, newPort);

        assertEquals(newHost, resource.getHost());
        assertEquals(newPort, resource.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateResourceThatDoesNotExist() {
        manager.updateResource(NAME, HOST, PORT);
    }

    @Test
    public void removeResource() {
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        manager.removeResource(NAME);
        assertEquals("there should be no resources", 0, manager.getResources().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removingResourceThatDoesntExist() {
        manager.removeResource(NAME);
    }

    @Test
    public void enableResource() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        resource.disable();
        manager.addResource(resource);
        manager.enableResource(NAME);

        assertTrue(resource.isEnabled());
    }

    @Test
    public void disableResource() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        manager.disableResource(NAME);

        assertFalse(resource.isEnabled());
    }

    @Test
    public void linkBuildToResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq(BUILD_TYPE_ID))).thenReturn(buildType);
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        manager.linkBuildToResource(NAME, BUILD_TYPE_ID);

        Map<String, Resource> resources = manager.getResources();
        assertEquals(1, resources.get(NAME).getBuildTypes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void linkBuildToInvalidResource() {
        manager.linkBuildToResource(NAME, BUILD_TYPE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void linkInvalidBuildToResource() {
        String invalidBuildTypeId = "bt124";
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        manager.linkBuildToResource(NAME, invalidBuildTypeId);
    }

    @Test
    public void unlinkBuildFromResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq(BUILD_TYPE_ID))).thenReturn(buildType);
        List<String> buildTypes = new ArrayList<String>();
        buildTypes.add(BUILD_TYPE_ID);
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        resource.setBuildTypes(buildTypes);
        manager.addResource(resource);

        manager.unlinkBuildFromResource(NAME, BUILD_TYPE_ID);

        Map<String, Resource> resources = manager.getResources();
        assertEquals(0, resources.get(NAME).getBuildTypes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlinkBuildFromInvalidResource() {
        manager.unlinkBuildFromResource(NAME, BUILD_TYPE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlinkInvalidBuildFromResource() {
        String invalidBuildTypeId = "bt124";
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        manager.unlinkBuildFromResource(NAME, invalidBuildTypeId);
    }
}
