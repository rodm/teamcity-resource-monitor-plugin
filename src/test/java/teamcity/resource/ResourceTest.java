package teamcity.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ResourceTest {

    private static final String INVALID_ID_MESSAGE = "id cannot be null or empty";
    private static final String INVALID_NAME_MESSAGE = "name cannot be null or empty";

    private static final String VALID_ID = "123";
    private static final String VALID_NAME = "name";
    private static final String VALID_HOST = "localhost";
    private static final int VALID_PORT = 1234;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void create() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);
        assertEquals(VALID_ID, resource.getId());
        assertEquals(VALID_NAME, resource.getName());
        assertEquals(VALID_HOST, resource.getHost());
        assertEquals(VALID_PORT, resource.getPort());
    }

    @Test
    public void idCannotBeNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(INVALID_ID_MESSAGE);
        new Resource(null, VALID_NAME, VALID_HOST, VALID_PORT);
    }

    @Test
    public void idCannotBeEmpty() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(INVALID_ID_MESSAGE);
        new Resource("", VALID_NAME, VALID_HOST, VALID_PORT);
    }

    @Test
    public void nameCannotBeNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(INVALID_NAME_MESSAGE);
        new Resource(VALID_ID, null, VALID_HOST, VALID_PORT);
    }

    @Test
    public void nameCannotBeEmpty() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(INVALID_NAME_MESSAGE);
        new Resource(VALID_ID, "", VALID_HOST, VALID_PORT);
    }

    @Test
    public void unavailableResource() {
        Resource rm = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);
        assertFalse(rm.isAvailable());
    }

    @Test
    public void addBuildToResource() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);
        resource.addBuildType("bt123");
        assertEquals(1, resource.getBuildTypes().size());
    }

    @Test
    public void removeBuildFromResource() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);
        resource.addBuildType("bt123");

        resource.removeBuildType("bt123");
        assertEquals(0, resource.getBuildTypes().size());
    }
}
