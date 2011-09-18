package teamcity.resource;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResourceController extends BaseController {

    private final WebControllerManager webControllerManager;

    private final String pluginPath;

    private ResourceMonitor resourceMonitor;

    public ResourceController(SBuildServer buildServer, WebControllerManager manager, PluginDescriptor pluginDescriptor, ResourceMonitor monitor) {
        super(buildServer);
        webControllerManager = manager;
        pluginPath = pluginDescriptor.getPluginResourcesPath();
        resourceMonitor = monitor;
    }

    public void register() {
        webControllerManager.registerController("/resource.html", this);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Loggers.SERVER.info("       method: [" + request.getMethod() + "]");
        Loggers.SERVER.info("submit action: [" + request.getParameter("submitAction") + "]");
        Loggers.SERVER.info("resource name: [" + request.getParameter("resourceName") + "]");
        Loggers.SERVER.info("resource host: [" + request.getParameter("resourceHost") + "]");
        Loggers.SERVER.info("resource port: [" + request.getParameter("resourcePort") + "]");

        String name = request.getParameter("resourceName");
        String host = request.getParameter("resourceHost");
        String port = request.getParameter("resourcePort");
        Resource resource = new Resource(name, host, Integer.valueOf(port));
        resourceMonitor.addResource(resource);
        return new ModelAndView(pluginPath + "response.jsp");
    }
}
