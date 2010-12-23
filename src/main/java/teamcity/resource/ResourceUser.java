package teamcity.resource;

import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.User;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

public class ResourceUser implements User {

    public long getId() {
        return -1;
    }

    public String getRealm() {
        return "";
    }

    public String getUsername() {
        return "";
    }

    public String getName() {
        return "ResourceUser";
    }

    public String getEmail() {
        return "";
    }

    public String getDescriptiveName() {
        return "ResourceUser";
    }

    public Date getLastLoginTimestamp() {
        return new Date();
    }

    public String getPropertyValue(PropertyKey propertyKey) {
        return "";
    }

    public Map<PropertyKey, String> getProperties() {
        return emptyMap();
    }

    public List<String> getVisibleProjects() {
        return emptyList();
    }

    public List<String> getAllProjects() {
        return emptyList();
    }
}