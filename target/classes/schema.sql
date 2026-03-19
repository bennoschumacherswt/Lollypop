-- ============================================================
-- Matsecom Subscriber Management System — Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS lollypop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE lollypop;

-- ----------------------------------------------------------------
-- Clean slate
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS subscriber;

-- ----------------------------------------------------------------
-- SUBSCRIBER
-- ----------------------------------------------------------------
CREATE TABLE subscriber (
    id                  BIGINT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
    firstname           VARCHAR(100)    NOT NULL,
    lastname            VARCHAR(100)    NOT NULL,

    -- IMSI = MCC (3 digits) + MNC (2 digits) + MSIN (10 digits)
    imsi_mcc            SMALLINT        NOT NULL DEFAULT 262,
    imsi_mnc            SMALLINT        NOT NULL DEFAULT 42,
    imsi_msin           BIGINT          NOT NULL,

    terminal_type       ENUM('PhairPhone','Pear_aphone_4s','Samsung_S42plus')  NOT NULL,
    subscription_type   ENUM('GreenMobilS','GreenMobilM','GreenMobilL')        NOT NULL,

    remaining_data_mb   DECIMAL(12,4)   NOT NULL DEFAULT 0.0000,

    -- IMSI must be globally unique per network
    CONSTRAINT uq_imsi UNIQUE (imsi_mcc, imsi_mnc, imsi_msin)
);

-- ----------------------------------------------------------------
-- SESSION
-- ----------------------------------------------------------------
CREATE TABLE session (
    id                      BIGINT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
    subscriber_id           BIGINT          NOT NULL,
    service_type            ENUM(
                                'Voice_call',
                                'Browsing_and_social_networking',
                                'App_download',
                                'Adaptive_HD_video'
                            ) NOT NULL,
    duration_seconds        INT             NOT NULL,
    used_data_volume_mb     DECIMAL(12,4)   NOT NULL DEFAULT 0.0000,
    charges_eur             DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,

    CONSTRAINT fk_session_sub
        FOREIGN KEY (subscriber_id) REFERENCES subscriber(id) ON DELETE CASCADE
);