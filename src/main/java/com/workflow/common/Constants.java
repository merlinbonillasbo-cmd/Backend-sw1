package com.workflow.common;

public final class Constants {

    private Constants() {}

    public static final String API_BASE_PATH = "/api/v1";

    // JWT
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    // Pagination
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;

    // Workflow node types
    public static final String NODE_TYPE_START = "START";
    public static final String NODE_TYPE_END = "END";
    public static final String NODE_TYPE_TASK = "TASK";
    public static final String NODE_TYPE_GATEWAY = "GATEWAY";

    // Instance states
    public static final String STATE_PENDING = "PENDING";
    public static final String STATE_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATE_COMPLETED = "COMPLETED";
    public static final String STATE_REJECTED = "REJECTED";
    public static final String STATE_CANCELLED = "CANCELLED";

}
