package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {

    private static final int DEFAULT_INTERVAL = 30;

    private Map<String, Resource> resources = new HashMap<String, Resource>();

    private int interval = DEFAULT_INTERVAL;

    private ProjectManager projectManager;

    public ResourceManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public void addResource(Resource resource) {
        if (resources.containsKey(resource.getName())) {
            throw new IllegalArgumentException("resource with name " + resource.getName() + " already exists");
        }
        resources.put(resource.getName(), resource);
    }

    public void updateResource(String name, String host, int port) {
        validResource(name);
        Resource resource = resources.get(name);
        resource.setHost(host);
        resource.setPort(port);
    }

    public void removeResource(String name) {
        validResource(name);
        resources.remove(name);
    }

    public void enableResource(String name) {
        validResource(name);
        resources.get(name).enable();
    }

    public void disableResource(String name) {
        validResource(name);
        resources.get(name).disable();
    }

    public void setResources(Map<String,Resource> resources) {
        this.resources = resources;
    }

    public Map<String,Resource> getResources() {
        return resources;
    }

    public void linkBuildToResource(String name, String buildTypeId) {
        validResource(name);
        validBuildType(buildTypeId);
        Resource resource = resources.get(name);
        resource.getBuildTypes().add(buildTypeId);
    }

    public void unlinkBuildFromResource(String name, String buildTypeId) {
        validResource(name);
        validBuildType(buildTypeId);
        Resource resource = resources.get(name);
        resource.getBuildTypes().remove(buildTypeId);
    }

    private void validResource(String name) {
        if (!resources.containsKey(name)) {
            throw new IllegalArgumentException("resource with name " + name + " does not exist");
        }
    }

    private void validBuildType(String buildTypeId) {
        SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
        if (buildType == null) {
            throw new IllegalArgumentException("build type id " + buildTypeId + " does not exist");
        }
    }
}
