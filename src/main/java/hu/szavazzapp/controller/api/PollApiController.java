package hu.szavazzapp.controller.api;

import java.util.List;

import hu.szavazzapp.service.PollQueryService;
import hu.szavazzapp.service.PollQueryService.PollCardView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/polls")
@Tag(name = "Szavazások", description = "Szavazások lekérdezése és eredmények megjelenítése")
public class PollApiController {

    private final PollQueryService pollQueryService;

    public PollApiController(PollQueryService pollQueryService) {
        this.pollQueryService = pollQueryService;
    }

    @GetMapping("/top")
    @Operation(summary = "Top szavazások lekérdezése", description = "Az aktív szavazásokat összesített szavazatszám alapján csökkenő sorrendben adja vissza.")
    public List<PollCardView> getTopPolls(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit) {
        return pollQueryService.findTopPolls(limit);
    }

    @GetMapping("/other")
    @Operation(summary = "Más felhasználók szavazásai", description = "Azokat az aktív szavazásokat adja vissza, amelyeket nem a bejelentkezett felhasználó hozott létre.")
    public List<PollCardView> getOtherUserPolls(Authentication authentication) {
        String username = getUsername(authentication);
        return pollQueryService.findOtherUserPolls(username);
    }

    @GetMapping("/own")
    @Operation(summary = "Saját szavazások lekérdezése", description = "A bejelentkezett felhasználó saját aktív szavazásait adja vissza.")
    public List<PollCardView> getOwnPolls(Authentication authentication) {
        String username = getUsername(authentication);
        return pollQueryService.findOwnPolls(username);
    }

    private String getUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "ismeretlen";
        }

        return authentication.getName();
    }
}