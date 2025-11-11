package messageapp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class MessageManager {

    private final List<Message> sentMessages = new ArrayList<>();
    private final List<Message> disregardedMessages = new ArrayList<>();
    private final List<Message> storedMessages = new ArrayList<>();

    private final List<String> messageHashes = new ArrayList<>();
    private final List<String> messageIDs = new ArrayList<>();

    public void addMessage(Message m) {
        switch (m.getFlag()) {
            case SENT:
                sentMessages.add(m);
                break;
            case STORED:
                storedMessages.add(m);
                break;
            case DISREGARD:
                disregardedMessages.add(m);
                break;
        }
        updateIndexes(m);
    }

    private void updateIndexes(Message m) {
        if (m.getHash() != null && !messageHashes.contains(m.getHash())) messageHashes.add(m.getHash());
        if (m.getMessageId() != null && !messageIDs.contains(m.getMessageId())) messageIDs.add(m.getMessageId());
    }

    public List<Message> getSentMessages() { return Collections.unmodifiableList(sentMessages); }
    public List<Message> getDisregardedMessages() { return Collections.unmodifiableList(disregardedMessages); }
    public List<Message> getStoredMessages() { return Collections.unmodifiableList(storedMessages); }
    public List<String> getMessageHashes() { return Collections.unmodifiableList(messageHashes); }
    public List<String> getMessageIDs() { return Collections.unmodifiableList(messageIDs); }

    public List<String> getSenderRecipientOfSent() {
        List<String> out = new ArrayList<>();
        for (Message m : sentMessages) {
            out.add(String.format("Sender: %s, Recipient: %s", m.getSender(), m.getRecipient()));
        }
        return out;
    }

    public Optional<Message> getLongestSentMessage() {
        return sentMessages.stream().max(Comparator.comparingInt(m -> m.getText() == null ? 0 : m.getText().length()));
    }

    public Optional<Message> findByMessageId(String messageId) {
        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        all.addAll(disregardedMessages);
        return all.stream().filter(m -> messageId.equals(m.getMessageId())).findFirst();
    }

    public List<Message> findAllByRecipient(String recipient) {
        List<Message> all = new ArrayList<>();
        all.addAll(sentMessages);
        all.addAll(storedMessages);
        return all.stream().filter(m -> recipient.equals(m.getRecipient())).collect(Collectors.toList());
    }

    public boolean deleteByHash(String hash) {
        boolean removed = sentMessages.removeIf(m -> hash.equals(m.getHash()));
        removed |= storedMessages.removeIf(m -> hash.equals(m.getHash()));
        removed |= disregardedMessages.removeIf(m -> hash.equals(m.getHash()));
        if (removed) {
            messageHashes.removeIf(h -> h.equals(hash));
            recomputeIds();
        }
        return removed;
    }

    private void recomputeIds() {
        messageIDs.clear();
        Set<String> ids = new LinkedHashSet<>();
        for (Message m : sentMessages) if (m.getMessageId() != null) ids.add(m.getMessageId());
        for (Message m : storedMessages) if (m.getMessageId() != null) ids.add(m.getMessageId());
        for (Message m : disregardedMessages) if (m.getMessageId() != null) ids.add(m.getMessageId());
        messageIDs.addAll(ids);
    }

    public String getSentMessagesReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("SENT MESSAGES REPORT\n");
        sb.append("--------------------\n");
        for (Message m : sentMessages) {
            sb.append("Message Hash: ").append(m.getHash()).append("\n");
            sb.append("Message ID: ").append(m.getMessageId()).append("\n");
            sb.append("Sender: ").append(m.getSender()).append("\n");
            sb.append("Recipient: ").append(m.getRecipient()).append("\n");
            sb.append("Text: ").append(m.getText()).append("\n");
            sb.append("Flag: ").append(m.getFlag()).append("\n");
            sb.append("--------------------\n");
        }
        return sb.toString();
    }

    public void readStoredMessagesFromJson(String pathToJson) throws IOException {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<MessageJson>>(){}.getType();
        try (FileReader fr = new FileReader(pathToJson)) {
            List<MessageJson> arr = gson.fromJson(fr, listType);
            if (arr == null) return;
            for (MessageJson mj : arr) {
                Message m = new Message(mj.messageId, mj.sender, mj.recipient, mj.text, mj.flag);
                addMessage(m);
            }
        }
    }

    private static class MessageJson {
        String messageId;
        String sender;
        String recipient;
        String text;
        Message.Flag flag;
    }
}
