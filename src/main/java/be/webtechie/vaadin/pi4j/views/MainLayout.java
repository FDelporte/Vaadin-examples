package be.webtechie.vaadin.pi4j.views;

import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends org.vaadin.firitin.appframework.MainLayout {

    @Override
    protected String getDrawerHeader() {
        return "Pi4J }>";
    }

    public void doMagic() {

    }
}
