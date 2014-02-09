package teamcity.resource;

import java.util.List;

public interface ProjectManager {
    BuildType findBuildTypeById(String id);
    List<BuildType> getAllBuildTypes();
}
