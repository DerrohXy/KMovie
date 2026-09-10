BEGIN;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(255) NOT NULL,
    email VARCHAR(1000) NOT NULL,
    password TEXT NOT NULL,
    phone_number VARCHAR(255),
    is_admin BOOLEAN DEFAULT FALSE,
    subscription VARCHAR(50) NOT NULL,
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_phone_number_verified BOOLEAN DEFAULT FALSE,
    profile_picture_file_id UUID,
    is_suspended BOOLEAN DEFAULT FALSE,
    -- base model fields
    id UUID PRIMARY KEY,
    date_created TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    date_deleted TIMESTAMPTZ,
    date_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    -- constraints
    CONSTRAINT user_profile_picture FOREIGN KEY (profile_picture_file_id) REFERENCES files(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS titles (
    name VARCHAR(255) NOT NULL,
    parent_title_id UUID,
    genre VARCHAR(1000),
    category VARCHAR(50),
    -- base model fields
    id UUID PRIMARY KEY,
    date_created TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    date_deleted TIMESTAMPTZ,
    date_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    -- constraints
    CONSTRAINT title_parent_title FOREIGN KEY (parent_title_id) REFERENCES titles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS videos (
    title_id UUID NOT NULL,
    file_id UUID NOT NULL,
    name VARCHAR(1000) NOT NULL,
    description VARCHAR(1000),
    duration INTEGER NOT NULL,
    -- base model fields
    id UUID PRIMARY KEY,
    date_created TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    date_deleted TIMESTAMPTZ,
    date_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    -- constraints
    CONSTRAINT video_title FOREIGN KEY (title_id) REFERENCES titles(id) ON DELETE CASCADE,
    CONSTRAINT video_file FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS video_plays (
    user_id UUID NOT NULL,
    video_id UUID NOT NULL,
    title_id UUID NOT NULL,
    start_play_time TIMESTAMPTZ NOT NULL,
    end_play_time TIMESTAMPTZ,
    playback_start_point INTEGER,
    playback_end_point INTEGER,
    -- base model fields
    id UUID PRIMARY KEY,
    date_created TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    date_deleted TIMESTAMPTZ,
    date_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    -- constraints
    CONSTRAINT video_play_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT video_play_video FOREIGN KEY (video_id) REFERENCES videos(id) ON DELETE CASCADE,
    CONSTRAINT video_play_title FOREIGN KEY (title_id) REFERENCES titles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS email_messages (
    receiver VARCHAR(255) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    file_ids VARCHAR(2000),
    is_html BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    date_sent TIMESTAMPTZ,
    -- base model fields
    id UUID PRIMARY KEY,
    date_created TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    date_deleted TIMESTAMPTZ,
    date_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sms_messages (
    receiver VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    is_sent BOOLEAN DEFAULT FALSE,
    date_sent TIMESTAMPTZ,
    -- base model fields
    id UUID PRIMARY KEY,
    date_created TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    date_deleted TIMESTAMPTZ,
    date_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS files (
    name VARCHAR(1000) NOT NULL,
    size INTEGER NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    -- base model fields
    id UUID PRIMARY KEY,
    date_created TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    date_deleted TIMESTAMPTZ,
    date_updated TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX users_email_index ON users (email);
CREATE INDEX users_phone_number_index ON users (phone_number);
CREATE UNIQUE INDEX users_username ON users (username);

COMMIT;