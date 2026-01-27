package be.webtechie.vaadin.pi4j.views;

import be.webtechie.vaadin.pi4j.views.electronics.*;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.auth.AnonymousAllowed;

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
    protected String getDrawerHeader() {
        return "Pi4J Vaadin Demo";
    }

}
