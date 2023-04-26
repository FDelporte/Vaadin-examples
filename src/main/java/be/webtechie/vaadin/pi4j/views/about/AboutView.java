package be.webtechie.vaadin.pi4j.views.about;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class AboutView extends VerticalLayout {

    public AboutView(@Autowired Pi4JService pi4JService) {
        setSpacing(false);
        add(
                new H2("Java"),
                new Paragraph("Version: " + System.getProperty("java.version")),
                new H2("Pi4J"),
                new Paragraph("Default platform: " + pi4JService.getDefaultPlatform()),
                new Paragraph("Loaded platforms: " + pi4JService.getLoadedPlatforms()),
                new Paragraph("Providers: " + pi4JService.getProviders()),
                new Paragraph("Registry: " + pi4JService.getRegistry())
        );
        setSizeFull();
        getStyle().set("text-align", "left");
    }
}
