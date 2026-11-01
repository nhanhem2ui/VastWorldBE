package com.vastworld.vwbe.controllers;


import com.vastworld.vwbe.dto.ServiceResult;
import com.vastworld.vwbe.dto.quests.GetAcceptedQuestsResponse;
import com.vastworld.vwbe.dto.quests.GetAvailableQuestsResponse;
import com.vastworld.vwbe.security.AuthenticatedUser;
import com.vastworld.vwbe.security.ratelimit.RateLimit;
import com.vastworld.vwbe.services.QuestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

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

    @GetMapping
    public ResponseEntity<ServiceResult<List<GetAvailableQuestsResponse>>> getAvailableQuests(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = questService.getAvailableQuests(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @GetMapping("/accepted")
    public ResponseEntity<ServiceResult<List<GetAcceptedQuestsResponse>>> getAcceptedQuests(Authentication authentication) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = questService.getAcceptedQuests(Objects.requireNonNull(user).playerId());
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @PostMapping("/accept/{questId}")
    public ResponseEntity<ServiceResult<Void>> acceptQuest(Authentication authentication, @PathVariable Integer questId) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = questService.acceptQuest(Objects.requireNonNull(user).playerId(), questId);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }

    @PostMapping("/{questId}/claim")
    public ResponseEntity<ServiceResult<Void>> claimQuest(Authentication authentication, @PathVariable Integer questId) {
        var user = (AuthenticatedUser) authentication.getPrincipal();
        var result = questService.claimQuestReward(Objects.requireNonNull(user).playerId(), questId);
        return ResponseEntity.status(resolveStatus(result, null)).body(result);
    }
}
