package org.brownie.server.events;

import org.brownie.server.db.User;

import java.util.List;

public interface IEventListener {

    boolean update(EventsManager.EVENT_TYPE eventType, User user, Object... params);
    List<EventsManager.EVENT_TYPE> getEventTypes();

}
