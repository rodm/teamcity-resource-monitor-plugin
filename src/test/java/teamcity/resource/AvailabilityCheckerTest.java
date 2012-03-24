package teamcity.resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AvailabilityCheckerTest {

    private AvailabilityChecker checker;

    private ServerSocket resourceSocket;

    private Resource availableResource;

    private Resource unavailableResource;

    @Before
    public void setup() throws IOException {
        checker = new AvailabilityChecker();
        resourceSocket = new ServerSocket(7400);
        availableResource = new Resource("1", "test", "localhost", 7400);
        unavailableResource = new Resource("1", "test", "localhost", 12345);
    }

    @After
    public void cleanup() throws IOException {
        resourceSocket.close();
    }

    @Test
    public void resourceIsAvailable() throws IOException {
        assertTrue(checker.isAvailable(availableResource));
    }

    @Test
    public void resourceIsUnavailable() {
        assertFalse(checker.isAvailable(unavailableResource));
    }
}
