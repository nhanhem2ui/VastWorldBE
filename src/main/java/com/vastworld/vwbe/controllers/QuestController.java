package com.vastworld.vwbe.controllers;


import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.quests.AcceptQuestRequest;
import com.vastworld.vwbe.dto.quests.GetAvailableQuestsResponse;
import com.vastworld.vwbe.entites.Quest;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.QuestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.vastworld.vwbe.common.Common.resolveStatus;

@RestController
@RequestMapping("/api/quests")
@RateLimit(limit = 30)
@PreAuthorize("isAuthenticated()")
public class QuestController {
    private final QuestService questService;
    public QuestController(QuestService questService) {
        this.questService = questService;
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<ServiceResult<List<GetAvailableQuestsResponse>>> getAvailableQuests(@PathVariable UUID playerId) {
        var result = questService.getAvailableQuests(playerId);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @PostMapping("/accept")
    public ResponseEntity<ServiceResult<Void>> acceptQuest(@Valid @RequestBody AcceptQuestRequest request) {
        var result = questService.acceptQuest(request);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }
}
