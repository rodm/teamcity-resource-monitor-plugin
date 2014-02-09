package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeProjectManager implements ProjectManager {

    private Map<String, BuildType> buildTypes = new HashMap<String, BuildType>();

    public BuildType findBuildTypeById(String id) {
        return buildTypes.get(id);
    }

    public List<BuildType> getAllBuildTypes() {
        return new ArrayList<BuildType>(buildTypes.values());
    }

    public void addBuildType(String id, SBuildType buildType) {
        buildTypes.put(id, new BuildTypeAdapter(buildType));
    }
}
