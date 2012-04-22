package teamcity.resource;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.*;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;

public class ResourceControllerTest {

    private static final String SUBMIT_ACTION = "submitAction";
    private static final String RESOURCE_ID = "resourceId";
    private static final String RESOURCE_NAME = "resourceName";
    private static final String RESOURCE_HOST = "resourceHost";
    private static final String RESOURCE_PORT = "resourcePort";

    private ResourceManager manager;
    private ResourceMonitor monitor;
    private ResourceMonitorPlugin plugin;
    private Resource resource;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringBuilder responseMessage;

    @Before
    public void setup() throws IOException {
        manager = new ResourceManager(null);
        monitor = mock(ResourceMonitor.class);
        plugin = mock(ResourceMonitorPlugin.class);

        resource = new Resource("123", "test", "localhost", 1234);
        manager.addResource(resource);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseMessage = new StringBuilder();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public void write(int i) throws IOException {
                responseMessage.append((char)i);
            }
        };
        when(response.getOutputStream()).thenReturn(sos);
    }

    @Test
    public void invalidAction() throws Exception {
        manager = mock(ResourceManager.class);
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("invalidAction");

        ResourceController controller = new ResourceController(null, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        assertThat(responseMessage.toString(), containsString("errors"));
        assertThat(responseMessage.toString(), containsString("Invalid action"));
        verifyZeroInteractions(manager, plugin);
    }

    @Test
    public void addResource() throws Exception {
        manager = new ResourceManager(null);
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("addResource");
        when(request.getParameter(RESOURCE_NAME)).thenReturn("test");
        when(request.getParameter(RESOURCE_HOST)).thenReturn("localhost");
        when(request.getParameter(RESOURCE_PORT)).thenReturn("1234");

        ResourceController controller = new ResourceController(null, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        Resource resource = manager.getResourceById("1");
        assertNotNull(resource);
        assertEquals("test", resource.getName());
        assertEquals("localhost", resource.getHost());
        assertEquals(1234, resource.getPort());
        assertEquals("<response />", responseMessage.toString());
        verify(plugin).saveConfiguration();
    }

    @Test
    public void updateResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("updateResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");
        when(request.getParameter(RESOURCE_NAME)).thenReturn("newname");
        when(request.getParameter(RESOURCE_HOST)).thenReturn("newhost");
        when(request.getParameter(RESOURCE_PORT)).thenReturn("4321");

        ResourceController controller = new ResourceController(null, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        Resource resource = manager.getResourceById("123");
        assertNotNull(resource);
        assertEquals("newname", resource.getName());
        assertEquals("newhost", resource.getHost());
        assertEquals(4321, resource.getPort());
        assertEquals("<response />", responseMessage.toString());
        verify(plugin).saveConfiguration();
    }

    @Test
    public void removeResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("removeResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        ResourceController controller = new ResourceController(null, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        Resource resource = manager.getResourceById("123");
        assertNull(resource);
        assertEquals("<response />", responseMessage.toString());
        verify(plugin).saveConfiguration();
    }

    @Test
    public void linkBuildType() throws Exception {
        manager = mock(ResourceManager.class);
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("linkBuildType");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");
        when(request.getParameter("buildTypeId")).thenReturn("bt123");

        ResourceController controller = new ResourceController(null, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        verify(manager).linkBuildToResource("123", "bt123");
        verify(plugin).saveConfiguration();
    }

    @Test
    public void unlinkBuildType() throws Exception {
        manager = mock(ResourceManager.class);
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("unlinkBuildType");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");
        when(request.getParameter("buildTypeId")).thenReturn("bt123");

        ResourceController controller = new ResourceController(null, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        verify(manager).unlinkBuildFromResource("123", "bt123");
        verify(plugin).saveConfiguration();
    }

    @Test
    public void enableResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("enableResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        ResourceController controller = new ResourceController(null, null, manager, null, monitor);
        controller.doHandle(request, response);

        verify(monitor).enableResource(same(resource));
    }

    @Test
    public void disableResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("disableResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        ResourceController controller = new ResourceController(null, null, manager, null, monitor);
        controller.doHandle(request, response);

        verify(monitor).disableResource(same(resource));
    }
}
