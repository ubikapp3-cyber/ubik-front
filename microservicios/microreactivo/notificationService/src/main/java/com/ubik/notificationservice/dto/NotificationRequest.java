package com.ubik.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class NotificationRequest {
    @NotBlank
    @Email
    private String to;

    @NotBlank
    private String subject;

    @NotBlank
    private String message;

    private byte[] attachment;
    
    private String attachmentName;

    public NotificationRequest() {
    }

    public NotificationRequest(String to, String subject, String message) {
        this.to = to;
        this.subject = subject;
        this.message = message;
    }

    public NotificationRequest(String to, String subject, String message, byte[] attachment, String attachmentName) {
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.attachment = attachment;
        this.attachmentName = attachmentName;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }
}
