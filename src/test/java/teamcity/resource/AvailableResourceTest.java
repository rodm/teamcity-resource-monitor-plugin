package teamcity.resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AvailableResourceTest {

    private ServerSocket resourceSocket;
    private Resource resource;

    @Before
    public void setup() throws IOException {
        resourceSocket = new ServerSocket(7400);
        resource = new Resource("test", "localhost", 7400);
    }

    @After
    public void cleanup() throws IOException {
        resourceSocket.close();
    }

    @Test
    public void enabledResourceIsAvailable() throws IOException {
        assertTrue(resource.isAvailable());
    }

    @Test
    public void disabledResourceIsUnavailable() throws IOException {
        resource.disable();
        assertFalse(resource.isAvailable());
    }

    @Test
    public void reenabledResourceIsAvailable() throws IOException {
        resource.disable();
        assertFalse(resource.isAvailable());
        resource.enable();
        assertTrue(resource.isAvailable());
    }
}
