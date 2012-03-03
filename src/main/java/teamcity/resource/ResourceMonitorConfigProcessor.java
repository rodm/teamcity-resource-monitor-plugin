package teamcity.resource;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.log.Loggers;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

public class ResourceMonitorConfigProcessor {

    private static final String CONFIG_ROOT = "monitored-resources";
    private static final String CONFIG_CHECK_INTERVAL = "check-interval";
    private static final String CONFIG_RESOURCE = "resource";
    private static final String CONFIG_ID = "id";
    private static final String CONFIG_NAME = "name";
    private static final String CONFIG_HOST = "host";
    private static final String CONFIG_PORT = "port";
    private static final String CONFIG_BUILD_TYPE = "build-type";
    private static final String CONFIG_BUILD_TYPE_ID = "id";

    private static final int DEFAULT_CHECK_INTERVAL = 30;

    private static final Logger log = Loggers.SERVER;

    private final ResourceManager resourceManager;

    public ResourceMonitorConfigProcessor(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void readFrom(Reader reader) throws JDOMException, IOException {
        log.info("ResourceMonitor reading config");
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(reader);
        Element configRoot = document.getRootElement();
        Set<String> ids = new HashSet<String>();
        Map<String, Resource> resources = new HashMap<String, Resource>();
        resourceManager.setInterval(readCheckIntervalFrom(configRoot));
        final List list = configRoot.getChildren(CONFIG_RESOURCE);
        for (Object o : list) {
            final Element element = (Element) o;
            Resource resource = readResourceFrom(element);
            if (ids.contains(resource.getId())) {
                continue;
            }
            if (resources.containsKey(resource.getName())) {
                continue;
            }
            ids.add(resource.getId());
            resources.put(resource.getName(), resource);
        }
        log.info("ResourceMonitor config loaded");
        resourceManager.setResources(resources);
    }

    private int readCheckIntervalFrom(Element configRoot) {
        int interval = DEFAULT_CHECK_INTERVAL;
        String checkInterval = "";
        try {
            checkInterval = configRoot.getAttributeValue(CONFIG_CHECK_INTERVAL);
            interval = Integer.valueOf(checkInterval);
        }
        catch (NumberFormatException e) {
            log.error("Invalid check interval: " + checkInterval);
        }
        return interval;
    }

    private Resource readResourceFrom(Element element) {
        final String id = element.getAttributeValue(CONFIG_ID);
        final String name = element.getAttributeValue(CONFIG_NAME);
        final String host = element.getAttributeValue(CONFIG_HOST);
        final int port = readPortFrom(element);
        Resource resource = new Resource(id, name, host, port);
        resource.setBuildTypes(readBuildTypesFrom(element));
        return resource;
    }

    private int readPortFrom(Element element) {
        int port = -1;
        String portValue = "";
        try {
            portValue = element.getAttributeValue(CONFIG_PORT);
            port = Integer.valueOf(portValue);
        }
        catch (NumberFormatException e) {
            log.error("Invalid port: " + portValue);
        }
        return port;
    }

    private List<String> readBuildTypesFrom(Element resourceElement) {
        List<String> buildTypes = new ArrayList<String>();
        final List list = resourceElement.getChildren(CONFIG_BUILD_TYPE);
        for (Object o : list) {
            final Element element = (Element) o;
            final String buildTypeId = element.getAttributeValue(CONFIG_BUILD_TYPE_ID);
            buildTypes.add(buildTypeId);
        }
        return buildTypes;
    }

    public void writeTo(Writer writer) throws IOException {
        log.info("ResourceMonitor writing config");
        Element root = new Element(CONFIG_ROOT);
        root.setAttribute(CONFIG_CHECK_INTERVAL, Integer.toString(resourceManager.getInterval()));
        Map<String, Resource> resources = resourceManager.getResources();
        for (Resource resource : resources.values()) {
            writeResourceTo(resource, root);
        }

        XMLOutputter xmlWriter = new XMLOutputter(Format.getPrettyFormat());
        xmlWriter.output(root, writer);
    }

    private void writeResourceTo(Resource resource, Element parentElement) {
        final Element element = new Element(CONFIG_RESOURCE);
        parentElement.addContent(element);
        element.setAttribute(CONFIG_ID, resource.getId());
        element.setAttribute(CONFIG_NAME, resource.getName());
        element.setAttribute(CONFIG_HOST, resource.getHost());
        element.setAttribute(CONFIG_PORT, Integer.toString(resource.getPort()));
        writeBuildTypesTo(resource.getBuildTypes(), element);
    }

    private void writeBuildTypesTo(List<String> buildTypeIds, Element parentElement) {
        for (String id : buildTypeIds) {
            final Element element = new Element(CONFIG_BUILD_TYPE);
            parentElement.addContent(element);
            element.setAttribute(CONFIG_BUILD_TYPE_ID, id);
        }
    }
}
