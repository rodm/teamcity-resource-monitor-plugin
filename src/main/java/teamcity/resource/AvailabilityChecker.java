package teamcity.resource;

import static teamcity.resource.ResourceMonitorPlugin.log;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;

public class AvailabilityChecker {

    public boolean isAvailable(Resource resource) {
        boolean result = false;
        try {
            Socket socket = new Socket(resource.getHost(), resource.getPort());
            socket.close();
            result = true;
            log.debug("Successfully connected to " + resource.getHost() + ":" + resource.getPort());
        }
        catch (NoRouteToHostException e) {
            log.warn("Error connecting to " + resource.getHost() + ":" + resource.getPort(), e);
        }
        catch (UnknownHostException e) {
            log.warn("Error connecting to " + resource.getHost() + ":" + resource.getPort(), e);
        }
        catch (ConnectException e) {
            log.debug("Failed to connect to " + resource.getHost() + ":" + resource.getPort());
        }
        catch (IOException e) {
            log.warn("Error connecting to " + resource.getHost() + ":" + resource.getPort(), e);
        }
        return result;
    }
}
