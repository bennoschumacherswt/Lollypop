package com.lollypop;

import com.lollypop.dao.SubscriberDAO;
import com.lollypop.dao.UserSessionDAO;
import com.lollypop.dao.impl.SubscriberDAOImpl;
import com.lollypop.dao.impl.UserSessionDAOImpl;
import com.lollypop.service.SubscriberService;
import com.lollypop.service.UserSessionService;
import com.lollypop.service.impl.SubscriberServiceImpl;
import com.lollypop.service.impl.UserSessionServiceImpl;

/**
 * Manual dependency-injection wiring.
 *
 * All {@code new} calls live here — nowhere else in the application creates
 * DAOs or Services directly. Swap an implementation by changing one line here.
 *
 * Usage:
 * <pre>
 *   ServiceFactory sf = new ServiceFactory();
 *   SubscriberService svc = sf.getSubscriberService();
 * </pre>
 */
public class ServiceFactory {

    private final SubscriberDAO    subscriberDAO;
    private final UserSessionDAO   userSessionDAO;
    private final SubscriberService  subscriberService;
    private final UserSessionService userSessionService;

    public ServiceFactory() {
        this.subscriberDAO    = new SubscriberDAOImpl();
        this.userSessionDAO   = new UserSessionDAOImpl();
        this.subscriberService  = new SubscriberServiceImpl(subscriberDAO, userSessionDAO);
        this.userSessionService = new UserSessionServiceImpl(userSessionDAO);
    }

    public SubscriberService   getSubscriberService()   { return subscriberService; }
    public UserSessionService  getUserSessionService()  { return userSessionService; }

    // Expose DAOs in case a caller needs direct access (e.g. integration tests)
    public SubscriberDAO  getSubscriberDAO()  { return subscriberDAO; }
    public UserSessionDAO getUserSessionDAO() { return userSessionDAO; }
}
