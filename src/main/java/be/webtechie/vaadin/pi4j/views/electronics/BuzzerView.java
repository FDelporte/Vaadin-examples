package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.event.BuzzerEvent;
import be.webtechie.vaadin.pi4j.event.ComponentEventBus;
import be.webtechie.vaadin.pi4j.service.buzzer.BuzzerService;
import be.webtechie.vaadin.pi4j.service.buzzer.Note;
import be.webtechie.vaadin.pi4j.service.buzzer.PlayNote;
import be.webtechie.vaadin.pi4j.views.component.LogGrid;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.Arrays;

@PageTitle("Buzzer")
// @Route("buzzer") - Conditionally registered by BuzzerService
@Menu(order = 12, icon = LineAwesomeIconUrl.VOLUME_UP_SOLID)
public class BuzzerView extends VerticalLayout {
    private final Logger logger = LoggerFactory.getLogger(BuzzerView.class);

    private final BuzzerService buzzerService;
    private final LogGrid logs;

    public BuzzerView(ComponentEventBus eventBus, BuzzerService buzzerService) {
        this.buzzerService = buzzerService;

        eventBus.subscribe(this, BuzzerEvent.class, this::onBuzzerEvent);

        setMargin(true);

        var buttonHolder = new FlexLayout();
        buttonHolder.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        add(buttonHolder);

        Arrays.stream(Note.values()).forEach(note -> buttonHolder.add(new NoteButton(note)));

        logs = new LogGrid();
        add(logs);
    }

    private void onBuzzerEvent(BuzzerEvent event) {
        var playNote = event.getPlayNote();
        logger.debug("PlayNote message received: {}, duration {}", playNote.note(), playNote.duration());
        logs.addLine("Note " + playNote.note() + ", frequency " + playNote.note().getFrequency() + ", duration " + playNote.duration());
    }

    private class NoteButton extends Button {
        public NoteButton(Note note) {
            this.setText(note.name());
            this.setWidth(50, Unit.PIXELS);
            this.getStyle().setMarginRight("10px");
            this.addClickListener(e -> buzzerService.playNote(new PlayNote(note, 150)));
        }
    }
}
