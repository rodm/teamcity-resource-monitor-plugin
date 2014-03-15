package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;

public abstract class BuildTypeAdapter implements BuildType {

    private SBuildType buildType;

    public BuildTypeAdapter(SBuildType buildType) {
        this.buildType = buildType;
    }

    protected SBuildType getBuildType() {
        return buildType;
    }

    public String getBuildTypeId() {
        return buildType.getBuildTypeId();
    }

    public String getFullName() {
        return buildType.getFullName();
    }
}
