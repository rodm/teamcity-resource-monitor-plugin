package teamcity.resource;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class ResourceMonitorConfigProcessorTest {

    private ResourceMonitor monitor;
    private ResourceMonitorConfigProcessor configProcessor;

    @Before
    public void setup() {
        monitor = new ResourceMonitor();
        configProcessor = new ResourceMonitorConfigProcessor(monitor);
    }

    @Test
    public void writeConfig() throws IOException {
        List<String> buildTypeIds = new ArrayList<String>();
        buildTypeIds.add("bt1");
        buildTypeIds.add("bt2");
        buildTypeIds.add("bt3");
        Resource resource1 = new Resource("Resource1", "locahost", 1080);
        resource1.setBuildTypes(buildTypeIds);
        monitor.addResource(resource1);

        Resource resource2 = new Resource("Resource2", "locahost", 1080);
        monitor.addResource(resource2);
        Element element = new Element("test");
        configProcessor.writeTo(element);

        StringWriter out = new StringWriter();
        XMLOutputter o = new XMLOutputter();
        o.output(element, out);
        System.out.println(out);
    }

    @Test
    public void ignoreResourcesWithSameName() {
        final Element root = new Element("root");
        final Element configRoot = new Element("monitored-resources");
        configRoot.setAttribute("check-interval", "30");
        root.addContent(configRoot);
        configRoot.addContent(createResource("resource", "host1", "123"));
        configRoot.addContent(createResource("resource", "host2", "456"));

        configProcessor.readFrom(root);
        assertEquals(1, monitor.getResources().size());
    }

    private Element createResource(String name, String host, String port) {
        final Element resource = new Element("resource");
        resource.setAttribute("name", name);
        resource.setAttribute("host", host);
        resource.setAttribute("port", port);
        return resource;
    }
}
