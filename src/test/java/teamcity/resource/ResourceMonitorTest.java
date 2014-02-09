package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildServer;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
        manager = new ResourceManager(new FakeProjectManager());
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

    @Test
    public void monitorShouldNotifyListenersWhenResourceIsDisabled() {
        ResourceMonitor monitor = new ResourceMonitor(server, manager, null);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);

        monitor.disableResource(resource);
        verify(listener).resourceDisabled(resource);
    }

    @Test
    public void resourceMonitorListenersShouldReceiveOneDisabledNotification() {
        ResourceMonitor monitor = new ResourceMonitor(server, manager, null);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);

        monitor.disableResource(resource);
        monitor.disableResource(resource);
        verify(listener).resourceDisabled(resource);
    }


    @Test
    public void monitorShouldNotifyListenersWhenResourceIsEnabled() {
        ResourceMonitor monitor = new ResourceMonitor(server, manager, null);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);
        monitor.disableResource(resource);

        monitor.enableResource(resource);
        verify(listener).resourceEnabled(resource);
    }

    @Test
    public void resourceMonitorListenersShouldReceiveOneEnabledNotification() {
        ResourceMonitor monitor = new ResourceMonitor(server, manager, null);
        ResourceMonitorListener listener = mock(ResourceMonitorListener.class);
        monitor.addListener(listener);
        monitor.disableResource(resource);

        monitor.enableResource(resource);
        monitor.enableResource(resource);
        verify(listener).resourceEnabled(resource);
    }

    @Test
    public void scheduleMonitor() {
        int checkInterval = 123;
        manager.setInterval(checkInterval);

        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        when(server.getExecutor()).thenReturn(executorService);

        ResourceMonitor monitor = new ResourceMonitor(server, manager, null);
        monitor.scheduleMonitor();

        long initialDelay = 1;
        verify(executorService).scheduleAtFixedRate(same(monitor), eq(initialDelay), eq((long) checkInterval), eq(TimeUnit.SECONDS));
    }

    @Test
    public void rescheduleMonitor() {
        int checkInterval = 123;
        manager.setInterval(checkInterval);

        ScheduledExecutorService executorService = mock(ScheduledExecutorService.class);
        when(server.getExecutor()).thenReturn(executorService);

        ScheduledFuture future = mock(ScheduledFuture.class);
        when(executorService.scheduleAtFixedRate(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(future);

        // initial schedule check of 123 seconds
        ResourceMonitor monitor = new ResourceMonitor(server, manager, null);
        monitor.scheduleMonitor();

        // re-schedule check to 456 seconds
        int newCheckInterval = 456;
        manager.setInterval(newCheckInterval);
        monitor.scheduleMonitor();

        long initialDelay = 1;
        verify(executorService).scheduleAtFixedRate(same(monitor), eq(initialDelay), eq((long) checkInterval), eq(TimeUnit.SECONDS));
        verify(executorService).scheduleAtFixedRate(same(monitor), eq(initialDelay), eq((long) newCheckInterval), eq(TimeUnit.SECONDS));
        verify(future).cancel(eq(false));
    }
}
