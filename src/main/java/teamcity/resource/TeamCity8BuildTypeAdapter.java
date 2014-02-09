package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TeamCity8BuildTypeAdapter implements BuildType {

    private SBuildType buildType;

    private String externalId;

    TeamCity8BuildTypeAdapter(SBuildType buildType) {
        this.buildType = buildType;
    }

    public String getBuildTypeId() {
        return buildType.getBuildTypeId();
    }

    public String getExternalId() {
        if (externalId == null) {
            try {
                Method method = buildType.getClass().getMethod("getExternalId", new Class[]{});
                externalId = (String) method.invoke(buildType, null);
            } catch (NoSuchMethodException e) {
                externalId = buildType.getBuildTypeId();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return externalId;
    }

    public String getFullName() {
        return buildType.getFullName();
    }
}
