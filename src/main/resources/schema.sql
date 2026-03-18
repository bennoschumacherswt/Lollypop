-- ============================================================
-- Matsecom Subscriber Management System — Schema
-- FIX 1: Database name was 'lollypop' in CREATE but 'matsecom' in USE → unified to 'lollypop'
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
    -- FIX 2: MCC for Germany is 262, not 49. Changed type to SMALLINT (max value 999).
    imsi_mcc            SMALLINT        NOT NULL DEFAULT 262,
    imsi_mnc            SMALLINT        NOT NULL DEFAULT 42,
    imsi_msin           BIGINT          NOT NULL,

    -- Enum values match Java TerminalType / SubscriptionType enum names exactly
    terminal_type       ENUM('PhairPhone','Pear_aphone_4s','Samsung_S42plus')  NOT NULL,
    subscription_type   ENUM('GreenMobilS','GreenMobilM','GreenMobilL')        NOT NULL,

    -- FIX 3: Was NULLable with no default. Must be NOT NULL; default is set from
    --        the subscription type at INSERT time (application layer responsibility).
    remaining_data_mb   DECIMAL(12,4)   NOT NULL DEFAULT 0.0000,

    -- IMSI must be globally unique per network
    CONSTRAINT uq_imsi UNIQUE (imsi_mcc, imsi_mnc, imsi_msin)
    -- FIX 4: Removed trailing comma after UNIQUE constraint (caused SQL syntax error)
);

-- ----------------------------------------------------------------
-- SESSION
-- ----------------------------------------------------------------
CREATE TABLE session (
    id                      BIGINT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
    subscriber_id           BIGINT          NOT NULL,

    -- Enum values match Java ServiceType enum names exactly
    service_type            ENUM(
                                'Voice_call',
                                'Browsing_and_social_networking',
                                'App_download',
                                'Adaptive_HD_video'
                            ) NOT NULL,

    -- FIX 5: Renamed duration_minutes → duration_seconds to match Java model (uses seconds).
    --        RFP does not specify seconds vs minutes explicitly; seconds is more precise.
    duration_seconds        INT             NOT NULL,

    -- FIX 6: Uncommented these two columns — required for DAO persistence and invoicing.
    --        Without them the SubscriberServiceImpl cannot persist session results
    --        and generateInvoice() cannot sum charges.
    used_data_volume_mb     DECIMAL(12,4)   NOT NULL DEFAULT 0.0000,
    charges_eur             DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,

    CONSTRAINT fk_session_sub
        FOREIGN KEY (subscriber_id) REFERENCES subscriber(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------
-- INVOICE  (kept commented — generated on-the-fly per RFP, but schema
--           is here ready to enable audit history if required later)
-- ----------------------------------------------------------------
-- CREATE TABLE invoice (
--     id                      BIGINT          NOT NULL PRIMARY KEY AUTO_INCREMENT,
--     subscriber_id           BIGINT          NOT NULL,
--     invoice_date            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     total_data_volume_mb    DECIMAL(12,4)   NOT NULL DEFAULT 0.0000,
--     total_voice_seconds     INT             NOT NULL DEFAULT 0,
--     basic_fee_eur           DECIMAL(10,2)   NOT NULL,
--     voice_charge_eur        DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,
--     data_charge_eur         DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,
--     total_charges_eur       DECIMAL(10,4)   NOT NULL DEFAULT 0.0000,
--     CONSTRAINT fk_invoice_sub FOREIGN KEY (subscriber_id) REFERENCES subscriber(id) ON DELETE CASCADE
-- );
