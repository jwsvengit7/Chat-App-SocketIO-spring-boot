package com.isds.messenging_system.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import java.util.Collections;
import java.util.Map;
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(@NotNull ServerHttpRequest request,
                                   @NotNull ServerHttpResponse response,
                                   @NotNull WebSocketHandler wsHandler,
                                   @NotNull Map<String, Object> attributes) throws Exception {
        ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
        String ticketId = servletRequest.getServletRequest().getParameter("ticketId");
        String userId = servletRequest.getServletRequest().getParameter("userId");
        attributes.put("userId", userId != null ? userId : "UnknownUser");
        attributes.put("ticketId", ticketId != null ? userId : "UnknownUserAdmin");
        attributes.put("principal", new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList()));

        LOGGER.info("userId {}",userId);
        LOGGER.info("ticketId {}",ticketId);
        return true;

        

    }


    @Override
    public void afterHandshake(@NotNull ServerHttpRequest request,@NotNull ServerHttpResponse response,@NotNull WebSocketHandler wsHandler, Exception exception) {

    }

}
