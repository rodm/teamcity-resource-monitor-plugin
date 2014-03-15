package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;

public class TeamCity7BuildTypeAdapter extends BuildTypeAdapter {

    TeamCity7BuildTypeAdapter(SBuildType buildType) {
        super(buildType);
    }

    public String getExternalId() {
        return getBuildType().getBuildTypeId();
    }

}
