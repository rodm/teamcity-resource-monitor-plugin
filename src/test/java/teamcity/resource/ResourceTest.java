package teamcity.resource;

import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class ResourceTest {

    @Test(expected = IllegalArgumentException.class)
    public void nameCannotBeNull() {
        new Resource(null, null, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nameCannotBeEmpty() {
        new Resource("", null, -1);
    }

    @Test
    public void unavailableResource() {
        Resource rm = new Resource("test", "localhost", 7401);
        assertFalse(rm.isAvailable());
    }
}
