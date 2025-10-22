package be.webtechie.vaadin.pi4j.service;

import com.pi4j.boardinfo.model.BoardInfo;
import com.pi4j.context.Context;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Pi4JService {

    private final Context pi4j;

    public Pi4JService(Context pi4j) {
        this.pi4j = pi4j;
    }

    public BoardInfo getBoardInfo() {
        return pi4j.boardInfo();
    }

    public List<String> getLoadedPlatforms() {
        if (pi4j == null || pi4j.platforms() == null) {
            return List.of("None");
        }
        return pi4j.platforms().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
    }

    public List<String> getProviders() {
        if (pi4j == null || pi4j.providers() == null) {
            return List.of("None");
        }
        return pi4j.providers().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
    }

    public List<String> getRegistry() {
        if (pi4j == null || pi4j.registry() == null) {
            return List.of("None");
        }
        return pi4j.registry().all().entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .toList();
    }
}