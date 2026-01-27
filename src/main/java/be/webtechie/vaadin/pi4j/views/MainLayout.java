package be.webtechie.vaadin.pi4j.views;

import be.webtechie.vaadin.pi4j.views.electronics.BuzzerView;
import be.webtechie.vaadin.pi4j.views.electronics.IrReceiverView;
import be.webtechie.vaadin.pi4j.views.electronics.JoystickView;
import be.webtechie.vaadin.pi4j.views.electronics.KeyPressView;
import be.webtechie.vaadin.pi4j.views.electronics.LEDView;
import be.webtechie.vaadin.pi4j.views.electronics.LcdDisplayView;
import be.webtechie.vaadin.pi4j.views.electronics.OledDisplayView;
import be.webtechie.vaadin.pi4j.views.electronics.RedMatrixView;
import be.webtechie.vaadin.pi4j.views.electronics.RgbMatrixView;
import be.webtechie.vaadin.pi4j.views.electronics.SevenSegmentView;
import be.webtechie.vaadin.pi4j.views.electronics.SimpleBuzzerView;
import be.webtechie.vaadin.pi4j.views.electronics.TempHumidityView;
import be.webtechie.vaadin.pi4j.views.electronics.TouchView;
import be.webtechie.vaadin.pi4j.views.electronics.WeatherView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.firitin.util.style.LumoProps;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
// Ensure components from dynamically registered views are included in production bundle
@Uses(WeatherView.class)
@Uses(BuzzerView.class)
@Uses(IrReceiverView.class)
@Uses(JoystickView.class)
@Uses(SimpleBuzzerView.class)
@Uses(KeyPressView.class)
@Uses(LcdDisplayView.class)
@Uses(LEDView.class)
@Uses(OledDisplayView.class)
@Uses(RedMatrixView.class)
@Uses(RgbMatrixView.class)
@Uses(SevenSegmentView.class)
@Uses(TempHumidityView.class)
@Uses(TouchView.class)
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
