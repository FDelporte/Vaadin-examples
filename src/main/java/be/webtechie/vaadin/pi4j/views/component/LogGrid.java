package be.webtechie.vaadin.pi4j.views.component;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogGrid extends Grid<LogGrid.LogLine> {

    private final Logger logger = LoggerFactory.getLogger(LogGrid.class);

    private final List<LogLine> lines;

    public LogGrid() {
        this.lines = Collections.synchronizedList(new ArrayList<>());

        this.addColumn(new LocalDateTimeRenderer<>(LogLine::timestamp, "HH:mm:ss.SSS"))
                .setHeader("Timestamp")
                .setFlexGrow(1);
        this.addColumn(LogLine::message).setHeader("Message")
                .setFlexGrow(3);
        this.setItems(this.lines);
    }

    public void addLine(String message) {
        logger.debug("Adding line {}", message);
        lines.add(0, new LogLine(LocalDateTime.now(), message));
        this.getDataProvider().refreshAll();
    }

    public record LogLine(LocalDateTime timestamp, String message) {
    }
}
