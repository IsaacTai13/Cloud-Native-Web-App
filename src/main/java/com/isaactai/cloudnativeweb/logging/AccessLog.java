package com.isaactai.cloudnativeweb.logging;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author tisaac
 */
public class AccessLog {
    private AccessLog() {}

    public static void label(HttpServletRequest req, String label) {
        req.setAttribute("access.label", label);
    }
    public static void success(HttpServletRequest req, String msg) {
        req.setAttribute("access.success", msg);
    }
    public static void clientWarn(HttpServletRequest req, String msg) {
        // mark as expected client error with a custom message
        req.setAttribute("error.expected", true);
        req.setAttribute("error.message", msg);
    }
    public static void serverError(HttpServletRequest req, String msg, Throwable t) {
        req.setAttribute("error.unexpected", true);
        req.setAttribute("error.message", msg);
        req.setAttribute("error.throwable", t);
        if (t != null) req.setAttribute("error.exception", t.getClass().getSimpleName());
    }
}
