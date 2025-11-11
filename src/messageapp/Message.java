package messageapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class Message {
    public enum Flag { SENT, STORED, DISREGARD }

    private String messageId;     // e.g. sender id or provided ID
    private String sender;        // can be "Developer" or phone number
    private String recipient;     // phone number
    private String text;
    private Flag flag;
    private String hash;          // computed hash

    public Message() {}

    public Message(String messageId, String sender, String recipient, String text, Flag flag) {
        this.messageId = messageId;
        this.sender = sender;
        this.recipient = recipient;
        this.text = text;
        this.flag = flag;
        this.hash = computeHash();
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; this.hash = computeHash(); }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; this.hash = computeHash(); }
    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; this.hash = computeHash(); }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; this.hash = computeHash(); }
    public Flag getFlag() { return flag; }
    public void setFlag(Flag flag) { this.flag = flag; }

    public String getHash() { return hash; }

    private String computeHash() {
        String base = (messageId == null ? "" : messageId) + "|" +
                      (sender == null ? "" : sender) + "|" +
                      (recipient == null ? "" : recipient) + "|" +
                      (text == null ? "" : text);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(base.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // fallback
            return Integer.toHexString(Objects.hash(base));
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", sender='" + sender + '\'' +
                ", recipient='" + recipient + '\'' +
                ", text='" + text + '\'' +
                ", flag=" + flag +
                ", hash='" + hash + '\'' +
                '}';
    }
}
