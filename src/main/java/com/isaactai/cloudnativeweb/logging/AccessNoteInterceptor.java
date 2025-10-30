package com.isaactai.cloudnativeweb.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author tisaac
 */
@Component
public class AccessNoteInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (handler instanceof HandlerMethod hm) {
            AccessNote note = hm.getMethodAnnotation(AccessNote.class);
            if (note != null) {
                // Persist notes to request so AccessLogFilter can read later
                request.setAttribute("access.label",       note.label());
                request.setAttribute("access.success",     note.success());
                request.setAttribute("access.clientWarn",  note.clientWarn());
                request.setAttribute("access.serverError", note.serverError());
            } else {
                // Reasonable defaults (optional)
                request.setAttribute("access.label", hm.getMethod().getName());
            }
        }
        return true;
    }
}
