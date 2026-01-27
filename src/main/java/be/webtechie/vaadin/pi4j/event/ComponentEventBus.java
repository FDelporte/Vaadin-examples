package be.webtechie.vaadin.pi4j.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.Registration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Bridges Spring application events to Vaadin UI components.
 *
 * <p>Services publish events using Spring's {@code ApplicationEventPublisher}.
 * Views subscribe to specific event types using this bus, which handles:
 * <ul>
 *   <li>Type-safe event filtering</li>
 *   <li>Automatic UI.access() for thread safety</li>
 *   <li>Automatic subscription cleanup on component detach</li>
 * </ul>
 *
 * <p>Example usage in a view:
 * <pre>{@code
 * public WeatherView(ComponentEventBus eventBus) {
 *     // Subscribe in constructor - lifecycle managed automatically
 *     eventBus.subscribe(this, BMP280Event.class, this::onWeatherUpdate);
 *
 *     // ... build UI
 * }
 *
 * private void onWeatherUpdate(BMP280Event event) {
 *     // Type-safe, runs in UI.access() automatically
 *     temperatureGauge.setTemperature(event.getMeasurement().temperature());
 * }
 * }</pre>
 */
@Service
public class ComponentEventBus {

    private final CopyOnWriteArrayList<Subscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    private record Subscription<T>(
            UI ui,
            Class<T> eventType,
            Consumer<T> listener
    ) {}

    /**
     * Subscribe a component to a specific event type.
     *
     * <p>Can be called from the component's constructor. The subscription is
     * automatically activated when the component attaches and removed when
     * it detaches.
     *
     * @param component The Vaadin component (typically the view itself)
     * @param eventType The event class to subscribe to
     * @param listener The type-safe event handler
     * @param <T> The event type
     */
    public <T> void subscribe(Component component, Class<T> eventType, Consumer<T> listener) {
        // Handle case where component is already attached
        component.getUI().ifPresentOrElse(
                ui -> registerSubscription(component, ui, eventType, listener),
                () -> {
                    // Not attached yet - wait for attach
                    component.addAttachListener(attachEvent ->
                            registerSubscription(component, attachEvent.getUI(), eventType, listener));
                }
        );
    }

    private <T> void registerSubscription(
            Component component, UI ui, Class<T> eventType, Consumer<T> listener) {
        var subscription = new Subscription<>(ui, eventType, listener);
        subscriptions.add(subscription);

        // Auto-cleanup on detach
        Registration[] detachReg = new Registration[1];
        detachReg[0] = component.addDetachListener(e -> {
            subscriptions.remove(subscription);
            detachReg[0].remove();
        });
    }

    /**
     * Receives Spring application events and dispatches to subscribed Vaadin components.
     * Runs asynchronously to avoid blocking the publishing threads.
     */
    @Async
    @EventListener
    public void onApplicationEvent(ApplicationEvent event) {
        for (var sub : subscriptions) {
            if (sub.eventType.isInstance(event)) {
                dispatch(sub, event);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void dispatch(Subscription<T> sub, Object event) {
        try {
            sub.ui.access(() -> sub.listener.accept((T) event));
        } catch (Exception e) {
            // UI might be detached or closed, ignore
        }
    }
}
