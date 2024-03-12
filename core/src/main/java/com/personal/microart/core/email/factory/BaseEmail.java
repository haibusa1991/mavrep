package com.personal.microart.core.email.factory;

import com.personal.microart.core.email.sender.Email;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Shared base class for all emails. It contains the basic fields that are required for all emails.

 */
@Setter(AccessLevel.PROTECTED)
@Getter
public abstract class BaseEmail implements Email {
    private String to;
    private String subject;
    private String htmlBody;
}
