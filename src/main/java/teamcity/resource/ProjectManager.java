package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;

import java.util.List;

public interface ProjectManager {
    BuildType findBuildTypeById(String id);
    List<BuildType> getAllBuildTypes();
    BuildType toBuildType(SBuildType buildType);
}
