package teamcity.resource;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertEquals;

public class ResourceMonitorTest {

    private ResourceMonitor monitor;

    @Before
    public void setup() {
        monitor = new ResourceMonitor();
    }

    @Test
    public void newResourceMonitorHasNoResources() {
        List<Resource> resources = monitor.getResources();
        assertEquals(0, resources.size());
    }

    @Test
    public void addResource() {
        Resource resource = new Resource("Test Resource", null, -1);
        monitor.addResource(resource);
        assertEquals(1, monitor.getResources().size());
    }
}
