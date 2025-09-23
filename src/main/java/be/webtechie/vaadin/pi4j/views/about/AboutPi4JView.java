package be.webtechie.vaadin.pi4j.views.about;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("About - Pi4J Information")
@Route("")
@RouteAlias(value = "about/pi4j")
@Menu(order = 0, icon = LineAwesomeIconUrl.JAVA)
public class AboutPi4JView extends VerticalLayout {

    public AboutPi4JView(@Autowired Pi4JService pi4JService) {
        setSpacing(false);
        add(
                new H2("Detected board"),
                new Paragraph("Board model: " + pi4JService.getBoardInfo().getBoardModel().getLabel()
                        + ", " + pi4JService.getBoardInfo().getBoardModel().getNumberOfCpu()
                        + "x" + pi4JService.getBoardInfo().getBoardModel().getCpu()),
                new Paragraph("OS: " + pi4JService.getBoardInfo().getOperatingSystem().getName()
                        + ", " + pi4JService.getBoardInfo().getOperatingSystem().getArchitecture()
                        + ", " + pi4JService.getBoardInfo().getOperatingSystem().getVersion()),
                new H2("Java"),
                new Paragraph("Version: " + pi4JService.getBoardInfo().getJavaInfo().getVersion()
                        + " (" + pi4JService.getBoardInfo().getJavaInfo().getRuntime() + ")"),
                new Paragraph("Vendor: " + pi4JService.getBoardInfo().getJavaInfo().getVendor()
                        + " (" + pi4JService.getBoardInfo().getJavaInfo().getVendorVersion() + ")"),
                new H2("Pi4J"),
                new Paragraph("Default platform: " + pi4JService.getDefaultPlatform()),
                new Paragraph("Loaded platforms: " + pi4JService.getLoadedPlatforms()),
                new Paragraph("Providers: " + pi4JService.getProviders()),
                new Paragraph("Registry: " + pi4JService.getRegistry())
        );
        setSizeFull();
        getStyle().setTextAlign(Style.TextAlign.LEFT);
    }
}
