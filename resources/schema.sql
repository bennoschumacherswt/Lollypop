-- ============================================================
-- Matsecom Subscriber Management System — Schema
-- Run once to initialise the database.
-- ============================================================

CREATE DATABASE IF NOT EXISTS lollypop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE matsecom;

-- DROP TABLE IF EXISTS invoice;
DROP TABLE IF EXISTS session;
DROP TABLE IF EXISTS subscriber;

-- ----------------------------------------------------------------
-- 3. SUBSCRIBER
-- ----------------------------------------------------------------
CREATE TABLE subscriber (
                            id                      BIGINT          PRIMARY KEY AUTO_INCREMENT,
                            firstname                VARCHAR(100)    NOT NULL,
                            lastname                 VARCHAR(100)    NOT NULL,
                            imsi_mcc                BIGINT        NOT NULL DEFAULT '262',
                            imsi_mnc                BIGINT        NOT NULL DEFAULT '42',
                            imsi_msin               BIGINT        NOT NULL,
                            terminal_type        ENUM('PhairPhone','Pear_aphone_4s','Samsung_S42plus')             NOT NULL,
                            subscription_type    ENUM('GreenMobilS','GreenMobilM','GreenMobilL')             NOT NULL,
--     data_volume    DECIMAL(12,4)   NOT NULL DEFAULT 0.0000,
--     used_voice_minutes      INT             NOT NULL DEFAULT 0,
                            remaining_data_mb       DECIMAL(12,4),
--     remaining_free_minutes  INT             NOT NULL DEFAULT 0,
--     created_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at              TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                            CONSTRAINT uq_imsi UNIQUE (imsi_mcc, imsi_mnc, imsi_msin),
);

-- ----------------------------------------------------------------
-- 4. SESSION
-- ----------------------------------------------------------------
CREATE TABLE session (
                         id                      BIGINT          PRIMARY KEY AUTO_INCREMENT,
                         subscriber_id           BIGINT          NOT NULL,
                         service_type            ENUM('Voice_call','Browsing_and_social_networking','App_download','Adaptive_HD_video') NOT NULL,
--     ran_technology          ENUM('2G','3G','4G') NOT NULL,
                         duration_minutes        INT,
--     voice_charge_eur        DECIMAL(10,4),
--     signal_strength         ENUM('GOOD','MEDIUM','LOW','NA'),
--     required_data_rate_mbit DECIMAL(10,2),
--     achieved_data_rate_mbit DECIMAL(10,2),
--     used_data_volume_mb     DECIMAL(12,4),
--     data_charge_eur         DECIMAL(10,4),
--     total_charge_eur        DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,
--     session_success         TINYINT(1)      NOT NULL DEFAULT 1,
--     session_timestamp       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_session_sub FOREIGN KEY (subscriber_id) REFERENCES subscriber(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- 5. INVOICE
-- ----------------------------------------------------------------
-- CREATE TABLE invoice (
--     id                      BIGINT          PRIMARY KEY AUTO_INCREMENT,
--     subscriber_id           BIGINT          NOT NULL,
--     invoice_date            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     total_data_volume_mb    DECIMAL(12,4)   NOT NULL DEFAULT 0.0000,
--     total_voice_minutes     INT             NOT NULL DEFAULT 0,
--     basic_fee_eur           DECIMAL(10,2)   NOT NULL,
--     voice_charge_eur        DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,
--     data_charge_eur         DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,
--     total_charges_eur       DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,
--
--     CONSTRAINT fk_invoice_sub FOREIGN KEY (subscriber_id) REFERENCES subscriber(id) ON DELETE CASCADE
-- );

