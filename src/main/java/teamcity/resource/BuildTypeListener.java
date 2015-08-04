package teamcity.resource;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import org.jetbrains.annotations.NotNull;

public class BuildTypeListener extends BuildServerAdapter {

    private ResourceManager resourceManager;

    private ProjectManager projectManager;

    public BuildTypeListener(@NotNull SBuildServer server, ResourceManager resourceManager, ProjectManager projectManager) {
        this.resourceManager = resourceManager;
        this.projectManager = projectManager;
        server.addListener(this);
    }

    @Override
    public void buildTypeRegistered(@NotNull SBuildType buildType) {
        resourceManager.buildTypeRegistered(projectManager.toBuildType(buildType));
    }

    @Override
    public void buildTypeUnregistered(@NotNull SBuildType buildType) {
        resourceManager.buildTypeUnregistered(projectManager.toBuildType(buildType));
    }

    public void buildTypePersisted(@NotNull SBuildType buildType) {
        resourceManager.buildTypePersisted(projectManager.toBuildType(buildType));
    }
}
