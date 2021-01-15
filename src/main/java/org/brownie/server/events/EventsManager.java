package org.brownie.server.events;

import org.brownie.server.Application;

import javax.validation.constraints.NotNull;
import java.util.*;

public class EventsManager {

    public enum EVENT_TYPE {
        FILE_CREATED,
        FILE_MOVED,
        FILE_DELETED,
        FILE_RENAMED,
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

    public synchronized void registerListener(@NotNull IEventListener listener) {
        if (listener.getEventTypes() == null || listener.getEventTypes().size() == 0) return;
        synchronized (listeners) {
            listener.getEventTypes().forEach(eventType ->
                    listeners.computeIfAbsent(eventType,
                            k -> Collections.synchronizedList(new ArrayList<>())).add(listener));
        }
        Application.LOGGER.log(System.Logger.Level.DEBUG, "Listener registered " + listener);
    }

    public void unregisterListener(@NotNull IEventListener listener) {
        if (listener.getEventTypes() == null || listener.getEventTypes().size() == 0) return;
        synchronized (listeners) {
            listener.getEventTypes().forEach(eventType ->
                    listeners.computeIfAbsent(eventType,
                            k -> Collections.synchronizedList(new ArrayList<>())).remove(listener));
        }
        Application.LOGGER.log(System.Logger.Level.DEBUG, "Listener unregistered " + listener);
    }

    public void notifyAllListeners(EVENT_TYPE eventType, Object... params) {
        Application.LOGGER.log(System.Logger.Level.DEBUG, "Notify all listeners");
        new Thread(() -> {
            synchronized (listeners) {
                listeners.computeIfAbsent(eventType,
                    k -> Collections.synchronizedList(new ArrayList<>()))
                    .forEach(listener -> listener.update(eventType, params));
            }
        }).start();
    }
}
