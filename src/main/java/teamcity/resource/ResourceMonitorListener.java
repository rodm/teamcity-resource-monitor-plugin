package teamcity.resource;

public interface ResourceMonitorListener {
    void resourceAvailable(Resource resource);

    void resourceUnavailable(Resource resource);

    void resourceEnabled(Resource resource);

    void resourceDisabled(Resource resource);
}
