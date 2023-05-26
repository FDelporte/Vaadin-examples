package be.webtechie.vaadin.pi4j.service;

import com.sun.management.OperatingSystemMXBean;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Based on
 * https://github.com/mstahv/VaadinTetris/blob/master/src/main/java/org/vaadin/example/SystemInformationService.java
 */
@Service
public class SystemInformationService {

    private final OperatingSystemMXBean platformMXBean;
    private final MemoryMXBean memoryMXBean;
    private final Queue<String> sessions;
    MemoryUsage heapMemoryUsage;
    MemoryUsage startHeapMemoryUsage;
    int startHeapUsers;
    int lastSessionSizeGuestimate;
    int lastSessionSizeGuestimateUsers;
    ScheduledFuture<?> scheduledFuture;

    public SystemInformationService(ScheduledExecutorService executorService) {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        platformMXBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        sessions = new ConcurrentLinkedQueue<>();
        scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            memoryMXBean.gc();
            heapMemoryUsage = heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            if (startHeapMemoryUsage == null && sessions.size() > 5) {
                startHeapMemoryUsage = heapMemoryUsage;
                startHeapUsers = sessions.size();
            }
            if (sessions.size() > 10 && sessions.size() > lastSessionSizeGuestimateUsers) {
                lastSessionSizeGuestimateUsers = sessions.size();
                // (a + xb = total1) || * -1
                // (a + yb = total2)
                //-xb + yb = total2 - total1
                // b = (total2 -total1)/(y-x)
                lastSessionSizeGuestimate = (int) ((heapMemoryUsage.getUsed() - startHeapMemoryUsage.getUsed()) / (sessions.size() - startHeapUsers));
            }
        }, 4, 20, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destroy() {
        scheduledFuture.cancel(true);
    }

    public void register() {
        sessions.add(VaadinSession.getCurrent().getSession().getId());
    }

    public void deRegister() {
        sessions.remove(VaadinSession.getCurrent().getSession().getId());
    }

    public int getNumberOfSessions() {
        return sessions.size();
    }

    public double getSystemLoad() {
        return platformMXBean.getSystemLoadAverage();
    }

    public MemoryUsage getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    public MemoryUsage getStartHeapMemoryUsage() {
        return startHeapMemoryUsage;
    }

    public int getLastSessionSizeGuestimate() {
        return lastSessionSizeGuestimate;
    }
}
