package hu.szavazzapp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record VoteRequest(

                @NotNull(message = "Válaszlehetőség kiválasztása kötelező.") @Positive(message = "Érvénytelen válaszlehetőség azonosító.") Long optionId) {
}