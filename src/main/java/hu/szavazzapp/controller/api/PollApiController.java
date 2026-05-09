package hu.szavazzapp.controller.api;

import java.util.List;

import hu.szavazzapp.dto.ApiResponse;
import hu.szavazzapp.dto.CreatePollRequest;
import hu.szavazzapp.dto.VoteRequest;
import hu.szavazzapp.service.PollCommandService;
import hu.szavazzapp.service.PollQueryService;
import hu.szavazzapp.service.PollQueryService.PollCardView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/polls")
@Tag(name = "Szavazások", description = "Szavazások lekérdezése, létrehozása, törlése és szavazás")
public class PollApiController {

    private final PollQueryService pollQueryService;
    private final PollCommandService pollCommandService;

    public PollApiController(
            PollQueryService pollQueryService,
            PollCommandService pollCommandService) {
        this.pollQueryService = pollQueryService;
        this.pollCommandService = pollCommandService;
    }

    @GetMapping("/top")
    @Operation(summary = "Top szavazások lekérdezése")
    public List<PollCardView> getTopPolls(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return pollQueryService.findTopPolls(limit);
    }

    @GetMapping("/other")
    @Operation(summary = "Más felhasználók szavazásai")
    public List<PollCardView> getOtherUserPolls(Authentication authentication) {
        return pollQueryService.findOtherUserPolls(getUsername(authentication));
    }

    @GetMapping("/own")
    @Operation(summary = "Saját szavazások lekérdezése")
    public List<PollCardView> getOwnPolls(Authentication authentication) {
        return pollQueryService.findOwnPolls(getUsername(authentication));
    }

    @PostMapping
    @Operation(summary = "Új saját szavazás létrehozása")
    public ResponseEntity<ApiResponse> createPoll(
            @Valid @RequestBody CreatePollRequest request,
            Authentication authentication) {
        Long pollId = pollCommandService.createPoll(getUsername(authentication), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Szavazás létrehozva. Azonosító: " + pollId));
    }

    @PostMapping("/{pollId}/vote")
    @Operation(summary = "Szavazás leadása")
    public ApiResponse vote(
            @PathVariable Long pollId,
            @Valid @RequestBody VoteRequest request,
            Authentication authentication) {
        pollCommandService.vote(getUsername(authentication), pollId, request.optionId());

        return new ApiResponse(true, "Szavazat rögzítve.");
    }

    @DeleteMapping("/{pollId}")
    @Operation(summary = "Saját szavazás törlése")
    public ApiResponse deletePoll(
            @PathVariable Long pollId,
            Authentication authentication) {
        pollCommandService.deletePoll(
                getUsername(authentication),
                hasRole(authentication, "ADMIN"),
                pollId);

        return new ApiResponse(true, "Szavazás törölve.");
    }

    private String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "ismeretlen";
        }

        return authentication.getName();
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }

        String authorityName = "ROLE_" + role;

        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authorityName.equals(authority.getAuthority()));
    }
}