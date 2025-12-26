package com.limport.tms.domain.event;

/**
 * Constants for event types to avoid hardcoded strings and tight coupling.
 */
public final class EventTypes {

    // Domain Events - TMS Transport
    public static final class Transport {
        public static final String PREFIX = "TMS.Transport";

        public static final class Request {
            public static final String PREFIX = Transport.PREFIX + ".Request";
            public static final String CREATED = PREFIX + ".Created";
            public static final String UPDATED = PREFIX + ".Updated";
            public static final String CANCELLED = PREFIX + ".Cancelled";
            public static final String COMPLETED = PREFIX + ".Completed";
            public static final String ASSIGNED = PREFIX + ".Assigned";
        }

        public static final class Route {
            public static final String PREFIX = Transport.PREFIX + ".Route";
            public static final String OPTIMIZED = PREFIX + ".Optimized";
        }
    }

    // External Events - Provider Management Service
    public static final class Provider {
        public static final String PREFIX = "PMS.Provider";

        public static final String MATCHED = PREFIX + ".Matched";
        public static final String ASSIGNMENT_RESPONSE = PREFIX + ".AssignmentResponse";
        public static final String CAPACITY_CHANGED = PREFIX + ".CapacityChanged";
    }

    private EventTypes() {
        // Utility class
    }
}