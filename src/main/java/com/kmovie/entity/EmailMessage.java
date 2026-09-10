package com.kmovie.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A queued outbound email. The application only ever creates rows here;
 * an external background worker process is responsible for actually
 * sending pending (isSent = false) messages and stamping dateSent.
 */
@Entity
@Table(name = "email_messages")
@Getter
@Setter
public class EmailMessage extends BaseEntity {

    @Column(nullable = false)
    private String receiver;

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(nullable = false)
    private String message;

    /** Comma-separated list of File ids to attach, if any. */
    @Column(length = 2000)
    private String fileIds;

    @Column(nullable = false)
    private boolean isHtml = false;

    @Column(nullable = false)
    private boolean isSent = false;

    private LocalDateTime dateSent;
}
