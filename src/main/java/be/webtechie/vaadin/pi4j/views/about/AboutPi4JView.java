package be.webtechie.vaadin.pi4j.views.about;

import be.webtechie.vaadin.pi4j.config.BoardConfig;
import be.webtechie.vaadin.pi4j.service.Pi4JService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;

@PageTitle("About - Pi4J Information")
@Route("")
@RouteAlias(value = "about/pi4j")
@Menu(order = 0, icon = LineAwesomeIconUrl.JAVA)
public class AboutPi4JView extends VerticalLayout {

    public AboutPi4JView(@Autowired Pi4JService pi4JService, @Autowired BoardConfig boardConfig) {
        setSpacing(false);
        add(
                new H2("Development Board"),
                getDevelopmentBoardInfo(boardConfig),
                new H2("Detected Raspberry Pi"),
                getBoardInfo(pi4JService),
                new H2("Java"),
                getJavaInfo(pi4JService),
                new H2("Pi4J"),
                new H3("Loaded platforms"),
                asUnorderedList(pi4JService.getLoadedPlatforms()),
                new H3("Providers"),
                asUnorderedList(pi4JService.getProviders()),
                new H3("Registry"),
                asUnorderedList(pi4JService.getRegistry())
        );
        setSizeFull();
        getStyle().setTextAlign(Style.TextAlign.LEFT);
    }

    private UnorderedList getDevelopmentBoardInfo(BoardConfig config) {
        var listView = new UnorderedList();
        listView.add(new ListItem("Configuration: " + config.getClass().getSimpleName()));
        listView.add(new ListItem("Available features: " + getAvailableFeatures(config)));
        return listView;
    }

    private String getAvailableFeatures(BoardConfig config) {
        StringBuilder features = new StringBuilder();
        if (config.hasLed()) features.append("LED, ");
        if (config.hasKey()) features.append("Key, ");
        if (config.hasTouch()) features.append("Touch, ");
        if (config.hasLcd()) features.append("LCD, ");
        if (config.hasSevenSegment()) features.append("7-Segment, ");
        if (config.hasBuzzer()) features.append("Buzzer, ");
        if (config.hasRedMatrix()) features.append("Red Matrix, ");
        if (config.hasRGBMatrix()) features.append("RGB Matrix, ");
        if (config.hasOled()) features.append("OLED, ");
        if (config.hasDht11()) features.append("DHT11, ");

        String result = features.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        return result.isEmpty() ? "None" : result;
    }

    private UnorderedList getJavaInfo(Pi4JService pi4JService) {
        var listView = new UnorderedList();
        listView.add(new ListItem("Version: " + pi4JService.getBoardInfo().getJavaInfo().getVersion()
                + " (" + pi4JService.getBoardInfo().getJavaInfo().getRuntime() + ")"));
        listView.add(new ListItem("Vendor: " + pi4JService.getBoardInfo().getJavaInfo().getVendor()
                + " (" + pi4JService.getBoardInfo().getJavaInfo().getVendorVersion() + ")"));
        return listView;
    }

    private UnorderedList getBoardInfo(Pi4JService pi4JService) {
        var listView = new UnorderedList();
        listView.add(new ListItem("Board model: " + pi4JService.getBoardInfo().getBoardModel().getLabel()
                + ", " + pi4JService.getBoardInfo().getBoardModel().getNumberOfCpu()
                + "x" + pi4JService.getBoardInfo().getBoardModel().getCpu()));
        listView.add(new ListItem("OS: " + pi4JService.getBoardInfo().getOperatingSystem().getName()
                + ", " + pi4JService.getBoardInfo().getOperatingSystem().getArchitecture()
                + ", " + pi4JService.getBoardInfo().getOperatingSystem().getVersion()));
        return listView;
    }

    private UnorderedList asUnorderedList(List<String> list) {
        var listView = new UnorderedList();
        list.forEach(listItem -> listView.add(new ListItem(listItem)));
        return listView;
    }
}
