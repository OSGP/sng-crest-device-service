CREATE TABLE firmware
(
    id                   uuid         not null,
    name                 varchar(255) not null,
    version              varchar(255) not null,
    previous_firmware_id uuid          null,
    PRIMARY KEY (id),
    CONSTRAINT fk_previous_firmware
        FOREIGN KEY (previous_firmware_id)
            REFERENCES firmware (id)
);

CREATE TABLE firmware_packet
(
    firmware_id   uuid not null,
    packet_number int  not null,
    packet        varchar(1024),
    PRIMARY KEY (firmware_id, packet_number),
    CONSTRAINT fk_firmware
        FOREIGN KEY (firmware_id)
            REFERENCES firmware (id)
)
