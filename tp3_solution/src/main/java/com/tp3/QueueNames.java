package com.tp3;

public final class QueueNames {
    public static final String WRITE_EXCHANGE = "replication_fanout";
    public static final String READ_LAST_EXCHANGE = "read_request_fanout";
    public static final String READ_ALL_EXCHANGE = "read_all_fanout";
    public static final String READ_LAST_RESPONSE_QUEUE = "client_reader_responses";
    public static final String READ_ALL_RESPONSE_QUEUE = "reader_v2_responses";

    private QueueNames() {}
}
