package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;

import java.util.ArrayList;
import java.util.List;

public class ProjectManagerAdapter implements ProjectManager {

    private SBuildServer server;

    private jetbrains.buildServer.serverSide.ProjectManager projectManager;

    public ProjectManagerAdapter(SBuildServer server, jetbrains.buildServer.serverSide.ProjectManager projectManager) {
        this.server = server;
        this.projectManager = projectManager;
    }

    public BuildType findBuildTypeById(String id) {
        SBuildType buildType = projectManager.findBuildTypeById(id);
        return buildType == null ? null : createBuildTypeAdapter(buildType);
    }

    public List<BuildType> getAllBuildTypes() {
        List<BuildType> buildTypes = new ArrayList<BuildType>();
        for (SBuildType buildType : projectManager.getAllBuildTypes()) {
            buildTypes.add(createBuildTypeAdapter(buildType));
        }
        return buildTypes;
    }

    private BuildType createBuildTypeAdapter(SBuildType buildType) {
        if (server.getServerMajorVersion() > 7) {
            return new TeamCity8BuildTypeAdapter(buildType);
        }
        return new BuildTypeAdapter(buildType);
    }
}
