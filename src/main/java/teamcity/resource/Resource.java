package teamcity.resource;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Resource {

    private String id = "";

    private String name = "";

    private String host = null;

    private int port = -1;

    private boolean enabled = true;

    private List<String> buildTypes = new ArrayList<String>();

    public Resource(String id, String name, String host, int port) {
        if (id == null || "".equals(id)) {
            throw new IllegalArgumentException("id cannot be null or empty");
        }
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        this.id = id;
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setBuildTypes(List<String> buildTypes) {
        this.buildTypes = buildTypes;
    }

    public List<String> getBuildTypes() {
        return buildTypes;
    }

    public boolean isAvailable() {
        boolean result = false;
        if (enabled) {
            try {
                Socket socket = new Socket(host, port);
                socket.close();
                result = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}
