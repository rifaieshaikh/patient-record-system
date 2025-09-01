package com.prs.worker.utils;

public class ErrorUtils {

    public static boolean isClientError(Throwable t) {
        return t instanceof feign.FeignException && ((feign.FeignException) t).status() >= 400
                && ((feign.FeignException) t).status() < 500;
    }

    public static String messageFrom(Throwable t) {
        return (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
    }
}
