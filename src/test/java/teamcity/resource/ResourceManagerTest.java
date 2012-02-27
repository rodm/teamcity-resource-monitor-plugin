package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class ResourceManagerTest {

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
        Resource resource = new Resource("Test Resource", null, -1);
        manager.addResource(resource);
        assertEquals(1, manager.getResources().size());
    }

    @Test
    public void addingResources() {
        manager.addResource(new Resource("Test Resource 1", null, -1));
        manager.addResource(new Resource("Test Resource 2", null, -1));
        assertEquals(2, manager.getResources().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotAddResourceWithSameName() {
        manager.addResource(new Resource("Test Resource", null, -1));
        manager.addResource(new Resource("Test Resource", null, -1));
    }

    @Test
    public void removeResource() {
        manager.addResource(new Resource("Test Resource", null, -1));

        manager.removeResource("Test Resource");
        assertEquals("there should be no resources", 0, manager.getResources().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removingResourceThatDoesntExist() {
        manager.removeResource("Resource");
    }

    @Test
    public void enableResource() {
        Resource resource = new Resource("Test Resource", null, -1);
        resource.disable();
        manager.addResource(resource);
        manager.enableResource("Test Resource");

        assertTrue(resource.isEnabled());
    }

    @Test
    public void disableResource() {
        Resource resource = new Resource("Test Resource", null, -1);
        manager.addResource(resource);
        manager.disableResource("Test Resource");

        assertFalse(resource.isEnabled());
    }

    @Test
    public void linkBuildToResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq("bt123"))).thenReturn(buildType);
        manager.addResource(new Resource("Test Resource", null, -1));

        manager.linkBuildToResource("Test Resource", "bt123");

        Map<String, Resource> resources = manager.getResources();
        assertEquals(1, resources.get("Test Resource").getBuildTypes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void linkBuildToInvalidResource() {
        manager.linkBuildToResource("Test Resource", "bt123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void linkInvalidBuildToResource() {
        manager.addResource(new Resource("Test Resource", null, -1));

        manager.linkBuildToResource("Test Resource", "bt124");
    }

    @Test
    public void unlinkBuildFromResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq("bt123"))).thenReturn(buildType);
        List<String> buildTypes = new ArrayList<String>();
        buildTypes.add("bt123");
        Resource resource = new Resource("Test Resource", null, -1);
        resource.setBuildTypes(buildTypes);
        manager.addResource(resource);

        manager.unlinkBuildFromResource("Test Resource", "bt123");

        Map<String, Resource> resources = manager.getResources();
        assertEquals(0, resources.get("Test Resource").getBuildTypes().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlinkBuildFromInvalidResource() {
        manager.unlinkBuildFromResource("Test Resource", "bt123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unlinkInvalidBuildFromResource() {
        manager.addResource(new Resource("Test Resource", null, -1));

        manager.unlinkBuildFromResource("Test Resource", "bt124");
    }
}
