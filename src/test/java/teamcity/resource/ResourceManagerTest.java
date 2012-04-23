package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

public class ResourceManagerTest {

    private static final String ID = "1";
    private static final String NAME = "Test Resource";
    private static final String HOST = "test";
    private static final int PORT = 1234;
    private static final String BUILD_TYPE_ID = "bt123";
    private static final String INVALID_BUILD_TYPE_ID = "bt124";

    private ResourceManager manager;

    private ProjectManager mockProjectManager;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        mockProjectManager = mock(ProjectManager.class);
        manager = new ResourceManager(mockProjectManager);
    }

    @Test
    public void newResourceManagerHasNoResources() {
        Collection<Resource> resources = manager.getResources();
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

    @Test
    public void cannotAddResourceWithSameId() {
        manager.addResource(new Resource(ID, NAME + "1", HOST, PORT));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("resource with id " + ID + " already exists");
        manager.addResource(new Resource(ID, NAME + "2", HOST, PORT));
    }

    @Test
    public void canReAddResourceWithSameId() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        manager.removeResource(ID);
        manager.addResource(resource);
    }

    @Test
    public void cannotAddResourceWithSameName() {
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("resource with name " + NAME + " already exists");
        manager.addResource(new Resource("2", NAME, HOST, PORT));
    }

    @Test
    public void updateResource() {
        String newName = "new name";
        String newHost = "newhost";
        int newPort = 4321;
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        manager.updateResource(ID, newName, newHost, newPort);

        assertEquals(newName, resource.getName());
        assertEquals(newHost, resource.getHost());
        assertEquals(newPort, resource.getPort());
    }

    @Test
    public void updateResourceThatDoesNotExist() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("resource with id " + ID + " does not exist");
        manager.updateResource(ID, NAME, HOST, PORT);
    }

    @Test
    public void removeResource() {
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        manager.removeResource(ID);
        assertEquals("there should be no resources", 0, manager.getResources().size());
    }

    @Test
    public void removingResourceThatDoesNotExist() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("resource with id " + ID + " does not exist");
        manager.removeResource(ID);
    }

    @Test
    public void getResourceById() {
        Resource expectedResource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(expectedResource);

        Resource resource = manager.getResourceById(ID);
        assertSame(expectedResource, resource);
    }

    @Test
    public void shouldReturnNullWhenIdNotFound() {
        Resource resource = manager.getResourceById(ID);
        assertNull(resource);
    }

    @Test
    public void shouldReturnResourceForLinkedBuildType() {
        Resource expectedResource = new Resource(ID, NAME, HOST, PORT);
        expectedResource.addBuildType(BUILD_TYPE_ID);
        manager.addResource(expectedResource);

        Resource resource = manager.findResourceByBuildTypeId(BUILD_TYPE_ID);
        assertNotNull(resource);
        assertSame(expectedResource, resource);
    }

    @Test
    public void shouldReturnNullForUnlinkedBuildType() {
        Resource expectedResource = new Resource(ID, NAME, HOST, PORT);
        expectedResource.addBuildType(BUILD_TYPE_ID);
        manager.addResource(expectedResource);

        Resource resource = manager.findResourceByBuildTypeId("bt124");
        assertNull(resource);
    }

    @Test
    public void linkBuildToResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq(BUILD_TYPE_ID))).thenReturn(buildType);
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        manager.linkBuildToResource(ID, BUILD_TYPE_ID);

        Resource resource = manager.getResourceById(ID);
        assertEquals(1, resource.getBuildTypes().size());
        assertEquals(BUILD_TYPE_ID, resource.getBuildTypes().get(0));
    }

    @Test
    public void linkBuildToInvalidResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq(BUILD_TYPE_ID))).thenReturn(buildType);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("resource with id " + ID + " does not exist");
        manager.linkBuildToResource(ID, BUILD_TYPE_ID);
    }

    @Test
    public void linkInvalidBuildToResource() {
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        thrown.expect(IllegalArgumentException.class);
        manager.linkBuildToResource(ID, INVALID_BUILD_TYPE_ID);
    }

    @Test
    public void unlinkBuildFromResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq(BUILD_TYPE_ID))).thenReturn(buildType);
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        resource.addBuildType(BUILD_TYPE_ID);
        manager.addResource(resource);

        manager.unlinkBuildFromResource(ID, BUILD_TYPE_ID);

        Resource resource2 = manager.getResourceById(ID);
        assertEquals(0, resource2.getBuildTypes().size());
    }

    @Test
    public void unlinkBuildFromInvalidResource() {
        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(eq(BUILD_TYPE_ID))).thenReturn(buildType);

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("resource with id " + ID + " does not exist");
        manager.unlinkBuildFromResource(ID, BUILD_TYPE_ID);
    }

    @Test
    public void unlinkInvalidBuildFromResource() {
        manager.addResource(new Resource(ID, NAME, HOST, PORT));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("build type id " + INVALID_BUILD_TYPE_ID + " does not exist");
        manager.unlinkBuildFromResource(ID, INVALID_BUILD_TYPE_ID);
    }

    @Test
    public void emptyManagerAllocatesFirstId() {
        assertEquals(1, manager.nextId());
    }

    @Test
    public void shouldAllocateNextId() {
        manager.addResource(new Resource(ID, NAME, HOST, PORT));
        assertEquals(2, manager.nextId());
    }

    @Test
    public void shouldAllocateNextHighestId() {
        manager.addResource(new Resource("123", NAME, HOST, PORT));
        assertEquals(124, manager.nextId());
    }

    @Test
    public void replacedResourcesShouldBeAccessibleById() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        Collection<Resource> newResources = new ArrayList<Resource>();
        newResources.add(resource);

        manager.setResources(newResources);
        manager.updateResource(ID, "newname", "newhost", 4321);
    }

    @Test
    public void settingResourcesShouldRemoveInvalidBuildTypes() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        resource.addBuildType(BUILD_TYPE_ID);
        resource.addBuildType(INVALID_BUILD_TYPE_ID);
        Collection<Resource> newResources = new ArrayList<Resource>();
        newResources.add(resource);

        SBuildType buildType = mock(SBuildType.class);
        when(mockProjectManager.findBuildTypeById(BUILD_TYPE_ID)).thenReturn(buildType);

        manager.setResources(newResources);
        assertEquals(1, resource.getBuildTypes().size());
        assertEquals(BUILD_TYPE_ID, resource.getBuildTypes().get(0));
    }

    @Test
    public void unlinkUnregisteredBuildType() {
        SBuildType buildType = mock(SBuildType.class);
        when(buildType.getBuildTypeId()).thenReturn(BUILD_TYPE_ID);
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        resource.addBuildType(BUILD_TYPE_ID);
        manager.addResource(resource);

        manager.unregisterBuild(BUILD_TYPE_ID);

        Resource resource2 = manager.getResourceById(ID);
        assertEquals(0, resource2.getBuildTypes().size());
    }

    @Test
    public void notifyListenersWhenResourceIsAdded() {
        ResourceManagerListener listener = mock(ResourceManagerListener.class);
        manager.addListener(listener);
        Resource resource = new Resource(ID, NAME, HOST, PORT);

        manager.addResource(resource);
        verify(listener).resourceAdded(resource);
    }

    @Test
    public void notifyListenersWhenResourceIsUpdated() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        ResourceManagerListener listener = mock(ResourceManagerListener.class);
        manager.addListener(listener);

        manager.updateResource(ID, "newname", "newhost", 4321);
        verify(listener).resourceUpdated(resource);
    }

    @Test
    public void notifyListenersWhenResourceIsRemoved() {
        Resource resource = new Resource(ID, NAME, HOST, PORT);
        manager.addResource(resource);
        ResourceManagerListener listener = mock(ResourceManagerListener.class);
        manager.addListener(listener);

        manager.removeResource(ID);
        verify(listener).resourceRemoved(resource);
    }
}
