package teamcity.resource;

import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class ResourceConfigExtension extends SimpleCustomTab {

    private ResourceMonitor resourceMonitor;

    public ResourceConfigExtension(PagePlaces pagePlaces, ResourceMonitor resourceMonitor) {
        super(pagePlaces);
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
    }

    private List<Resource> getResources() {
        List<Resource> resources = new ArrayList<Resource>(resourceMonitor.getResources().values());
        return resources;
    }
}
