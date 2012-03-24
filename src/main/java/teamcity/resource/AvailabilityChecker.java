package teamcity.resource;

import java.io.IOException;
import java.net.Socket;

class AvailabilityChecker {

    public boolean isAvailable(Resource resource) {
        boolean result = false;
        try {
            Socket socket = new Socket(resource.getHost(), resource.getPort());
            socket.close();
            result = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
