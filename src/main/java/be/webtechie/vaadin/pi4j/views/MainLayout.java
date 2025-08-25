package be.webtechie.vaadin.pi4j.views;

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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * The main view is a top-level placeholder for other views.
 */
public class MainLayout extends AppLayout {

    private final SystemInformationService systemInformationService;
    private H2 viewTitle;

    public MainLayout(SystemInformationService systemInformationService) {
        this.systemInformationService = systemInformationService;

        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1("Pi4J Vaadin Demo");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        SideNav nav = getSideNav();

        Scroller scroller = new Scroller(nav);
        scroller.setClassName(LumoUtility.Padding.SMALL);

        addToDrawer(scroller);
        addToNavbar(toggle, title);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        systemInformationService.register();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        systemInformationService.deRegister();
    }

    private SideNav getSideNav() {
        var nav = new SideNav();
        nav.addItem(new SideNavItem("Pi4J Info", AboutPi4JView.class, VaadinIcon.INFO.create()));
        nav.addItem(new SideNavItem("System Info", AboutSystemView.class, VaadinIcon.SERVER.create()));
        nav.addItem(new SideNavItem("LED", LEDView.class, VaadinIcon.LIGHTBULB.create()));
        nav.addItem(new SideNavItem("Buzzer", BuzzerView.class, VaadinIcon.VOLUME_UP.create()));
        nav.addItem(new SideNavItem("Touch", TouchView.class, VaadinIcon.TOUCH.create()));
        nav.addItem(new SideNavItem("LCD Display", LcdDisplayView.class, VaadinIcon.VIEWPORT.create()));
        nav.addItem(new SideNavItem("LED Matrix", MatrixView.class, VaadinIcon.GRID_SMALL.create()));
        nav.addItem(new SideNavItem("Seven Segments", SevenSegmentView.class, VaadinIcon.TABLE.create()));
        return nav;
    }

    private Footer createFooter() {
        return new Footer();
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
