DROP TABLE IF EXISTS outbox_events;

create table outbox_events(
    id BIGINT auto_increment primary key ,
    aggregate_id bigint not null ,
    aggregate_type varchar(50) not null ,
    event_type varchar(50) not null ,
    payload text not null ,
    created_at timestamp not null DEFAULT CURRENT_TIMESTAMP,
    processed_at timestamp,
    INDEX idx_outbox_unprocessed (processed_at, created_at)
) ENGINE=InnoDB;