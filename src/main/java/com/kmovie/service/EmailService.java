package com.kmovie.service;

import com.kmovie.entity.EmailMessage;
import com.kmovie.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Only ever queues messages. Actual sending (SMTP dispatch) is done by a
 * separate external background worker process that polls EmailMessage rows
 * where isSent = false and marks them sent.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailMessageRepository emailMessageRepository;

    public EmailMessage queue(String receiver, String subject, String message, boolean isHtml) {
        return queue(receiver, subject, message, isHtml, null);
    }

    public EmailMessage queue(String receiver, String subject, String message, boolean isHtml, String fileIds) {
        EmailMessage email = new EmailMessage();
        email.setReceiver(receiver);
        email.setSubject(subject);
        email.setMessage(message);
        email.setHtml(isHtml);
        email.setFileIds(fileIds);
        email.setSent(false);
        return emailMessageRepository.save(email);
    }

    public void queueEmailVerification(String receiver, String otpCode) {
        String subject = "Verify your KMovie email address";
        String body = "Your KMovie email verification code is: " + otpCode
                + ". This code expires shortly, please use it soon.";
        queue(receiver, subject, body, false);
    }
}
