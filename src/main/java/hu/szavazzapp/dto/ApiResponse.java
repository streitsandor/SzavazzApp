package hu.szavazzapp.dto;

public record ApiResponse(
                boolean success,
                String message) {
}