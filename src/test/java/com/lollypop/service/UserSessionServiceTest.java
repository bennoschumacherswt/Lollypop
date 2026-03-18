package com.lollypop.service;

import com.lollypop.dao.UserSessionDAO;
import com.lollypop.model.UserSession;
import com.lollypop.model.enums.ServiceType;
import com.lollypop.service.impl.UserSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock private UserSessionDAO userSessionDAO;

    private UserSessionService service;

    @BeforeEach
    void setUp() {
        service = new UserSessionServiceImpl(userSessionDAO);
    }

    @Test
    @DisplayName("getSessionsForSubscriber delegates to DAO")
    void getSessionsForSubscriber_delegatesToDAO() {
        UserSession session = new UserSession(ServiceType.Voice_call, 60);
        when(userSessionDAO.findBySubscriberId(7)).thenReturn(List.of(session));

        List<UserSession> result = service.getSessionsForSubscriber(7);

        assertEquals(1, result.size());
        verify(userSessionDAO).findBySubscriberId(7);
    }

    @Test
    @DisplayName("clearSessionsForSubscriber delegates to DAO")
    void clearSessions_delegatesToDAO() {
        service.clearSessionsForSubscriber(5);
        verify(userSessionDAO).deleteBySubscriberId(5);
    }

    @Test
    @DisplayName("formatSessions returns helpful message for empty list")
    void formatSessions_empty_returnsMessage() {
        String result = service.formatSessions(Collections.emptyList());
        assertFalse(result.isBlank());
        assertTrue(result.toLowerCase().contains("no session"));
    }

    @Test
    @DisplayName("formatSessions includes all sessions in output")
    void formatSessions_includesAllRows() {
        UserSession s1 = new UserSession(ServiceType.Voice_call, 120);
        s1.setId(1);
        UserSession s2 = new UserSession(ServiceType.App_download, 30);
        s2.setId(2);
        s2.setUsedDataVolumeMb(37.5);
        s2.setChargesEur(0.00);

        String result = service.formatSessions(List.of(s1, s2));

        assertTrue(result.contains("Voice_call"));
        assertTrue(result.contains("App_download"));
        assertTrue(result.contains("120"));
        assertTrue(result.contains("37.50"));
    }
}
