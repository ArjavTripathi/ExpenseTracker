package com.chat.aj.expensetracker.Websockets;

import com.chat.aj.expensetracker.Groups.GroupService;
import com.chat.aj.expensetracker.common.Entities.Group;
import com.chat.aj.expensetracker.common.Entities.User;
import com.chat.aj.expensetracker.security.JWT.JWTService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StompAuthChannelInterceptorTest {
    @Mock
    private JWTService jwtService;
    @Mock
    private UserDetailsService userDetailsService;
    @Mock
    private GroupService groupService;
    @InjectMocks
    private StompAuthChannelInterceptor interceptor;

    @Test
    void connectRejectsAMissingToken() {
        Message<byte[]> message = stomp(StompCommand.CONNECT);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void subscribeRequiresMembership() {
        User outsider = new User();
        outsider.setId(2L);
        outsider.setEmail("bob@example.com");
        Group group = new Group();
        group.setGroupId(5L);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/group/5");
        accessor.setUser(new UsernamePasswordAuthenticationToken("bob@example.com", null, List.of()));
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(groupService.findGroupById(5L)).thenReturn(group);
        when(groupService.findUserByEmail("bob@example.com")).thenReturn(outsider);
        when(groupService.isGroupMember(group, outsider)).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class);
        verify(groupService).isGroupMember(group, outsider);
    }

    @Test
    void subscribeRejectsDestinationsOutsideAGroupTopic() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/everything");
        accessor.setUser(new UsernamePasswordAuthenticationToken("bob@example.com", null, List.of()));
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, null))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static Message<byte[]> stomp(StompCommand command) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
