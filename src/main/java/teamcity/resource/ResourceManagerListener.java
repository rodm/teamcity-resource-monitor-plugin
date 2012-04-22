package teamcity.resource;

public interface ResourceManagerListener {
    void resourceAdded(Resource resource);

    void resourceUpdated(Resource resource);

    void resourceRemoved(Resource resource);
}
