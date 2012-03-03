package teamcity.resource;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ResourceTest {

    private String VALID_ID = "123";
    private String VALID_NAME = "name";
    private String VALID_HOST = "localhost";
    private int VALID_PORT = 1234;

    @Test
    public void create() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);
        assertEquals(VALID_ID, resource.getId());
        assertEquals(VALID_NAME, resource.getName());
        assertEquals(VALID_HOST, resource.getHost());
        assertEquals(VALID_PORT, resource.getPort());
    }

    @Test(expected = IllegalArgumentException.class)
    public void idCannotBeNull() {
        new Resource(null, VALID_NAME, VALID_HOST, VALID_PORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void idCannotBeEmpty() {
        new Resource("", VALID_NAME, VALID_HOST, VALID_PORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameCannotBeNull() {
        new Resource(VALID_ID, null, VALID_HOST, VALID_PORT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameCannotBeEmpty() {
        new Resource(VALID_ID, "", VALID_HOST, VALID_PORT);
    }

    @Test
    public void unavailableResource() {
        Resource rm = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);
        assertFalse(rm.isAvailable());
    }
}
