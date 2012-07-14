package teamcity.resource;

import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;

import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class ResourceStatusController extends BaseController
        implements ResourceMonitorListener, ResourceUsageListener
{

    private Map<String, Boolean> availability = new HashMap<String, Boolean>();

    private Map<String, Integer> usage = new HashMap<String, Integer>();

    public ResourceStatusController(WebControllerManager controllerManager, ResourceMonitor resourceMonitor) {
        controllerManager.registerController("/resourceStatus.html", this);
        resourceMonitor.addListener(this);
    }

    public void resourceAvailable(Resource resource) {
        availability.put(resource.getId(), Boolean.TRUE);
    }

    public void resourceUnavailable(Resource resource) {
        availability.put(resource.getId(), Boolean.FALSE);
    }

    public void resourceEnabled(Resource resource) {
    }

    public void resourceDisabled(Resource resource) {
    }

    public void resourceUsageChanged(Resource resource, int count) {
        usage.put(resource.getId(), count);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        new AjaxRequestProcessor().processRequest(request, response, new AjaxRequestProcessor.RequestHandler() {
            public void handleRequest(@NotNull final HttpServletRequest request,
                                      @NotNull final HttpServletResponse response,
                                      @NotNull final Element xmlResponse)
            {
                try {
                    doAction(xmlResponse);
                } catch (Exception e) {
                }
            }
        });
        return null;
    }

    private void doAction(Element xmlResponse) {
        if (availability.size() > 0 || usage.size() > 0) {
            Element resources = new Element("resources");
            for (Map.Entry<String, Boolean> entry : availability.entrySet()) {
                addAvailabilityStatusTo(resources, entry.getKey(), entry.getValue());
            }
            availability.clear();
            for (Map.Entry<String, Integer> entry : usage.entrySet()) {
                addUsageStatusTo(resources, entry.getKey(), entry.getValue());
            }
            xmlResponse.addContent(resources);
        }
    }

    private void addAvailabilityStatusTo(Element resources, String id, boolean available) {
        Element resource = new Element("resource");
        resource.setAttribute("id", id);
        resource.setAttribute("available", Boolean.toString(available));
        resources.addContent(resource);
    }

    private void addUsageStatusTo(Element resources, String id, int count) {
        Element resource = new Element("resource");
        resource.setAttribute("id", id);
        resource.setAttribute("count", Integer.toString(count));
        resources.addContent(resource);
    }
}
