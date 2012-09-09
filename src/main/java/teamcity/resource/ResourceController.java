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

    private static final String ACTION_PARAMETER = "submitAction";
    private static final String ID_PARAMETER = "resourceId";
    private static final String NAME_PARAMETER = "resourceName";
    private static final String HOST_PARAMETER = "resourceHost";
    private static final String PORT_PARAMETER = "resourcePort";
    private static final String LIMIT_PARAMETER = "resourceLimit";
    private static final String BUILD_TYPE_ID_PARAMETER = "buildTypeId";

    private static final String ADD_ACTION = "addResource";
    private static final String UPDATE_ACTION = "updateResource";
    private static final String REMOVE_ACTION = "removeResource";
    private static final String LINK_BUILD_ACTION = "linkBuildType";
    private static final String UNLINK_BUILD_ACTION = "unlinkBuildType";
    private static final String ENABLE_ACTION = "enableResource";
    private static final String DISABLE_ACTION = "disableResource";

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
                } catch (InvalidNameException e) {
                    buildExceptionResponse("invalidName", e, xmlResponse);
                } catch (InvalidHostException e) {
                    buildExceptionResponse("invalidHost", e, xmlResponse);
                } catch (InvalidPortException e) {
                    buildExceptionResponse("invalidPort", e, xmlResponse);
                } catch (InvalidLimitException e) {
                    buildExceptionResponse("invalidLimit", e, xmlResponse);
                } catch (Exception e) {
                    Loggers.SERVER.warn(e);
                    buildExceptionResponse("resource", e, xmlResponse);
                }
            }
        });
        return null;
    }

    private void doAction(final HttpServletRequest request) throws Exception {
        Loggers.SERVER.debug("       method: [" + request.getMethod() + "]");
        Loggers.SERVER.debug("submit action: [" + request.getParameter(ACTION_PARAMETER) + "]");
        Loggers.SERVER.debug("  resource id: [" + request.getParameter(ID_PARAMETER) + "]");
        Loggers.SERVER.debug("resource name: [" + request.getParameter(NAME_PARAMETER) + "]");
        Loggers.SERVER.debug("resource host: [" + request.getParameter(HOST_PARAMETER) + "]");
        Loggers.SERVER.debug("resource port: [" + request.getParameter(PORT_PARAMETER) + "]");
        Loggers.SERVER.debug("build type id: [" + request.getParameter(BUILD_TYPE_ID_PARAMETER) + "]");

        String action = request.getParameter(ACTION_PARAMETER);
        if (ADD_ACTION.equals(action)) {
            String name = request.getParameter(NAME_PARAMETER);
            String host = request.getParameter(HOST_PARAMETER);
            String port = request.getParameter(PORT_PARAMETER);
            String limit = request.getParameter(LIMIT_PARAMETER);
            resourceManager.addResource(name, host, port, limit);
            plugin.saveConfiguration();
        } else if (UPDATE_ACTION.equals(action)) {
            String id = request.getParameter(ID_PARAMETER);
            String name = request.getParameter(NAME_PARAMETER);
            String host = request.getParameter(HOST_PARAMETER);
            String port = request.getParameter(PORT_PARAMETER);
            String limit = request.getParameter(LIMIT_PARAMETER);
            resourceManager.updateResource(id, name, host, port, limit);
            plugin.saveConfiguration();
        } else if (REMOVE_ACTION.equals(action)) {
            String id = request.getParameter(ID_PARAMETER);
            resourceManager.removeResource(id);
            plugin.saveConfiguration();
        } else if (LINK_BUILD_ACTION.equals(action)) {
            String id = request.getParameter(ID_PARAMETER);
            String buildTypeId = request.getParameter(BUILD_TYPE_ID_PARAMETER);
            resourceManager.linkBuildToResource(id, buildTypeId);
            plugin.saveConfiguration();
        } else if (UNLINK_BUILD_ACTION.equals(action)) {
            String id = request.getParameter(ID_PARAMETER);
            String buildTypeId = request.getParameter(BUILD_TYPE_ID_PARAMETER);
            resourceManager.unlinkBuildFromResource(id, buildTypeId);
            plugin.saveConfiguration();
        } else if (ENABLE_ACTION.equals(action)) {
            String id = request.getParameter(ID_PARAMETER);
            monitor.enableResource(resourceManager.getResourceById(id));
        } else if (DISABLE_ACTION.equals(action)) {
            String id = request.getParameter(ID_PARAMETER);
            monitor.disableResource(resourceManager.getResourceById(id));
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }
    }

    private void buildExceptionResponse(String name, Exception e, Element xmlResponse) {
        ActionErrors errors = new ActionErrors();
        errors.addError(name, getMessageWithNested(e));
        errors.serialize(xmlResponse);
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
