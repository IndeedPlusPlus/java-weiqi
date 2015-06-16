package moe.indeed.homework.go.client.data;

import java.net.InetAddress;

public class Server {
    private InetAddress address;
    private String name;

    public Server(InetAddress address, String name) {
        this.address = address;
        this.name = name;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (" + address + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Server server = (Server) o;

        if (address != null ? !address.equals(server.address) : server.address != null) return false;
        return !(name != null ? !name.equals(server.name) : server.name != null);

    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
