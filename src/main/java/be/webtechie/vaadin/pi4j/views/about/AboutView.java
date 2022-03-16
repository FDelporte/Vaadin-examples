package be.webtechie.vaadin.pi4j.views.about;

import be.webtechie.vaadin.pi4j.service.Pi4JService;
import be.webtechie.vaadin.pi4j.views.MainLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
public class AboutView extends VerticalLayout {

    public AboutView(@Autowired Pi4JService pi4JService) {
        setSpacing(false);

        add(new H2("Java"));
        add(new Paragraph("Version: " + System.getProperty("java.version")));
        add(new H2("Pi4J"));
        add(new Paragraph("Default platform: " + pi4JService.getDefaultPlatform()));
        add(new Paragraph("Loaded platforms: " + pi4JService.getLoadedPlatforms()));
        add(new Paragraph("Providers: " + pi4JService.getProviders()));
        add(new Paragraph("Registry: " + pi4JService.getRegistry()));

        setSizeFull();
        getStyle().set("text-align", "left");
    }
}
