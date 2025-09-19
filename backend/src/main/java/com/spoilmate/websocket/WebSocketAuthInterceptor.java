package com.spoilmate.websocket;

import com.spoilmate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        System.out.println("=== WebSocket Interceptor ===");
        System.out.println("Command: " + accessor.getCommand());
        System.out.println("Destination: " + accessor.getDestination());
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("Processing CONNECT command");
            String authToken = accessor.getFirstNativeHeader("Authorization");
            System.out.println("Auth token present: " + (authToken != null));
            
            if (authToken != null && authToken.startsWith("Bearer ")) {
                String jwt = authToken.substring(7);
                String username = jwtService.extractUsername(jwt);
                System.out.println("Extracted username: " + username);
                
                if (username != null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken authenticationToken = 
                                new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                            
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            accessor.setUser(authenticationToken);
                            System.out.println("Authentication set successfully for user: " + username);
                        } else {
                            System.out.println("Token validation failed");
                        }
                    } catch (Exception e) {
                        System.err.println("Error loading user details: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Username extraction failed");
                }
            } else {
                System.out.println("No valid Authorization header found");
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            System.out.println("Processing " + accessor.getCommand() + " command");
            System.out.println("Destination: " + accessor.getDestination());
            System.out.println("User in accessor: " + (accessor.getUser() != null ? accessor.getUser().getName() : "null"));
            
            // 对于SEND和SUBSCRIBE命令，从会话中恢复用户认证信息
            if (accessor.getUser() != null && accessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
                SecurityContextHolder.getContext().setAuthentication((UsernamePasswordAuthenticationToken) accessor.getUser());
                System.out.println("Authentication restored for " + accessor.getCommand());
            } else {
                System.out.println("No user authentication found in accessor");
                // 尝试从Authorization header重新认证
                String authToken = accessor.getFirstNativeHeader("Authorization");
                System.out.println("Trying to re-authenticate with token: " + (authToken != null));
                
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    String jwt = authToken.substring(7);
                    String username = jwtService.extractUsername(jwt);
                    System.out.println("Re-extracted username: " + username);
                    
                    if (username != null) {
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            
                            if (jwtService.isTokenValid(jwt, userDetails)) {
                                UsernamePasswordAuthenticationToken authenticationToken = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                
                                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                                accessor.setUser(authenticationToken);
                                System.out.println("Re-authentication successful for " + accessor.getCommand());
                            } else {
                                System.out.println("Token re-validation failed");
                            }
                        } catch (Exception e) {
                            System.err.println("Error during re-authentication: " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        return message;
    }
}