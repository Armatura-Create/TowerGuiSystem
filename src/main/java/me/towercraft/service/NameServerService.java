package me.towercraft.service;

import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.wrapper.Wrapper;

public class NameServerService {

    public String getNameServer() {
        ServiceInfoSnapshot currentServiceInfoSnapshot = Wrapper.getInstance().getCurrentServiceInfoSnapshot();
        return currentServiceInfoSnapshot.getName();
    }

}
