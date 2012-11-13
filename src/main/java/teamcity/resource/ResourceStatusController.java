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

    private Map<String, Status> status = new HashMap<String, Status>();

    public ResourceStatusController(WebControllerManager controllerManager, ResourceMonitor resourceMonitor, ResourceBuildLimitStartPrecondition precondition) {
        controllerManager.registerController("/resourceStatus.html", this);
        resourceMonitor.addListener(this);
        precondition.addListener(this);
    }

    public void resourceAvailable(Resource resource) {
        getStatus(resource.getId()).available = true;
    }

    public void resourceUnavailable(Resource resource) {
        getStatus(resource.getId()).available = false;
    }

    public void resourceEnabled(Resource resource) {
    }

    public void resourceDisabled(Resource resource) {
    }

    public void resourceUsageChanged(Resource resource, int count) {
        getStatus(resource.getId()).count = count;
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
                }
                catch (Exception ignored) {
                }
            }
        });
        return null;
    }

    private void doAction(Element xmlResponse) {
        if (status.size() > 0) {
            Element resources = new Element("resources");
            for (Map.Entry<String, Status> entry : status.entrySet()) {
                Element resource = new Element("resource");
                resource.setAttribute("id", entry.getKey());
                resource.setAttribute("available", Boolean.toString(entry.getValue().available));
                resource.setAttribute("count", Integer.toString(entry.getValue().count));
                resources.addContent(resource);
            }
            xmlResponse.addContent(resources);
        }
    }

    private Status getStatus(String id) {
        Status resourceStatus = status.get(id);
        if (resourceStatus == null) {
            resourceStatus = new Status();
            status.put(id, resourceStatus);
        }
        return resourceStatus;
    }

    private static class Status {
        boolean available = true;
        int count = 0;
    }
}
