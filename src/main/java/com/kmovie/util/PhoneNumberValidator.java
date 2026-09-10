package com.kmovie.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.springframework.stereotype.Component;

/**
 * Wraps Google's libphonenumber to validate and normalize phone numbers
 * to E.164 format before they're stored / used to send SMS.
 */
@Component
public class PhoneNumberValidator {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public boolean isValid(String rawNumber) {
        try {
            Phonenumber.PhoneNumber parsed = phoneNumberUtil.parse(rawNumber, null);
            return phoneNumberUtil.isValidNumber(parsed);
        } catch (NumberParseException e) {
            return false;
        }
    }

    /** Returns the E.164 normalized form, e.g. +254712345678. Throws if invalid. */
    public String normalize(String rawNumber) {
        try {
            Phonenumber.PhoneNumber parsed = phoneNumberUtil.parse(rawNumber, null);
            if (!phoneNumberUtil.isValidNumber(parsed)) {
                throw new IllegalArgumentException("Invalid phone number: " + rawNumber);
            }
            return phoneNumberUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Invalid phone number: " + rawNumber, e);
        }
    }
}
