package com.kmovie.service;

import com.kmovie.entity.SmsMessage;
import com.kmovie.repository.SmsMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Only ever queues messages; a separate external background worker process
 * dispatches pending SmsMessage rows via Twilio / Africa's Talking and
 * marks them sent.
 */
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsMessageRepository smsMessageRepository;

    public SmsMessage queue(String receiverE164, String message) {
        SmsMessage sms = new SmsMessage();
        sms.setReceiver(receiverE164);
        sms.setMessage(message);
        sms.setSent(false);
        return smsMessageRepository.save(sms);
    }

    public void queuePhoneVerification(String receiverE164, String otpCode) {
        queue(receiverE164, "Your KMovie verification code is " + otpCode);
    }
}
