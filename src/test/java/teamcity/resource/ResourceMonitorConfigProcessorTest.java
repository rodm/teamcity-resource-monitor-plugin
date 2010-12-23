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
}
