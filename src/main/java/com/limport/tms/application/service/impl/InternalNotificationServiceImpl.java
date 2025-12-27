package com.limport.tms.application.service.impl;

import com.limport.tms.application.service.interfaces.IInternalNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementation of internal notification service.
 * Currently logs notifications - in a real implementation, this would
 * send emails, Slack messages, or other notification types.
 */
@Service
public class InternalNotificationServiceImpl implements IInternalNotificationService {

    private static final Logger log = LoggerFactory.getLogger(InternalNotificationServiceImpl.class);

    @Override
    public void notifyOperationsTeam(String message, Object eventData) {
        log.info("Notifying operations team: {} - Event data: {}", message, eventData);

        // TODO: Send notification to operations team
        // Example: Send email, Slack message, or push notification

        log.debug("Operations team notification sent for: {}", message);
    }

    @Override
    public void notifyProviderTeam(String message, Object eventData) {
        log.info("Notifying provider team: {} - Event data: {}", message, eventData);

        // TODO: Send notification to provider team
        // Example: Send email or system notification to provider dashboard

        log.debug("Provider team notification sent for: {}", message);
    }
}