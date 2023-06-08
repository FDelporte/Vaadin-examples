package be.webtechie.vaadin.pi4j.views;


import be.webtechie.vaadin.pi4j.components.appnav.AppNav;
import be.webtechie.vaadin.pi4j.components.appnav.AppNavItem;
import be.webtechie.vaadin.pi4j.service.SystemInformationService;
import be.webtechie.vaadin.pi4j.views.about.AboutPi4JView;
import be.webtechie.vaadin.pi4j.views.about.AboutSystemView;
import be.webtechie.vaadin.pi4j.views.electronics.*;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.lineawesome.LineAwesomeIcon;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final SystemInformationService systemInformationService;
    private H2 viewTitle;

    public MainLayout(SystemInformationService systemInformationService) {
        this.systemInformationService = systemInformationService;
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        systemInformationService.register();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        systemInformationService.deRegister();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Menu toggle");

        viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        H1 appName = new H1("My App");
        appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private AppNav createNavigation() {
        // AppNav is not yet an official component.
        // For documentation, visit https://github.com/vaadin/vcf-nav#readme
        AppNav nav = new AppNav();
        nav.addItem(new AppNavItem("Pi4J Info", AboutPi4JView.class, LineAwesomeIcon.JAVA.create()));
        nav.addItem(new AppNavItem("System Info", AboutSystemView.class, LineAwesomeIcon.JAVA.create()));
        nav.addItem(new AppNavItem("LED", LEDView.class, LineAwesomeIcon.LIGHTBULB_SOLID.create()));
        nav.addItem(new AppNavItem("Touch", TouchView.class, LineAwesomeIcon.POWER_OFF_SOLID.create()));
        nav.addItem(new AppNavItem("LCD Display", LcdDisplayView.class, LineAwesomeIcon.TABLE_SOLID.create()));
        nav.addItem(new AppNavItem("LED Matrix", MatrixView.class, LineAwesomeIcon.TABLE_SOLID.create()));
        nav.addItem(new AppNavItem("Seven Segments", SevenSegmentView.class, LineAwesomeIcon.TABLE_SOLID.create()));
        return nav;
    }

    private Footer createFooter() {
        Footer layout = new Footer();

        return layout;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
        return title == null ? "" : title.value();
    }
}
