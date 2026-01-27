package be.webtechie.vaadin.pi4j.service;

import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.pi4j.boardinfo.model.BoardInfo;
import com.pi4j.context.Context;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class Pi4JService {

    private static final Logger logger = LoggerFactory.getLogger(Pi4JService.class);

    private Set<Class<? extends Component>> enabledViews = new HashSet<>();

    private final Context pi4j;

    public Pi4JService(Context pi4j) {
        this.pi4j = pi4j;
    }

    public BoardInfo getBoardInfo() {
        return pi4j.boardInfo();
    }

    public List<String> getLoadedPlatforms() {
        if (pi4j == null || pi4j.platforms() == null) {
            return List.of("None");
        }
        return pi4j.platforms().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
    }

    public List<String> getProviders() {
        if (pi4j == null || pi4j.providers() == null) {
            return List.of("None");
        }
        return pi4j.providers().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
    }

    public List<String> getRegistry() {
        if (pi4j == null || pi4j.registry() == null) {
            return List.of("None");
        }
        return pi4j.registry().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
    }

    /**
     * Registers a view to be enabled when Vaadin context starts
     *
     * @param viewClass
     */
    public void registerView(Class<? extends Component> viewClass) {
        enabledViews.add(viewClass);
    }

    @EventListener
    void onVaadinStart(ServiceInitEvent evt) {
        logger.info("Registering {} views enabled based on features", enabledViews.size());
        enabledViews.forEach(view -> {
            logger.info("Registering view {}", view.getSimpleName());
            RouteConfiguration.forApplicationScope().setRoute(view.getSimpleName().toLowerCase(), view, MainLayout.class);
        });
    }
}