package teamcity.resource;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class ResourceConfigExtension extends SimpleCustomTab {

    private ProjectManager projectManager;

    private ResourceMonitor resourceMonitor;

    public ResourceConfigExtension(PagePlaces pagePlaces, ProjectManager projectManager, ResourceMonitor resourceMonitor) {
        super(pagePlaces);
        this.projectManager = projectManager;
        this.resourceMonitor = resourceMonitor;
    }

    @NotNull
    public String getTabId() {
        return "resourceConfig";
    }

    @NotNull
    public String getTabTitle() {
        return "Resources";
    }

    @NotNull
    public List<String> getCssPaths() {
        List<String> cssPaths = new ArrayList<String>();
        cssPaths.add("resource.css");
        return cssPaths;
    }

    public boolean isVisible() {
        return true;
    }

    public boolean isAvailable(@NotNull HttpServletRequest request) {
        return true;
    }

    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        model.put("resources", getResources());
        model.put("buildTypes", getBuildTypes());
    }

    private List<Resource> getResources() {
        List<Resource> resources = new ArrayList<Resource>(resourceMonitor.getResources().values());
        return resources;
    }

    private Map getBuildTypes() {
        List<SBuildType> buildTypes = projectManager.getAllBuildTypes();
        Map<String, SBuildType> buildTypesMap = new HashMap<String, SBuildType>();
        for (SBuildType buildType : buildTypes) {
            buildTypesMap.put(buildType.getBuildTypeId(), buildType);
        }
        return buildTypesMap;
    }
}
