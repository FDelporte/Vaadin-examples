package be.webtechie.vaadin.pi4j.service;

import com.sun.management.OperatingSystemMXBean;
import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

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

    public SystemInformationService(TaskScheduler taskScheduler) {
        memoryMXBean = ManagementFactory.getMemoryMXBean();
        platformMXBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        sessions = new ConcurrentLinkedQueue<>();
        scheduledFuture = taskScheduler.scheduleAtFixedRate(() -> {
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
        }, Instant.now().plusSeconds(4), Duration.ofSeconds(20));
    }

    @PreDestroy
    private void destroy() {
        scheduledFuture.cancel(true);
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

    public int getLastSessionSizeGuestimate() {
        return lastSessionSizeGuestimate;
    }
}
