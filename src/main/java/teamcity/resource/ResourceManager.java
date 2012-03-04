package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.*;

public class ResourceManager {

    private static final int DEFAULT_INTERVAL = 30;

    private Set<String> ids = new HashSet<String>();

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
        if (ids.contains(resource.getId())) {
            throw new IllegalArgumentException("resource with id '" + resource.getId() + "' already exists");
        }
        if (resources.containsKey(resource.getName())) {
            throw new IllegalArgumentException("resource with name " + resource.getName() + " already exists");
        }
        ids.add(resource.getId());
        resources.put(resource.getName(), resource);
    }

    public void updateResource(String id, String name, String host, int port) {
        Resource resource = getResource(id);
        resource.setName(name);
        resource.setHost(host);
        resource.setPort(port);
    }

    public void removeResource(String id) {
        Resource resource = getResource(id);
        ids.remove(resource.getId());
        resources.remove(resource.getName());
    }

    public void enableResource(String id) {
        getResource(id).enable();
    }

    public void disableResource(String id) {
        getResource(id).disable();
    }

    public void setResources(Map<String,Resource> resources) {
        this.ids.clear();
        this.resources = resources;
        for (Resource resource : resources.values()) {
            ids.add(resource.getId());
        }
    }

    public Map<String,Resource> getResources() {
        return Collections.unmodifiableMap(resources);
    }

    public void linkBuildToResource(String id, String buildTypeId) {
        validBuildType(buildTypeId);
        Resource resource = getResource(id);
        resource.getBuildTypes().add(buildTypeId);
    }

    public void unlinkBuildFromResource(String id, String buildTypeId) {
        validBuildType(buildTypeId);
        Resource resource = getResource(id);
        resource.getBuildTypes().remove(buildTypeId);
    }

    public int nextId() {
        int highestId = 0;
        for (Resource resource : resources.values()) {
            int id = Integer.parseInt(resource.getId());
            if (id > highestId) {
                highestId = id;
            }
        }
        return highestId + 1;
    }

    private Resource getResource(String id) {
        validResource(id);
        for (Resource resource : resources.values()) {
            if (resource.getId().equals(id)) {
                return resource;
            }
        }
        return null;
    }

    private void validResource(String id) {
        if (!ids.contains(id)) {
            throw new IllegalArgumentException("resource with id " + id + " does not exist");
        }
    }

    private void validBuildType(String buildTypeId) {
        SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
        if (buildType == null) {
            throw new IllegalArgumentException("build type id " + buildTypeId + " does not exist");
        }
    }
}
