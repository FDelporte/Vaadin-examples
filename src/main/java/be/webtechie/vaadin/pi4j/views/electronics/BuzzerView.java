package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.buzzer.Note;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Buzzer")
@Route("buzzer")
@Menu(order = 1, icon = LineAwesomeIconUrl.VOLUME_UP_SOLID)
public class BuzzerView extends VerticalLayout implements ChangeListener {
    private final Logger logger = LoggerFactory.getLogger(BuzzerView.class);

    private final Pi4JService pi4JService;
    private final LogGrid logs;

    public BuzzerView(Pi4JService pi4JService) {
        this.pi4JService = pi4JService;

        setMargin(true);

        var buttonHolder = new FlexLayout();
        buttonHolder.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        add(buttonHolder);

        for (Note note : Note.values()) {
            var noteButton = new Button(note.name());
            noteButton.setWidth(50, Unit.PIXELS);
            noteButton.getStyle().set("margin-right", "10px");
            noteButton.addClickListener(e -> pi4JService.playNote(note));
            buttonHolder.add(noteButton);
        }

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
        if (!type.equals(ChangeType.BUZZER)) {
            return;
        }
        logger.debug("Message received: {}", message);
        logs.addLine(message);
    }
}

