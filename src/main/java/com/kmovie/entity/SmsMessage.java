package com.kmovie.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A queued outbound SMS. Like EmailMessage, this app only writes pending
 * rows; a separate worker process dispatches them via Twilio/Africa's
 * Talking and marks isSent/dateSent.
 */
@Entity
@Table(name = "sms_messages")
@Getter
@Setter
public class SmsMessage extends BaseEntity {

    @Column(name = "receiver", nullable = false)
    private String receiver;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "is_sent", nullable = false)
    private boolean isSent = false;

    @Column(name = "date_sent")
    private LocalDateTime dateSent;
}
