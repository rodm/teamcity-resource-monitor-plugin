package teamcity.resource;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.serverSide.SBuildServer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
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
    private static final String RESOURCE_LIMIT = "resourceLimit";
    private static final String BUILD_TYPE_ID = "buildTypeId";

    private SBuildServer buildServer;
    private ResourceManager manager;
    private ResourceManager managerSpy;
    private ResourceMonitor monitor;
    private ResourceMonitorPlugin plugin;
    private ResourceController controller;

    private Resource resource;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringBuilder responseMessage;

    @Before
    public void setup() throws IOException {
        buildServer = mock(SBuildServer.class);
        manager = new ResourceManager(null);
        managerSpy = spy(manager);
        monitor = mock(ResourceMonitor.class);
        plugin = mock(ResourceMonitorPlugin.class);
        controller = new ResourceController(buildServer, null, managerSpy, plugin, monitor);

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

        controller.doHandle(request, response);

        assertThat(responseMessage.toString(), containsString("errors"));
        assertThat(responseMessage.toString(), containsString("Invalid action"));
        verifyZeroInteractions(manager, plugin);
    }

    @Test
    public void addResource() throws Exception {
        setupRequest("addResource", "test", "localhost", "1234", "123");
        manager = new ResourceManager(null);
        managerSpy = spy(manager);
        ResourceController controller = new ResourceController(buildServer, null, managerSpy, plugin, monitor);
        controller.doHandle(request, response);

        assertEquals("<response />", responseMessage.toString());
        verify(managerSpy).addResource(eq("test"), eq("localhost"), eq("1234"), eq("123"));
    }

    @Test
    public void shouldSaveConfigurationOnAddingResource() throws Exception {
        setupRequest("addResource", "test", "localhost", "1234", "123");
        manager = new ResourceManager(null);
        ResourceController controller = new ResourceController(buildServer, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        verify(plugin).saveConfiguration();
    }

    @Test
    public void updateResource() throws Exception {
        setupRequest("updateResource", "newname", "newhost", "4321", "321");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        controller.doHandle(request, response);

        assertEquals("<response />", responseMessage.toString());
        verify(managerSpy).updateResource(eq("123"), eq("newname"), eq("newhost"), eq("4321"), eq("321"));
    }

    @Test
    public void shouldSaveConfigurationOnUpdatingResource() throws Exception {
        setupRequest("updateResource", "newname", "newhost", "4321", "321");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        controller.doHandle(request, response);
        verify(plugin).saveConfiguration();
    }

    @Test
    public void removeResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("removeResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        controller.doHandle(request, response);

        assertEquals("<response />", responseMessage.toString());
        verify(managerSpy).removeResource(eq("123"));
    }

    @Test
    public void shouldSaveConfigurationOnRemovingResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("removeResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        controller.doHandle(request, response);
        verify(plugin).saveConfiguration();
    }

    @Test
    public void linkBuildType() throws Exception {
        setupRequest("linkBuildType", "123", "bt123");
        manager = mock(ResourceManager.class);
        ResourceController controller = new ResourceController(buildServer, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        verify(manager).linkBuildToResource("123", "bt123");
    }

    @Test
    public void shouldSaveConfigurationOnLinkingBuildType() throws Exception {
        setupRequest("linkBuildType", "123", "bt123");
        manager = mock(ResourceManager.class);
        ResourceController controller = new ResourceController(buildServer, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        verify(plugin).saveConfiguration();
    }

    @Test
    public void unlinkBuildType() throws Exception {
        setupRequest("unlinkBuildType", "123", "bt123");
        manager = mock(ResourceManager.class);
        ResourceController controller = new ResourceController(buildServer, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        verify(manager).unlinkBuildFromResource("123", "bt123");
    }

    @Test
    public void shouldSaveConfigurationOnUnlinkingBuildType() throws Exception {
        setupRequest("unlinkBuildType", "123", "bt123");
        manager = mock(ResourceManager.class);
        ResourceController controller = new ResourceController(buildServer, null, manager, plugin, monitor);
        controller.doHandle(request, response);

        verify(plugin).saveConfiguration();
    }

    @Test
    public void enableResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("enableResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        controller.doHandle(request, response);

        verify(monitor).enableResource(same(resource));
    }

    @Test
    public void disableResource() throws Exception {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn("disableResource");
        when(request.getParameter(RESOURCE_ID)).thenReturn("123");

        controller.doHandle(request, response);

        verify(monitor).disableResource(same(resource));
    }

    @Test
    public void invalidNameReturnsErrorMessage() throws Exception {
        setupRequest("addResource", "", "localhost", "1234");

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("invalidName", "//response/errors/error/@id", responseMessage.toString());
        assertXpathEvaluatesTo("name cannot be null or empty", "//response/errors/error", responseMessage.toString());
    }

    @Test
    public void invalidHostReturnsErrorMessage() throws Exception {
        setupRequest("addResource", "test", "", "1234");

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("invalidHost", "//response/errors/error/@id", responseMessage.toString());
        assertXpathEvaluatesTo("host cannot be null or empty", "//response/errors/error", responseMessage.toString());
    }

    @Test
    public void invalidPortReturnsErrorMessage() throws Exception {
        setupRequest("addResource", "test", "localhost", "");

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("invalidPort", "//response/errors/error/@id", responseMessage.toString());
        assertXpathEvaluatesTo("invalid port number", "//response/errors/error", responseMessage.toString());
    }

    @Test
    public void invalidBuildLimitReturnsErrorMessage() throws Exception {
        setupRequest("addResource", "limitTest", "limithost", "1234", "-1");

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("invalidLimit", "//response/errors/error/@id", responseMessage.toString());
        assertXpathEvaluatesTo("invalid limit number", "//response/errors/error", responseMessage.toString());
    }

    private void setupRequest(String action, String name, String host, String port, String limit) {
        setupRequest(action, name, host, port);
        when(request.getParameter(RESOURCE_LIMIT)).thenReturn(limit);
    }

    private void setupRequest(String action, String name, String host, String port) {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn(action);
        when(request.getParameter(RESOURCE_NAME)).thenReturn(name);
        when(request.getParameter(RESOURCE_HOST)).thenReturn(host);
        when(request.getParameter(RESOURCE_PORT)).thenReturn(port);
    }

    private void setupRequest(String action, String resourceId, String buildTypeId) {
        when(request.getParameter(SUBMIT_ACTION)).thenReturn(action);
        when(request.getParameter(RESOURCE_ID)).thenReturn(resourceId);
        when(request.getParameter(BUILD_TYPE_ID)).thenReturn(buildTypeId);
    }
}
