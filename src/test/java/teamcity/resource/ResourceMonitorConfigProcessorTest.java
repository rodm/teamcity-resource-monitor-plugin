package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;
import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.mockito.Mockito.mock;

public class ResourceMonitorConfigProcessorTest {

    private static final String BUILD_TYPE_ID = "bt1";

    private ResourceManager manager;
    private ResourceMonitorConfigProcessor configProcessor;

    @Before
    public void setup() {
        FakeProjectManager projectManager = new FakeProjectManager();
        SBuildType buildType = mock(SBuildType.class);
        projectManager.addBuildType(BUILD_TYPE_ID, buildType);

        manager = new ResourceManager(projectManager);
        configProcessor = new ResourceMonitorConfigProcessor(manager);
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
    public void shouldWriteOutResourceMatchers() throws Exception {
        Resource resource = new Resource("1", "Resource1", "localhost", 1000);
        resource.addBuildTypeMatcher("matcher1");
        resource.addBuildTypeMatcher("matcher2");

        manager.addResource(resource);

        StringWriter writer = new StringWriter();
        configProcessor.writeTo(writer);

        assertXpathEvaluatesTo("2", "count(//resource/matcher)", writer.toString());
        assertXpathEvaluatesTo("matcher1", "//resource/matcher[1]/@name", writer.toString());
        assertXpathEvaluatesTo("matcher2", "//resource/matcher[2]/@name", writer.toString());
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
    public void shouldReadResourceWithMatchers() throws Exception {
        String config = "<monitored-resources check-interval=\"25\">" +
                "    <resource id=\"123\" name=\"Resource\" host=\"localhost\" port=\"1234\">" +
                "        <build-type id=\"" + BUILD_TYPE_ID + "\"/>" +
                "        <matcher name=\"build type pattern\"/>" +
                "    </resource>" +
                "</monitored-resources>";
        Reader reader = new StringReader(config);
        configProcessor.readFrom(reader);

        Resource resource = manager.getResourceById("123");
        assertEquals(1, resource.getMatchers().size());
        assertEquals("build type pattern", resource.getMatchers().get(0).pattern());
    }
}
