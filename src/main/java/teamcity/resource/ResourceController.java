package teamcity.resource;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.AjaxRequestProcessor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceController extends BaseController {

    private final WebControllerManager webControllerManager;

    private final String pluginPath;

    private ResourceManager resourceManager;

    public ResourceController(SBuildServer buildServer, WebControllerManager webControllerManager,
                              ResourceManager resourceManager, PluginDescriptor pluginDescriptor)
    {
        super(buildServer);
        this.webControllerManager = webControllerManager;
        this.resourceManager = resourceManager;
        pluginPath = pluginDescriptor.getPluginResourcesPath();
    }

    public void register() {
        webControllerManager.registerController("/resource.html", this);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        new AjaxRequestProcessor().processRequest(request, response, new AjaxRequestProcessor.RequestHandler() {
            public void handleRequest(final HttpServletRequest request, final HttpServletResponse response, final Element xmlResponse) {
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
        Loggers.SERVER.info("resource name: [" + request.getParameter("resourceName") + "]");
        Loggers.SERVER.info("resource host: [" + request.getParameter("resourceHost") + "]");
        Loggers.SERVER.info("resource port: [" + request.getParameter("resourcePort") + "]");
        Loggers.SERVER.info("build type id: [" + request.getParameter("buildTypeId") + "]");

        String action = request.getParameter("submitAction");
        if ("saveResource".equals(action)) {
            String name = request.getParameter("resourceName");
            String host = request.getParameter("resourceHost");
            String port = request.getParameter("resourcePort");
            Resource resource = new Resource(name, host, Integer.valueOf(port));
            resourceManager.addResource(resource);
        } else if ("removeResource".equals(action)) {
            String name = request.getParameter("resourceName");
            resourceManager.removeResource(name);
        } else if ("linkBuildType".equals(action)) {
            String name = request.getParameter("resourceName");
            String buildTypeId = request.getParameter("buildTypeId");
            resourceManager.linkBuildToResource(name, buildTypeId);
        } else if ("unlinkBuildType".equals(action)) {
            String name = request.getParameter("resourceName");
            String buildTypeId = request.getParameter("buildTypeId");
            resourceManager.unlinkBuildFromResource(name, buildTypeId);
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
