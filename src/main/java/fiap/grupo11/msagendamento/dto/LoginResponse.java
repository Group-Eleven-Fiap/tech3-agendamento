package fiap.grupo11.msagendamento.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
}