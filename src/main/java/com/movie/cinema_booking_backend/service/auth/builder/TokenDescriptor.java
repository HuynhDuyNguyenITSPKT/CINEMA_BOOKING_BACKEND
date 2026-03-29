package com.movie.cinema_booking_backend.service.auth.builder;

public class TokenDescriptor {

    private final String username;
    private final String scope;
    private final long durationSeconds;
    private final String type;
    private final String token;
    private final String accessToken;
    private final String refreshToken;

    private TokenDescriptor(Builder builder) {
        this.username = builder.username;
        this.scope = builder.scope;
        this.durationSeconds = builder.durationSeconds;
        this.type = builder.type;
        this.token = builder.token;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
    }

    public String getUsername() {
        return username;
    }

    public String getScope() {
        return scope;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public String getType() {
        return type;
    }

    public String getToken() {
        return token;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public static class Builder {
        private String username;
        private String scope;
        private long durationSeconds;
        private String type;
        private String token;
        private String accessToken;
        private String refreshToken;

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder durationSeconds(long durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public TokenDescriptor build() {
            return new TokenDescriptor(this);
        }
    }
}
