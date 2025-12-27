package com.limport.tms.domain.model.enums;

/**
 * High-level lifecycle for a transport request within TMS.
 */
public enum TransportRequestStatus {

    /** Request has been captured but not yet planned/assigned. */
    REQUESTED,

    /** Request has been planned and assigned to a provider/vehicle. */
    PLANNED,

    /** Vehicle has departed and the shipment is in transit. */
    IN_TRANSIT,

    /** Request has been successfully completed. */
    COMPLETED,

    /** Request was cancelled before completion. */
    CANCELLED,

    /** No provider could be assigned after maximum retry attempts. Requires manual intervention. */
    UNASSIGNABLE
};