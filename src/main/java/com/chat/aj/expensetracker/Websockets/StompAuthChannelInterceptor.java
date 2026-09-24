package com.chat.aj.expensetracker.Websockets;

import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.Group;
import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.security.JWT.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private static final Pattern GROUP_TOPIC = Pattern.compile("^/topic/group/(\\d+)$");

    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;
    private final GroupService groupService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String bearer = accessor.getFirstNativeHeader("Authorization");
            String jwt = (bearer != null && bearer.startsWith("Bearer ")) ? bearer.substring(7) : null;

            if (jwt == null || !jwtService.validateToken(jwt)) {
                throw new BadCredentialsException("Invalid or missing token on STOMP CONNECT");
            }

            String username = jwtService.getUserNameFromJwtToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                throw new BadCredentialsException("Unauthenticated STOMP SUBSCRIBE");
            }
            String destination = accessor.getDestination();
            Matcher matcher = destination == null ? null : GROUP_TOPIC.matcher(destination);
            if (matcher == null || !matcher.matches()) {
                throw new AccessDeniedException("Unsupported subscription");
            }
            Long groupId = Long.valueOf(matcher.group(1));
            String email = accessor.getUser().getName();
            Group group = groupService.findGroupById(groupId);
            User user = groupService.findUserByEmail(email);
            if (!groupService.isGroupMember(group, user)) {
                throw new AccessDeniedException("You are not a member of this group");
            }
        }

        return message;
    }
}
