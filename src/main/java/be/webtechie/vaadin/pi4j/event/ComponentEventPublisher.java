package be.webtechie.vaadin.pi4j.event;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Component
public class ComponentEventPublisher {

    private static final Executor executor = Executors.newSingleThreadExecutor();
    private final Queue<ChangeListener> listeners = new ConcurrentLinkedQueue<>();

    public synchronized void addListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public synchronized <T> void publish(ChangeListener.ChangeType type, T message) {
        for (ChangeListener listener : listeners) {
            executor.execute(() -> listener.onMessage(type, message));
        }
    }
}