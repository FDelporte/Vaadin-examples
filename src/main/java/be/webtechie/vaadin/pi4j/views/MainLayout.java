package be.webtechie.vaadin.pi4j.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.theme.lumo.Lumo;
import org.vaadin.firitin.util.style.LumoProps;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet(Lumo.UTILITY_STYLESHEET)
public class MainLayout extends org.vaadin.firitin.appframework.MainLayout {

    @Override
    protected Component getDrawerHeader() {
        var logo = new Image("https://avatars.githubusercontent.com/u/2251562?s=200&v=4", "Pi4J Logo");
        logo.setWidth(LumoProps.ICON_SIZE_L.var());
        logo.setHeight(LumoProps.ICON_SIZE_L.var());

        var title = new H1("Pi4J Vaadin Demo");
        title.getStyle()
                .setFontSize(LumoProps.FONT_SIZE_L.var());

        return new VerticalLayout(logo, title);
    }

}
