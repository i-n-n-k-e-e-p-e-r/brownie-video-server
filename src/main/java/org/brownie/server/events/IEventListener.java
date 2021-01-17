package org.brownie.server.events;

import java.util.List;

public interface IEventListener {

    boolean update(EventsManager.EVENT_TYPE eventType, Object... params);
    List<EventsManager.EVENT_TYPE> getEventTypes();

}
