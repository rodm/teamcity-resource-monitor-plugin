package teamcity.resource;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceController extends BaseController {

    private final WebControllerManager webControllerManager;

    private ResourceManager resourceManager;

    private ResourceMonitorPlugin plugin;

    private ResourceMonitor monitor;

    public ResourceController(SBuildServer buildServer, WebControllerManager webControllerManager,
                              ResourceManager resourceManager, ResourceMonitorPlugin plugin, ResourceMonitor monitor)
    {
        super(buildServer);
        this.webControllerManager = webControllerManager;
        this.resourceManager = resourceManager;
        this.plugin = plugin;
        this.monitor = monitor;
    }

    public void register() {
        webControllerManager.registerController("/resource.html", this);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        new AjaxRequestProcessor().processRequest(request, response, new AjaxRequestProcessor.RequestHandler() {
            public void handleRequest(@NotNull final HttpServletRequest request,
                                      @NotNull final HttpServletResponse response,
                                      @NotNull final Element xmlResponse)
            {
                try {
                    doAction(request);
                } catch (Exception e) {
                    Loggers.SERVER.warn(e);
                    ActionErrors errors = new ActionErrors();
                    errors.addError("Resource", getMessageWithNested(e));
                    errors.serialize(xmlResponse);
                }
            }
        });
        return null;
    }

    private void doAction(final HttpServletRequest request) throws Exception {
        Loggers.SERVER.info("       method: [" + request.getMethod() + "]");
        Loggers.SERVER.info("submit action: [" + request.getParameter("submitAction") + "]");
        Loggers.SERVER.info("  resource id: [" + request.getParameter("resourceId") + "]");
        Loggers.SERVER.info("resource name: [" + request.getParameter("resourceName") + "]");
        Loggers.SERVER.info("resource host: [" + request.getParameter("resourceHost") + "]");
        Loggers.SERVER.info("resource port: [" + request.getParameter("resourcePort") + "]");
        Loggers.SERVER.info("build type id: [" + request.getParameter("buildTypeId") + "]");

        String action = request.getParameter("submitAction");
        if ("addResource".equals(action)) {
            String name = request.getParameter("resourceName");
            String host = request.getParameter("resourceHost");
            String port = request.getParameter("resourcePort");
            resourceManager.addResource(name, host, port);
            plugin.saveConfiguration();
        } else if ("updateResource".equals(action)) {
            String id = request.getParameter("resourceId");
            String name = request.getParameter("resourceName");
            String host = request.getParameter("resourceHost");
            String port = request.getParameter("resourcePort");
            resourceManager.updateResource(id, name, host, port);
            plugin.saveConfiguration();
        } else if ("removeResource".equals(action)) {
            String id = request.getParameter("resourceId");
            resourceManager.removeResource(id);
            plugin.saveConfiguration();
        } else if ("linkBuildType".equals(action)) {
            String id = request.getParameter("resourceId");
            String buildTypeId = request.getParameter("buildTypeId");
            resourceManager.linkBuildToResource(id, buildTypeId);
            plugin.saveConfiguration();
        } else if ("unlinkBuildType".equals(action)) {
            String id = request.getParameter("resourceId");
            String buildTypeId = request.getParameter("buildTypeId");
            resourceManager.unlinkBuildFromResource(id, buildTypeId);
            plugin.saveConfiguration();
        } else if ("enableResource".equals(action)) {
            String id = request.getParameter("resourceId");
            monitor.enableResource(resourceManager.getResourceById(id));
        } else if ("disableResource".equals(action)) {
            String id = request.getParameter("resourceId");
            monitor.disableResource(resourceManager.getResourceById(id));
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    private String getMessageWithNested(Throwable e) {
        String result = e.getMessage();
        Throwable cause = e.getCause();
        if (cause != null) {
            result += " Caused by: " + getMessageWithNested(cause);
        }
        return result;
    }
}
