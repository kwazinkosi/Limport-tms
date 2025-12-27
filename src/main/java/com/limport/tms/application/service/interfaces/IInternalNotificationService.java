package com.limport.tms.application.service.interfaces;

/**
 * Service for sending internal notifications to operations teams and stakeholders.
 */
public interface IInternalNotificationService {

    /**
     * Notifies the operations team about important events.
     *
     * @param message the notification message
     * @param eventData additional event data for context
     */
    void notifyOperationsTeam(String message, Object eventData);

    /**
     * Notifies the provider team about assignment-related events.
     *
     * @param message the notification message
     * @param eventData additional event data for context
     */
    void notifyProviderTeam(String message, Object eventData);
}