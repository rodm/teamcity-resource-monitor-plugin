package teamcity.resource;

public class FakeBuildType implements BuildType {

    private String buildTypeId;

    private String name;

    FakeBuildType(String buildTypeId, String name) {
        this.buildTypeId = buildTypeId;
        this.name = name;
    }

    @Override
    public String getBuildTypeId() {
        return buildTypeId;
    }

    @Override
    public String getExternalId() {
        return null;
    }

    @Override
    public String getFullName() {
        return name;
    }
}
