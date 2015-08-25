package teamcity.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Resource {

    private String id = "";

    private String name = "";

    private String host = null;

    private int port = -1;

    private int buildLimit = 0;

    private List<Pattern> patterns = new ArrayList<Pattern>();

    private List<String> buildTypes = new ArrayList<String>();

    private List<String> matchedBuildTypes = new ArrayList<String>();

    public Resource(String id, String name, String host, int port) {
        this(id, name, host, port, 0);
    }

    public Resource(String id, String name, String host, int port, int limit) {
        checkId(id);
        checkName(name);
        checkHost(host);
        checkPort(port);
        checkBuildLimit(limit);
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
        this.buildLimit = limit;
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
        checkBuildLimit(buildLimit);
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

    private void checkBuildLimit(int buildLimit) {
        if (buildLimit < 0) {
            throw new InvalidLimitException("invalid limit number");
        }
    }

    public List<Pattern> getMatchers() {
        return Collections.unmodifiableList(patterns);
    }

    public void addBuildTypeMatcher(String pattern) {
        patterns.add(Pattern.compile(pattern));
    }

    public void buildTypeRegistered(BuildType buildType) {
        if (nameMatches(buildType.getFullName())) {
            matchedBuildTypes.add(buildType.getBuildTypeId());
        }
    }

    public void buildTypeUnregistered(BuildType buildType) {
        if (nameMatches(buildType.getFullName())) {
            matchedBuildTypes.remove(buildType.getBuildTypeId());
        }
    }

    public void buildTypePersisted(BuildType buildType) {
        if (nameMatches(buildType.getFullName())) {
            matchedBuildTypes.add(buildType.getBuildTypeId());
        } else {
            matchedBuildTypes.remove(buildType.getBuildTypeId());
        }
    }

    private boolean nameMatches(String name) {
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(name);
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getMatchedBuildTypes() {
        List<String> result = new ArrayList<String>(matchedBuildTypes);
        result.removeAll(buildTypes);
        return Collections.unmodifiableList(result);
    }

    public List<String> getAllBuildTypes() {
        List<String> allBuildTypes = new ArrayList<String>();
        allBuildTypes.addAll(getBuildTypes());
        allBuildTypes.addAll(getMatchedBuildTypes());
        return allBuildTypes;
    }
}
