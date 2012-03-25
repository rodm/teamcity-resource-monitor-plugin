package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ResourceMonitorTest {

    private static final boolean UNAVAILABLE = false;
    private static final boolean AVAILABLE = true;

    private SBuildServer server;

    private ResourceManager manager;

    private Resource resource;

    @Before
    public void setup() {
        server = mock(SBuildServer.class);
        ProjectManager projectManager = mock(ProjectManager.class);
        when(server.getProjectManager()).thenReturn(projectManager);
        manager = new ResourceManager(projectManager);
        resource = new Resource("1", "test", "localhost", 1234);
        manager.addResource(resource);
    }

    @Test
    public void monitorShouldCheckResourceAvailability() {
        AvailabilityChecker checker = mock(AvailabilityChecker.class);
        ResourceMonitor monitor = new ResourceMonitor(server, manager, checker);
        monitor.run();

        verify(checker).isAvailable(resource);
    }

    @Test
    public void monitorShouldNotifyListenersOfResourceUnavailability() {
        AvailabilityChecker checker = mock(AvailabilityChecker.class);
        when(checker.isAvailable(resource)).thenReturn(UNAVAILABLE);

        ResourceMonitor monitor = new ResourceMonitor(server, manager, checker);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);
        monitor.run();

        verify(listener).resourceUnavailable(resource);
    }

    @Test
    public void resourceMonitorListenerShouldReceiveOneUnavailableNotification() {
        AvailabilityChecker checker = mock(AvailabilityChecker.class);
        when(checker.isAvailable(resource)).thenReturn(UNAVAILABLE);

        ResourceMonitor monitor = new ResourceMonitor(server, manager, checker);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);
        monitor.run();
        monitor.run();

        verify(listener).resourceUnavailable(resource);
    }

    @Test
    public void monitorShouldNotifyListenersOfResourceAvailability() {
        AvailabilityChecker checker = mock(AvailabilityChecker.class);
        when(checker.isAvailable(resource)).thenReturn(UNAVAILABLE, AVAILABLE);

        ResourceMonitor monitor = new ResourceMonitor(server, manager, checker);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);
        monitor.run(); // set unavailable

        monitor.run();
        verify(listener).resourceAvailable(resource);
    }

    @Test
    public void resourceMonitorListenerShouldReceiveOneAvailableNotification() {
        AvailabilityChecker checker = mock(AvailabilityChecker.class);
        when(checker.isAvailable(resource)).thenReturn(UNAVAILABLE, AVAILABLE, AVAILABLE);

        ResourceMonitor monitor = new ResourceMonitor(server, manager, checker);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);
        monitor.run(); // set unavailable

        monitor.run();
        monitor.run();
        verify(listener).resourceAvailable(resource);
    }
}
