package com.vastworld.vwbe.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseNotificationService {
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    public final long sseTimeout = 600000L;

    public SseEmitter subscribe(UUID playerId) {
        var emitter = new SseEmitter(sseTimeout); //10 minutes

        emitters.computeIfAbsent(playerId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(playerId, emitter));
        emitter.onTimeout(() -> removeEmitter(playerId, emitter));
        emitter.onError(e -> removeEmitter(playerId, emitter));

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("connected!")
            );
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void sendToPlayer(UUID playerId, String eventName, Object payload) {
        var playerEmitters = emitters.get(playerId);
        if (playerEmitters == null || playerEmitters.isEmpty()) {
            return; // player not connected, nothing to push
        }

        for (var emitter : playerEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException e) {
                log.warn("Failed to send SSE to player {}, removing emitter", playerId);
                removeEmitter(playerId, emitter);
            }
        }
    }

    private void removeEmitter(UUID playerId, SseEmitter emitter) {
        var list = emitters.get(playerId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emitters.remove(playerId);
            }
        }
    }
}