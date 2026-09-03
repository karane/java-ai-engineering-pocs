package com.pocs.restbackedtools;

public record WeatherToolResult(Object result, JsonRpcError error) {

    public static WeatherToolResult success(Object result) {
        return new WeatherToolResult(result, null);
    }

    public static WeatherToolResult failure(int code, String message) {
        return new WeatherToolResult(null, new JsonRpcError(code, message));
    }

    public boolean isError() {
        return error != null;
    }

    public record JsonRpcError(int code, String message) {
    }
}
