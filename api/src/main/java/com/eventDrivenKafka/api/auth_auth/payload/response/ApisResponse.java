package com.eventDrivenKafka.api.auth_auth.payload.response;

public class ApisResponse {
    private boolean success;
    private String message;

    public ApisResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
