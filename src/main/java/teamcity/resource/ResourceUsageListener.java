package teamcity.resource;

public interface ResourceUsageListener {
    void resourceUsageChanged(Resource resource, int count);
}
