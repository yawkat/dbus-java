
package at.yawk.dbus.client;

import java.util.Objects;

public class DBUSDestinationImpl implements DBUSDestination {
    private final String bus;
    private final String destination;
    private final String objectPath;

    public DBUSDestinationImpl() {
        this.bus = null;
        this.destination = null;
        this.objectPath = null;
    }

    public DBUSDestinationImpl(String bus, String destination, String objectPath) {
        this.bus = bus;
        this.destination = destination;
        this.objectPath = objectPath;
    }

    public String getBus() {
        return bus;
    }

    public String getDestination() {
        return destination;
    }

    public String getObjectPath() {
        return objectPath;
    }
    
    public DBUSDestinationImpl withBus(String bus) {
        return new DBUSDestinationImpl(bus, this.getDestination(), this.getObjectPath());
    }
    
     public DBUSDestinationImpl withDestination(String destination) {
        return new DBUSDestinationImpl(this.getBus(), destination, this.getObjectPath());
    }

    public DBUSDestinationImpl withObjectPath(String objectPath) {
        return new DBUSDestinationImpl(this.getBus(), this.getDestination(), objectPath);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.bus);
        hash = 47 * hash + Objects.hashCode(this.destination);
        hash = 47 * hash + Objects.hashCode(this.objectPath);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DBUSDestinationImpl other = (DBUSDestinationImpl) obj;
        if (!Objects.equals(this.bus, other.bus)) {
            return false;
        }
        if (!Objects.equals(this.destination, other.destination)) {
            return false;
        }
        if (!Objects.equals(this.objectPath, other.objectPath)) {
            return false;
        }
        return true;
    }
}
