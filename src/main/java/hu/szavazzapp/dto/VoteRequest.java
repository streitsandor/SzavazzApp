package hu.szavazzapp.dto;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(

        @NotNull(message = "Válaszlehetőség kiválasztása kötelező.") Long optionId) {
}