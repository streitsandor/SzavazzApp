package hu.szavazzapp.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdatePollRequest(

        @NotBlank(message = "A cím megadása kötelező.") @Size(max = 180, message = "A cím legfeljebb 180 karakter lehet.") String title,

        @NotBlank(message = "A leírás megadása kötelező.") @Size(max = 3000, message = "A leírás legfeljebb 3000 karakter lehet.") String description,

        @NotEmpty(message = "Legalább egy témát ki kell választani.") @Size(max = 10, message = "Legfeljebb 10 témát lehet kiválasztani.") List<@NotNull(message = "A téma azonosító nem lehet üres.") @Positive(message = "Érvénytelen téma azonosító.") Long> topicIds,

        @NotEmpty(message = "Legalább két válaszlehetőség szükséges.") @Size(min = 2, max = 10, message = "Legalább 2, legfeljebb 10 válaszlehetőség adható meg.") List<@NotBlank(message = "A válaszlehetőség nem lehet üres.") @Size(max = 180, message = "Egy válaszlehetőség legfeljebb 180 karakter lehet.") String> options) {
}