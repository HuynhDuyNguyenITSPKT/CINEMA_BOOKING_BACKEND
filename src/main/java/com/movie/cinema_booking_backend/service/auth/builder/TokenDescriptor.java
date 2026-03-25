package com.movie.cinema_booking_backend.service.auth.builder;

public class TokenDescriptor {

    private final long durationSeconds;
    private final String type;

    private TokenDescriptor(Builder builder) {
        this.durationSeconds = builder.durationSeconds;
        this.type = builder.type;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public String getType() {
        return type;
    }

    public static class Builder {
        private long durationSeconds;
        private String type;

        public Builder durationSeconds(long durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public TokenDescriptor build() {
            return new TokenDescriptor(this);
        }
    }
}
