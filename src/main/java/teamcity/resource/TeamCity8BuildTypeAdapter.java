package teamcity.resource;

import jetbrains.buildServer.serverSide.SBuildType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TeamCity8BuildTypeAdapter extends BuildTypeAdapter {

    private String externalId;

    TeamCity8BuildTypeAdapter(SBuildType buildType) {
        super(buildType);
    }

    public String getExternalId() {
        if (externalId == null) {
            SBuildType buildType = getBuildType();
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
}
