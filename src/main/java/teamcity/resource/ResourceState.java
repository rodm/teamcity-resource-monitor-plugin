package teamcity.resource;

import java.util.List;

public class ResourceState {

    private Resource resource;

    private boolean available;

    private boolean enabled;

    public ResourceState(Resource resource, boolean available, boolean enabled) {
        this.resource = resource;
        this.available = available;
        this.enabled = enabled;
    }

    public String getId() {
        return resource.getId();
    }

    public String getName() {
        return resource.getName();
    }

    public String getHost() {
        return resource.getHost();
    }

    public int getPort() {
        return resource.getPort();
    }

    public int getBuildLimit() {
        return resource.getBuildLimit();
    }

    public List<String> getBuildTypes() {
        return resource.getBuildTypes();
    }

    public List<String> getMatchedBuildTypes() {
        return resource.getMatchedBuildTypes();
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
