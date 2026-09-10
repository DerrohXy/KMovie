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

    @Column(name = "receiver", nullable = false)
    private String receiver;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Lob
    @Column(name = "message", nullable = false)
    private String message;

    /** Comma-separated list of File ids to attach, if any. */
    @Column(name = "file_ids", length = 2000)
    private String fileIds;

    @Column(name = "is_html", nullable = false)
    private boolean isHtml = false;

    @Column(name = "is_sent", nullable = false)
    private boolean isSent = false;

    @Column(name = "date_sent")
    private LocalDateTime dateSent;
}
