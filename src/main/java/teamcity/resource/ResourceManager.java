package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.*;

public class ResourceManager {

    private static final int DEFAULT_INTERVAL = 30;

    private Map<String, Resource> resources = new HashMap<String, Resource>();

    private Set<String> names = new HashSet<String>();

    private int interval = DEFAULT_INTERVAL;

    private ProjectManager projectManager;

    private List<ResourceManagerListener> listeners = new ArrayList<ResourceManagerListener>();

    private enum ResourceEvent { Added, Updated, Removed }

    public ResourceManager(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    public void addResource(String name, String host, String port) {
        String id = Integer.toString(nextId());
        Resource resource = new Resource(id, name, host, parsePort(port));
        addResource(resource);
    }

    public void addResource(Resource resource) {
        if (resources.containsKey(resource.getId())) {
            throw new IllegalArgumentException("resource with id " + resource.getId() + " already exists");
        }
        if (names.contains(resource.getName())) {
            throw new IllegalArgumentException("resource with name " + resource.getName() + " already exists");
        }
        names.add(resource.getName());
        resources.put(resource.getId(), resource);
        notifyListeners(ResourceEvent.Added, resource);
    }

    public void updateResource(String id, String name, String host, String port) {
        Integer portNumber = parsePort(port);

        Resource resource = getResource(id);
        resource.setName(name);
        resource.setHost(host);
        resource.setPort(portNumber);
        notifyListeners(ResourceEvent.Updated, resource);
    }

    public void removeResource(String id) {
        Resource resource = getResource(id);
        names.remove(resource.getName());
        resources.remove(resource.getId());
        notifyListeners(ResourceEvent.Removed, resource);
    }

    public Resource getResourceById(String id) {
        for (Resource resource : resources.values()) {
            if (resource.getId().equals(id)) {
                return resource;
            }
        }
        return null;
    }

    public Resource findResourceByBuildTypeId(String buildTypeId) {
        for (Resource resource : getResources()) {
            if (resource.getBuildTypes().contains(buildTypeId)) {
                return resource;
            }
        }
        return null;
    }

    public void setResources(Collection<Resource> resources) {
        this.names.clear();
        this.resources.clear();
        for (Resource resource : resources) {
            if (this.resources.containsKey(resource.getId())) {
                continue;
            }
            if (this.names.contains(resource.getName())) {
                continue;
            }
            this.names.add(resource.getName());
            this.resources.put(resource.getId(), resource);
            removeInvalidBuildTypes(resource);
        }
    }

    public Collection<Resource> getResources() {
        return Collections.unmodifiableCollection(resources.values());
    }

    public void linkBuildToResource(String id, String buildTypeId) {
        validBuildType(buildTypeId);
        Resource resource = getResource(id);
        resource.addBuildType(buildTypeId);
    }

    public void unlinkBuildFromResource(String id, String buildTypeId) {
        validBuildType(buildTypeId);
        Resource resource = getResource(id);
        resource.removeBuildType(buildTypeId);
    }

    public void unregisterBuild(String buildTypeId) {
        for (Resource resource : getResources()) {
            resource.removeBuildType(buildTypeId);
        }
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

    public void addListener(ResourceManagerListener listener) {
        listeners.add(listener);
    }

    private Resource getResource(String id) {
        validResource(id);
        return getResourceById(id);
    }

    private void validResource(String id) {
        if (!resources.containsKey(id)) {
            throw new IllegalArgumentException("resource with id " + id + " does not exist");
        }
    }

    private Integer parsePort(String value) {
        if (value == null || "".equals(value)) {
            throw new InvalidPortException("invalid port number");
        }
        Integer port;
        try {
            port = Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new InvalidPortException("invalid port number");
        }
        return port;
    }

    private void validBuildType(String buildTypeId) {
        SBuildType buildType = projectManager.findBuildTypeById(buildTypeId);
        if (buildType == null) {
            throw new IllegalArgumentException("build type id " + buildTypeId + " does not exist");
        }
    }

    private void notifyListeners(ResourceEvent event, Resource resource) {
        for (ResourceManagerListener listener : listeners) {
            switch (event) {
                case Added:
                    listener.resourceAdded(resource);
                    break;
                case Updated:
                    listener.resourceUpdated(resource);
                    break;
                case Removed:
                    listener.resourceRemoved(resource);
                    break;
            }
        }
    }

    private void removeInvalidBuildTypes(Resource resource) {
        List<String> invalidBuildTypeIds = new ArrayList<String>();
        for (String buildTypeId : resource.getBuildTypes()) {
            if (projectManager.findBuildTypeById(buildTypeId) == null) {
                invalidBuildTypeIds.add(buildTypeId);
            }
        }
        for (String buildTypeId : invalidBuildTypeIds) {
            resource.removeBuildType(buildTypeId);
        }
    }
}
