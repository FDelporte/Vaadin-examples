package be.webtechie.vaadin.pi4j.views.about;

import be.webtechie.vaadin.pi4j.service.SystemInformationService;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.lang.management.MemoryUsage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Based on
 * https://github.com/mstahv/VaadinTetris/blob/master/src/main/java/org/vaadin/example/ServerDetails.java
 */
@PageTitle("About - System Information")
@Route(value = "about/system", layout = MainLayout.class)
public class AboutSystemView extends VerticalLayout {

    private final ScheduledFuture<?> scheduledFuture;
    private final SystemInformationService sis;
    private final Paragraph memory = new Paragraph();
    private final Paragraph cpu = new Paragraph();
    private final Paragraph players = new Paragraph();


    public AboutSystemView(SystemInformationService sis, ScheduledExecutorService es) {
        this.sis = sis;
        setSpacing(false);

        scheduledFuture = es.scheduleWithFixedDelay(this::updateDetails, 5, 5, TimeUnit.SECONDS);
        add(new H3("Server details"));
        add(new Paragraph("Running on: " + "TODO" + ". Below some usage stats."));
        add(memory, cpu, players);

        Paragraph latencyReport = new Paragraph();
        Button testLatency = new Button("Ping server (test server-round-trip latency)");
        testLatency.setId("latencyTestButton");
        // store the click time on the client side before hitting the server
        testLatency.getElement().executeJs("var el = this; this.addEventListener('click', function(){el.start = new Date().getTime();});");
        testLatency.addClickListener(e -> {
            // Ask the browser to execute this JS that reads the click time and reports how much it took to get back
            latencyReport.getElement().executeJs("var start = document.getElementById('latencyTestButton').start; this.innerHTML='Server roundtrip and client side overhead took: ' + (new Date().getTime() - start) +'ms';");
        });
        add(testLatency, latencyReport);

        doUpdateDetails();
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }

    private void doUpdateDetails() {
        MemoryUsage usage = sis.getHeapMemoryUsage();
        memory.setText(String.format("Heap used/available: %s/%s ",
                usage == null ? "unknown" : formatSize(usage.getUsed()),
                usage == null ? "unknown" : formatSize(usage.getMax()))
        );
        cpu.setText(String.format("CPU usage: %,.2f", sis.getSystemLoad()));

        // This estimation is twice too bad in the beginning and may change due to JVM optimisations, thus report only after n users are in
        String max;
        if (sis.getLastSessionSizeGuestimate() > 0) {
            max = String.valueOf(sis.getNumberOfSessions() + (usage.getMax() - usage.getUsed()) / sis.getLastSessionSizeGuestimate());
        } else {
            max = "unknown (more sessions needed for an estimate)";
        }
        players.setText("Active users: estimated max: " + sis.getNumberOfSessions() + "/" + max + ", estimated memory usage per player " + formatSize(sis.getLastSessionSizeGuestimate()));
    }

    private void updateDetails() {
        getUI().ifPresentOrElse(
                ui -> ui.access(() -> doUpdateDetails()),
                () -> scheduledFuture.cancel(true)
        );
    }
}
