package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class ResourceManagerTest {

    private ResourceManager manager;

    @Before
    public void setup() {
        manager = new ResourceManager();
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
    public void linkBuildToResource() {
        manager.setProjectMananger(createProjectManager());
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
        manager.setProjectMananger(createProjectManager());
        manager.addResource(new Resource("Test Resource", null, -1));

        manager.linkBuildToResource("Test Resource", "bt124");
    }

    @Test
    public void unlinkBuildFromResource() {
        manager.setProjectMananger(createProjectManager());
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
        manager.setProjectMananger(createProjectManager());
        manager.addResource(new Resource("Test Resource", null, -1));

        manager.unlinkBuildFromResource("Test Resource", "bt124");
    }

    private ProjectManager createProjectManager() {
        ClassLoader loader = ProjectManager.class.getClassLoader();
        Class[] proxyInterfaces = new Class[] { ProjectManager.class };
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object obj, Method method, Object[] objects) throws Throwable {
                if ("findBuildTypeById".equals(method.getName())) {
                    if ("bt123".equals(objects[0])) {
                        return createBuildType();
                    }
                }
                return null;
            }

            private SBuildType createBuildType() {
                ClassLoader loader = SBuildType.class.getClassLoader();
                Class[] proxyInterfaces = new Class[] { SBuildType.class };
                InvocationHandler handler = new InvocationHandler() {
                    public Object invoke(Object obj, Method method, Object[] objects) throws Throwable {
                        return null;
                    }
                };
                return (SBuildType) Proxy.newProxyInstance(loader, proxyInterfaces, handler);
            }
        };
        return (ProjectManager) Proxy.newProxyInstance(loader, proxyInterfaces, handler);
    }
}
