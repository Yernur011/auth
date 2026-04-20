package kz.dev.api.adapter.rest.dto;

import kz.dev.core.model.TokenPair;

public record TokenPairResponse(String accessToken, String refreshToken) {
    public static TokenPairResponse from(TokenPair pair) {
        return new TokenPairResponse(pair.accessToken().getValue(), pair.refreshToken().getValue());
    }
}
