package teamcity.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Resource {

    private String id = "";

    private String name = "";

    private String host = null;

    private int port = -1;

    private int buildLimit = 0;

    private List<String> buildTypes = new ArrayList<String>();

    public Resource(String id, String name, String host, int port) {
        checkId(id);
        checkName(name);
        checkHost(host);
        checkPort(port);
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        checkName(name);
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        checkHost(host);
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        checkPort(port);
        this.port = port;
    }

    public int getBuildLimit() {
        return buildLimit;
    }

    public void setBuildLimit(int buildLimit) {
        if (buildLimit < 0) {
            throw new InvalidLimitException("invalid build limit");
        }
        this.buildLimit = buildLimit;
    }

    public List<String> getBuildTypes() {
        return Collections.unmodifiableList(buildTypes);
    }

    public void addBuildType(String buildTypeId) {
        buildTypes.add(buildTypeId);
    }

    public void removeBuildType(String buildTypeId) {
        buildTypes.remove(buildTypeId);
    }

    private void checkId(String value) {
        if (value == null || "".equals(value)) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
    }

    private void checkName(String name) {
        if (name == null || "".equals(name)) {
            throw new InvalidNameException("name cannot be null or empty");
        }
    }

    private void checkHost(String host) {
        if (host == null || "".equals(host)) {
            throw new InvalidHostException("host cannot be null or empty");
        }
    }

    private void checkPort(int port) {
        if (port < 1 || port > 65535) {
            throw new InvalidPortException("invalid port number");
        }
    }
}
