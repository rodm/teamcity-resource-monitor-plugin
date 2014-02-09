package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;

public class BuildTypeAdapter implements BuildType {

    private SBuildType buildType;

    BuildTypeAdapter(SBuildType buildType) {
        this.buildType = buildType;
    }

    public String getBuildTypeId() {
        return buildType.getBuildTypeId();
    }

    public String getExternalId() {
        return buildType.getBuildTypeId();
    }

    public String getFullName() {
        return buildType.getFullName();
    }
}
