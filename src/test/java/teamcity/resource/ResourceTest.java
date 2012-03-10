package teamcity.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ResourceTest {

    private String VALID_ID = "123";
    private String VALID_NAME = "name";
    private String VALID_HOST = "localhost";
    private int VALID_PORT = 1234;

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
        thrown.expectMessage("id cannot be null or empty");
        new Resource(null, VALID_NAME, VALID_HOST, VALID_PORT);
    }

    @Test
    public void idCannotBeEmpty() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("id cannot be null or empty");
        new Resource("", VALID_NAME, VALID_HOST, VALID_PORT);
    }

    @Test
    public void nameCannotBeNull() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("name cannot be null or empty");
        new Resource(VALID_ID, null, VALID_HOST, VALID_PORT);
    }

    @Test
    public void nameCannotBeEmpty() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("name cannot be null or empty");
        new Resource(VALID_ID, "", VALID_HOST, VALID_PORT);
    }

    @Test
    public void unavailableResource() {
        Resource rm = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);
        assertFalse(rm.isAvailable());
    }
}
