package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ResourceStatusControllerTest {

    private WebControllerManager controllerManager;
    private ResourceMonitor monitor;
    private ResourceStatusController controller;
    private ResourceBuildLimitStartPrecondition precondition;

    private Resource resource1;
    private Resource resource2;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringBuilder responseMessage;

    @Before
    public void setup() throws IOException {
        controllerManager = mock(WebControllerManager.class);
        SBuildServer buildServer = mock(SBuildServer.class);
        monitor = new ResourceMonitor(buildServer, null, null);
        ResourceManager manager = mock(ResourceManager.class);
        precondition = new ResourceBuildLimitStartPrecondition(buildServer, manager);
        controller = new ResourceStatusController(controllerManager, monitor, precondition);

        resource1 = new Resource("123", "Test resource", "localhost", 1234);
        resource2 = new Resource("124", "Test resource", "localhost", 1234);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        responseMessage = new StringBuilder();
        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int i) throws IOException {
                responseMessage.append((char) i);
            }
        };
        when(response.getOutputStream()).thenReturn(servletOutputStream);
    }

    @Test
    public void shouldRegisterWithResourceMonitor() {
        ResourceMonitor resourceMonitor = spy(monitor);
        ResourceStatusController controller = new ResourceStatusController(controllerManager, resourceMonitor, precondition);
        verify(resourceMonitor).addListener(same(controller));
    }

    @Test
    public void shouldRegisterWithWebControllerManager() {
        verify(controllerManager).registerController(eq("/resourceStatus.html"), eq(controller));
    }

    @Test
    public void shouldRegisterWithBuildStartPrecondition() {
        SBuildServer buildServer = mock(SBuildServer.class);
        ResourceManager manager = mock(ResourceManager.class);
        ResourceBuildLimitStartPrecondition precondition = spy(new ResourceBuildLimitStartPrecondition(buildServer, manager));

        ResourceStatusController controller = new ResourceStatusController(controllerManager, monitor, precondition);
        verify(precondition).addListener(same(controller));
    }

    @Test
    public void shouldReturnEmptyResponseWithNoMonitoredResources() throws Exception {
        controller.doHandle(request, response);

        assertEquals("<response />", responseMessage.toString());
    }

    @Test
    public void shouldReturnResponseWithMonitoredResource() throws Exception {
        controller.resourceUnavailable(resource1);

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("1", "count(//resources)", responseMessage.toString());
        assertXpathEvaluatesTo("1", "count(//resource)", responseMessage.toString());
    }

    @Test
    public void shouldReturnResponseWithUnavailableResource() throws Exception {
        controller.resourceUnavailable(resource1);

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("123", "//resource/@id", responseMessage.toString());
        assertXpathEvaluatesTo("false", "//resource/@available", responseMessage.toString());
    }

    @Test
    public void shouldReturnResponseWithAvailableResource() throws Exception {
        controller.resourceAvailable(resource1);

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("123", "//resource/@id", responseMessage.toString());
        assertXpathEvaluatesTo("true", "//resource/@available", responseMessage.toString());
    }

    @Test
    public void shouldReturnResponseWithMultipleResources() throws Exception {
        controller.resourceAvailable(resource1);
        controller.resourceUnavailable(resource2);

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("1", "count(//resources)", responseMessage.toString());
        assertXpathEvaluatesTo("2", "count(//resource)", responseMessage.toString());
        assertXpathEvaluatesTo("123", "//resource[1]/@id", responseMessage.toString());
        assertXpathEvaluatesTo("true", "//resource[1]/@available", responseMessage.toString());
        assertXpathEvaluatesTo("124", "//resource[2]/@id", responseMessage.toString());
        assertXpathEvaluatesTo("false", "//resource[2]/@available", responseMessage.toString());
    }

    @Test
    public void shouldReturnResponseWithOneResourceAfterAvailabilityChange() throws Exception {
        controller.resourceAvailable(resource1);
        controller.resourceUnavailable(resource1);

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("1", "count(//resources)", responseMessage.toString());
        assertXpathEvaluatesTo("1", "count(//resource)", responseMessage.toString());
        assertXpathEvaluatesTo("123", "//resource/@id", responseMessage.toString());
        assertXpathEvaluatesTo("false", "//resource/@available", responseMessage.toString());
    }

    @Test
    public void shouldAlwaysReturnResourceStatus() throws Exception {
        controller.resourceAvailable(resource1);

        controller.doHandle(request, response);
        assertXpathEvaluatesTo("1", "count(//resource)", responseMessage.toString());

        responseMessage.setLength(0);
        controller.doHandle(request, response);
        assertXpathEvaluatesTo("1", "count(//resource)", responseMessage.toString());
    }

    @Test
    public void shouldReturnResourceUsage() throws Exception {
        controller.resourceUsageChanged(resource1, 3);
        controller.doHandle(request, response);
        assertXpathEvaluatesTo("1", "count(//resource)", responseMessage.toString());
        assertXpathEvaluatesTo("123", "//resource/@id", responseMessage.toString());
        assertXpathEvaluatesTo("3", "//resource/@count", responseMessage.toString());
    }

    @Test
    public void shouldReturnOneMessageForMultipleStatusChanges() throws Exception {
        controller.resourceAvailable(resource1);
        controller.resourceUsageChanged(resource1, 3);

        controller.doHandle(request, response);

        assertXpathEvaluatesTo("1", "count(//resource)", responseMessage.toString());
        assertXpathEvaluatesTo("123", "//resource/@id", responseMessage.toString());
        assertXpathEvaluatesTo("3", "//resource/@count", responseMessage.toString());
    }
}
