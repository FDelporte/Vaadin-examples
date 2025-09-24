package be.webtechie.vaadin.pi4j.views.electronics;

import be.webtechie.vaadin.pi4j.service.ChangeListener;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.service.buzzer.Note;
import be.webtechie.vaadin.pi4j.service.buzzer.PlayNote;
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

import java.util.Arrays;

@PageTitle("Buzzer")
@Route("buzzer")
@Menu(order = 12, icon = LineAwesomeIconUrl.VOLUME_UP_SOLID)
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

        Arrays.stream(Note.values()).forEach(note -> buttonHolder.add(new NoteButton(note)));

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
    public <T> void onMessage(ChangeType type, T message) {
        if (!type.equals(ChangeType.BUZZER) && !(message instanceof PlayNote)) {
            return;
        }
        var playNote = (PlayNote) message;
        logger.debug("PlayNote message received: {}, duration {}", playNote.note(), playNote.duration());
        logs.addLine("Note " + playNote.note() + ", frequency " + playNote.note().getFrequency() + ", duration " + playNote.duration());
    }

    private class NoteButton extends Button {
        public NoteButton(Note note) {
            this.setText(note.name());
            this.setWidth(50, Unit.PIXELS);
            this.getStyle().setMarginRight("10px");
            this.addClickListener(e -> pi4JService.playNote(new PlayNote(note, 150)));
        }
    }
}

