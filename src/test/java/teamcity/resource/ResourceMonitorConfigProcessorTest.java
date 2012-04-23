package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceMonitorConfigProcessorTest {

    private static final String BUILD_TYPE_ID = "bt1";

    private ResourceManager manager;
    private ResourceMonitorConfigProcessor configProcessor;

    @Before
    public void setup() {
        ProjectManager projectManager = mock(ProjectManager.class);
        manager = new ResourceManager(projectManager);
        configProcessor = new ResourceMonitorConfigProcessor(manager);

        SBuildType buildType = mock(SBuildType.class);
        when(projectManager.findBuildTypeById(BUILD_TYPE_ID)).thenReturn(buildType);
    }

    @Test
    public void shouldWriteRootElementAndDefaultCheckInterval() throws Exception {
        StringWriter writer = new StringWriter();
        configProcessor.writeTo(writer);

        assertXpathEvaluatesTo("1", "count(//monitored-resources)", writer.toString());
        assertXpathEvaluatesTo("30", "/monitored-resources/@check-interval", writer.toString());
    }

    @Test
    public void shouldWriteOutNewCheckInterval() throws Exception {
        manager.setInterval(15);

        StringWriter writer = new StringWriter();
        configProcessor.writeTo(writer);

        assertXpathEvaluatesTo("15", "/monitored-resources/@check-interval", writer.toString());
    }

    @Test
    public void shouldWriteOutResource() throws Exception {
        Resource resource = new Resource("1", "Resource1", "localhost", 1080);
        resource.setBuildLimit(123);
        manager.addResource(resource);

        StringWriter writer = new StringWriter();
        configProcessor.writeTo(writer);

        assertXpathEvaluatesTo("1", "//resource/@id", writer.toString());
        assertXpathEvaluatesTo("Resource1", "//resource/@name", writer.toString());
        assertXpathEvaluatesTo("localhost", "//resource/@host", writer.toString());
        assertXpathEvaluatesTo("1080", "//resource/@port", writer.toString());
        assertXpathEvaluatesTo("123", "//resource/@build-limit", writer.toString());
    }

    @Test
    public void shouldWriteOutResourceWithBuildTypeIds() throws Exception {
        Resource resource = new Resource("1", "Resource1", "locahost", 1080);
        resource.addBuildType("bt1");
        resource.addBuildType("bt2");
        manager.addResource(resource);

        StringWriter writer = new StringWriter();
        configProcessor.writeTo(writer);

        assertXpathEvaluatesTo("2", "count(//resource/build-type)", writer.toString());
        assertXpathEvaluatesTo("bt1", "//resource/build-type[1]/@id", writer.toString());
        assertXpathEvaluatesTo("bt2", "//resource/build-type[2]/@id", writer.toString());
    }

    @Test
    public void shouldReadEmptyConfig() throws Exception {
        String config = "<monitored-resources check-interval=\"25\"/>";
        Reader reader = new StringReader(config);
        configProcessor.readFrom(reader);

        assertEquals(0, manager.getResources().size());
        assertEquals(25, manager.getInterval());
    }

    @Test
    public void shouldReadResource() throws Exception {
        String config = "<monitored-resources check-interval=\"25\">" +
                        "    <resource id=\"123\" name=\"Resource\" host=\"localhost\" port=\"1234\" build-limit=\"123\"/>" +
                        "</monitored-resources>";
        Reader reader = new StringReader(config);
        configProcessor.readFrom(reader);

        assertEquals(1, manager.getResources().size());
        Resource resource = manager.getResourceById("123");
        assertEquals("123", resource.getId());
        assertEquals("Resource", resource.getName());
        assertEquals("localhost", resource.getHost());
        assertEquals(1234, resource.getPort());
        assertEquals(123, resource.getBuildLimit());
    }

    @Test
    public void shouldReadResourceWithBuildTypeIds() throws Exception {
        String config = "<monitored-resources check-interval=\"25\">" +
                        "    <resource id=\"123\" name=\"Resource\" host=\"localhost\" port=\"1234\">" +
                        "        <build-type id=\"" + BUILD_TYPE_ID + "\"/>" +
                        "    </resource>" +
                        "</monitored-resources>";
        Reader reader = new StringReader(config);
        configProcessor.readFrom(reader);

        Resource resource = manager.getResourceById("123");
        assertEquals(1, resource.getBuildTypes().size());
        assertEquals(BUILD_TYPE_ID, resource.getBuildTypes().get(0));
    }

    @Test
    public void ignoreResourcesWithSameId() throws Exception {
        String config = "<monitored-resources check-interval=\"25\">" +
                        "    <resource id=\"1\" name=\"Resource1\" host=\"host1\" port=\"123\"/>" +
                        "    <resource id=\"1\" name=\"Resource2\" host=\"host2\" port=\"456\"/>" +
                        "</monitored-resources>";
        Reader reader = new StringReader(config);
        configProcessor.readFrom(reader);
        assertEquals(1, manager.getResources().size());
    }

    @Test
    public void ignoreResourcesWithSameName() throws Exception {
        String config = "<monitored-resources check-interval=\"25\">" +
                        "    <resource id=\"1\" name=\"Resource\" host=\"host1\" port=\"123\"/>" +
                        "    <resource id=\"2\" name=\"Resource\" host=\"host2\" port=\"456\"/>" +
                        "</monitored-resources>";
        Reader reader = new StringReader(config);
        configProcessor.readFrom(reader);
        assertEquals(1, manager.getResources().size());
    }
}
