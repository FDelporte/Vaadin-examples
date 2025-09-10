package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Temperature and Humidity")
@Route("temperature-humidity")
@Menu(order = 1, icon = LineAwesomeIconUrl.THERMOMETER_EMPTY_SOLID)
public class TempHumidityView extends HorizontalLayout implements ChangeListener {

    private final Pi4JService pi4JService;
    private final LogGrid logs;
    private static Logger logger = LoggerFactory.getLogger(TempHumidityView.class);

    public TempHumidityView(Pi4JService pi4JService) {
        this.pi4JService = pi4JService;

        setMargin(true);

        logs = new LogGrid();
        add(logs);
    }

    @Override
    public void onAttach(AttachEvent attachEvent) {
        pi4JService.addListener(this);
    }

    @Override
    public void onDetach(DetachEvent detachEvent) {
        pi4JService.removeListener(this);
    }

    @Override
    public void onMessage(ChangeType type, String message) {
        if (!type.equals(ChangeType.DHT11)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine(message);
    }
}
