package teamcity.resource;

import jetbrains.buildServer.log.Loggers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

class AvailabilityChecker {

    public boolean isAvailable(Resource resource) {
        boolean result = false;
        try {
            Socket socket = new Socket(resource.getHost(), resource.getPort());
            socket.close();
            result = true;
        }
        catch (UnknownHostException e) {
            Loggers.SERVER.warn("Error connecting to " + resource.getHost() + ":" + resource.getPort(), e);
        }
        catch (ConnectException e) {
            Loggers.SERVER.debug("Failed to connect to " + resource.getHost() + ":" + resource.getPort());
        }
        catch (IOException e) {
            Loggers.SERVER.warn("Error connecting to " + resource.getHost() + ":" + resource.getPort(), e);
        }
        return result;
    }
}
