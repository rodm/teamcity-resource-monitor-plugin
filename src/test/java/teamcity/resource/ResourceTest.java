package teamcity.resource;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ResourceTest {

    private static final String INVALID_ID_MESSAGE = "id cannot be null or empty";
    private static final String INVALID_NAME_MESSAGE = "name cannot be null or empty";
    private static final String INVALID_HOST_MESSAGE = "host cannot be null or empty";

    private static final String VALID_ID = "123";
    private static final String VALID_NAME = "name";
    private static final String VALID_HOST = "localhost";
    private static final int VALID_PORT = 1234;
    private static final int VALID_LIMIT = 12;

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
    public void createWithBuildLimit() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT, VALID_LIMIT);
        assertEquals(VALID_LIMIT, resource.getBuildLimit());
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
        thrown.expect(InvalidNameException.class);
        thrown.expectMessage(INVALID_NAME_MESSAGE);
        new Resource(VALID_ID, null, VALID_HOST, VALID_PORT);
    }

    @Test
    public void nameCannotBeEmpty() {
        thrown.expect(InvalidNameException.class);
        thrown.expectMessage(INVALID_NAME_MESSAGE);
        new Resource(VALID_ID, "", VALID_HOST, VALID_PORT);
    }

    @Test
    public void nameCannotBeSetToNull() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);

        thrown.expect(InvalidNameException.class);
        thrown.expectMessage(INVALID_NAME_MESSAGE);
        resource.setName(null);
    }

    @Test
    public void nameCannotBeSetToEmpty() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);

        thrown.expect(InvalidNameException.class);
        thrown.expectMessage(INVALID_NAME_MESSAGE);
        resource.setName("");
    }

    @Test
    public void hostCannotBeNull() {
        thrown.expect(InvalidHostException.class);
        thrown.expectMessage(INVALID_HOST_MESSAGE);
        new Resource(VALID_ID, VALID_NAME, null, VALID_PORT);
    }

    @Test
    public void hostCannotBeEmpty() {
        thrown.expect(InvalidHostException.class);
        thrown.expectMessage(INVALID_HOST_MESSAGE);
        new Resource(VALID_ID, VALID_NAME, "", VALID_PORT);
    }

    @Test
    public void hostCannotBeSetToNull() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);

        thrown.expect(InvalidHostException.class);
        thrown.expectMessage(INVALID_HOST_MESSAGE);
        resource.setHost(null);
    }

    @Test
    public void hostCannotBeSetToEmpty() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);

        thrown.expect(InvalidHostException.class);
        thrown.expectMessage(INVALID_HOST_MESSAGE);
        resource.setHost("");
    }

    @Test
    public void portCannotBeNegative() {
        thrown.expect(InvalidPortException.class);
        new Resource(VALID_ID, VALID_NAME, VALID_HOST, -1);
    }

    @Test
    public void portCannotBeZero() {
        thrown.expect(InvalidPortException.class);
        new Resource(VALID_ID, VALID_NAME, VALID_HOST, 0);
    }

    @Test
    public void portCannotBeInvalidValue() {
        thrown.expect(InvalidPortException.class);
        new Resource(VALID_ID, VALID_NAME, VALID_HOST, 65536);
    }

    @Test
    public void portCannotBeSetToNegativeValue() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);

        thrown.expect(InvalidPortException.class);
        resource.setPort(-1);
    }

    @Test
    public void portCannotBeSetToInvalidValue() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT);

        thrown.expect(InvalidPortException.class);
        resource.setPort(65536);
    }

    @Test
    public void buildLimitCannotBeNegative() {
        thrown.expect(InvalidLimitException.class);
        new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT, -1);
    }

    @Test
    public void buildLimitCannotBeSetToNegativeValue() {
        Resource resource = new Resource(VALID_ID, VALID_NAME, VALID_HOST, VALID_PORT, VALID_LIMIT);

        thrown.expect(InvalidLimitException.class);
        thrown.expectMessage("invalid limit number");
        resource.setBuildLimit(-1);
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
