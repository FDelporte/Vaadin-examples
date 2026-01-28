package be.webtechie.vaadin.pi4j.views.electronics;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * Base class for hardware demo views that are conditionally registered
 * based on available hardware features.
 *
 * <p>The route path is auto-generated from the subclass name. Routes are registered
 * at runtime by the corresponding hardware service when the feature is available.
 */
@Route(registerAtStartup = false)
public abstract class HardwareDemoView extends VerticalLayout {

    protected HardwareDemoView() {
        setMargin(true);
        setSpacing(true);
    }
}
