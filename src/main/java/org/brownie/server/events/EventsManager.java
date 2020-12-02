package org.brownie.server.events;

import javax.validation.constraints.NotNull;
import java.util.*;

public class EventsManager {

    public enum EVENT_TYPE {
        FILE_SYSTEM_CHANGED,
        ENCODING_STARTED,
        ENCODING_FINISHED,
        PLAYER_OPENED,
        PLAYER_CLOSED
    }

    private static final Map<EVENT_TYPE, List<IEventListener>> listeners = Collections.synchronizedMap(new HashMap<>());

    private static EventsManager manager = null;

    private EventsManager() {}

    public static EventsManager getManager() {
        synchronized (EventsManager.class) {
            if (manager == null) {
                manager = new EventsManager();

                return manager;
            }
        }
        return manager;
    }

    public void registerListener(@NotNull IEventListener listener) {
        if (listener.getEventTypes() == null || listener.getEventTypes().size() == 0) return;

        listener.getEventTypes().forEach(eventType -> {
            checkEventType(eventType);
            listeners.get(eventType).add(listener);
        });

        System.out.println("REGISTERED " + listener);
    }

    public void unregisterListener(@NotNull IEventListener listener) {
        if (listener.getEventTypes() == null || listener.getEventTypes().size() == 0) return;

        listener.getEventTypes().forEach(eventType -> {
            checkEventType(eventType);
            listeners.get(eventType).remove(listener);
        });

        System.out.println("UNREGISTERED " + listener);
    }

    public void notifyAllListeners(EVENT_TYPE eventType, Object[] params) {
        new Thread(() -> {
            checkEventType(eventType);
            listeners.get(eventType).parallelStream().forEach(listener -> listener.update(eventType, params));
        }).start();
    }

    private void checkEventType(EVENT_TYPE eventType) {
        listeners.computeIfAbsent(eventType, k -> Collections.synchronizedList(new ArrayList<>()));
    }

}
