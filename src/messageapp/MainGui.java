package messageapp;

import messageapp.Message.Flag;



import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

/**
 * MainGui - Swing GUI front-end for the MessageManager system.
 *
 * Drop into your project (package com.myapp.messaging). Requires Message and MessageManager classes.
 * To enable "Load JSON" button functionality you should have gson on the classpath (gson-2.8.x.jar).
 */
public class MainGui extends JPanel {

    private final MessageManager mgr = new MessageManager();

    // Input fields
    private final JTextField tfMessageId = new JTextField(12);
    private final JTextField tfSender = new JTextField(12);
    private final JTextField tfRecipient = new JTextField(12);
    private final JTextField tfHashToDelete = new JTextField(40);
    private final JTextArea taMessageText = new JTextArea(3, 30);
    private final JComboBox<String> cbFlag = new JComboBox<>(new String[]{"SENT", "STORED", "DISREGARD"});

    // Displays
    private final DefaultListModel<String> lmSent = new DefaultListModel<>();
    private final DefaultListModel<String> lmStored = new DefaultListModel<>();
    private final DefaultListModel<String> lmDisregarded = new DefaultListModel<>();
    private final JList<String> listSent = new JList<>(lmSent);
    private final JList<String> listStored = new JList<>(lmStored);
    private final JList<String> listDisregarded = new JList<>(lmDisregarded);
    private final JTextArea taOutput = new JTextArea(15, 60);

    public MainGui() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        taOutput.setEditable(false);
        taMessageText.setLineWrap(true);
        taMessageText.setWrapStyleWord(true);
    }

    private JPanel createTopPanel() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.add(createInputPanel(), BorderLayout.CENTER);
        p.add(createButtonsPanel(), BorderLayout.EAST);
        return p;
    }

    private JPanel createInputPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Message ID:"), c);
        c.gridx = 1; p.add(tfMessageId, c);

        c.gridx = 0; c.gridy = 1; p.add(new JLabel("Sender:"), c);
        c.gridx = 1; p.add(tfSender, c);

        c.gridx = 0; c.gridy = 2; p.add(new JLabel("Recipient:"), c);
        c.gridx = 1; p.add(tfRecipient, c);

        c.gridx = 0; c.gridy = 3; p.add(new JLabel("Flag:"), c);
        c.gridx = 1; p.add(cbFlag, c);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; p.add(new JLabel("Message Text:"), c);
        c.gridy = 5; JScrollPane sp = new JScrollPane(taMessageText); p.add(sp, c);

        return p;
    }

    private JPanel createButtonsPanel() {
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));

        JButton btnAdd = new JButton("Add Message");
        btnAdd.addActionListener(this::onAddMessage);

        JButton btnLongest = new JButton("Show Longest Sent");
        btnLongest.addActionListener(e -> onShowLongest());

        JButton btnReport = new JButton("Show Sent Report");
        btnReport.addActionListener(e -> taOutput.setText(mgr.getSentMessagesReport()));

        JButton btnSearchById = new JButton("Search by ID");
        btnSearchById.addActionListener(e -> onSearchById());

        JButton btnSearchByRecipient = new JButton("Search by Recipient");
        btnSearchByRecipient.addActionListener(e -> onSearchByRecipient());

        JButton btnDeleteByHash = new JButton("Delete by Hash");
        btnDeleteByHash.addActionListener(e -> onDeleteByHash());

        JButton btnLoadJson = new JButton("Load Stored JSON...");
        btnLoadJson.addActionListener(e -> onLoadJson());

        p.add(btnAdd);
        p.add(btnLongest);
        p.add(btnReport);
        p.add(btnSearchById);
        p.add(btnSearchByRecipient);
        p.add(new JLabel("Hash to delete (paste):"));
        p.add(tfHashToDelete);
        p.add(btnDeleteByHash);
        p.add(Box.createVerticalStrut(10));
        p.add(btnLoadJson);

        return p;
    }

    private JSplitPane createCenterPanel() {
        JPanel left = new JPanel(new BorderLayout(4,4));
        left.add(new JLabel("Sent Messages"), BorderLayout.NORTH);
        left.add(new JScrollPane(listSent), BorderLayout.CENTER);

        JPanel mid = new JPanel(new BorderLayout(4,4));
        mid.add(new JLabel("Stored Messages"), BorderLayout.NORTH);
        mid.add(new JScrollPane(listStored), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(4,4));
        right.add(new JLabel("Disregarded Messages"), BorderLayout.NORTH);
        right.add(new JScrollPane(listDisregarded), BorderLayout.CENTER);

        JPanel lists = new JPanel(new GridLayout(1,3,8,8));
        lists.add(left);
        lists.add(mid);
        lists.add(right);

        JScrollPane outputPane = new JScrollPane(taOutput);
        outputPane.setBorder(BorderFactory.createTitledBorder("Output / Report"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, lists, outputPane);
        split.setResizeWeight(0.35);

        return split;
    }

    private JPanel createBottomPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(new JLabel("Tip: copy message hash from the report to delete a message."));
        return p;
    }

    // ------------- Actions --------------

    private void onAddMessage(ActionEvent ev) {
        try {
            String id = tfMessageId.getText().trim();
            String sender = tfSender.getText().trim();
            String recipient = tfRecipient.getText().trim();
            String text = taMessageText.getText().trim();
            String flagStr = ((String) cbFlag.getSelectedItem()).trim().toUpperCase();
            if (text.isEmpty() || recipient.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Recipient and Message text are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Flag flag = Flag.valueOf(flagStr);
            Message m = new Message(id.isEmpty() ? null : id, sender.isEmpty() ? "Unknown" : sender, recipient, text, flag);
            mgr.addMessage(m);
            updateLists();
            taOutput.setText("Message added.\n\n" + m.toString());
            clearInputs();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Invalid flag value. Use SENT, STORED or DISREGARD.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onShowLongest() {
        Optional<Message> longest = mgr.getLongestSentMessage();
        if (longest.isPresent()) {
            taOutput.setText("Longest Sent Message:\n" + longest.get().getText() + "\n\nFull:\n" + longest.get().toString());
        } else {
            taOutput.setText("No sent messages available.");
        }
    }

    private void onSearchById() {
        String id = JOptionPane.showInputDialog(this, "Enter Message ID to search:");
        if (id == null || id.trim().isEmpty()) return;
        Optional<Message> found = mgr.findByMessageId(id.trim());
        if (found.isPresent()) {
            Message m = found.get();
            taOutput.setText("Found message by ID:\nRecipient: " + m.getRecipient() + "\nMessage: " + m.getText() + "\n\nFull:\n" + m.toString());
        } else {
            taOutput.setText("Message ID not found.");
        }
    }

    private void onSearchByRecipient() {
        String r = JOptionPane.showInputDialog(this, "Enter Recipient to search (e.g. +27838884567):");
        if (r == null || r.trim().isEmpty()) return;
        List<Message> found = mgr.findAllByRecipient(r.trim());
        if (found.isEmpty()) {
            taOutput.setText("No messages for recipient: " + r);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Messages for ").append(r).append(":\n\n");
            for (Message m : found) {
                sb.append("Message ID: ").append(m.getMessageId()).append("\n");
                sb.append("Text: ").append(m.getText()).append("\n");
                sb.append("Flag: ").append(m.getFlag()).append("\n");
                sb.append("Hash: ").append(m.getHash()).append("\n");
                sb.append("-------------------\n");
            }
            taOutput.setText(sb.toString());
        }
    }

    private void onDeleteByHash() {
        String hash = tfHashToDelete.getText().trim();
        if (hash.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Paste the hash to delete, or copy from the report.", "Missing hash", JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean ok = mgr.deleteByHash(hash);
        if (ok) {
            updateLists();
            taOutput.setText("Message with hash " + hash + " successfully deleted.");
            tfHashToDelete.setText("");
        } else {
            taOutput.setText("No message found with hash: " + hash);
        }
    }

    private void onLoadJson() {
        JFileChooser fc = new JFileChooser();
        int res = fc.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        try {
            String path = fc.getSelectedFile().getAbsolutePath();
            // MessageManager.readStoredMessagesFromJson uses Gson; show clear error if Gson not present
            mgr.readStoredMessagesFromJson(path);
            updateLists();
            taOutput.setText("Loaded messages from JSON: " + path);
        } catch (NoClassDefFoundError ncd) {
            taOutput.setText("Gson library not found. Add gson-2.8.x.jar to project libraries to use JSON load.");
        } catch (Exception ex) {
            taOutput.setText("Error loading JSON: " + ex.getMessage());
        }
    }

    private void clearInputs() {
        tfMessageId.setText("");
        tfSender.setText("");
        tfRecipient.setText("");
        taMessageText.setText("");
        cbFlag.setSelectedIndex(0);
    }

    private void updateLists() {
        lmSent.clear();
        lmStored.clear();
        lmDisregarded.clear();

        for (Message m : mgr.getSentMessages()) {
            lmSent.addElement(displaySummary(m));
        }
        for (Message m : mgr.getStoredMessages()) {
            lmStored.addElement(displaySummaryWithHash(m));
        }
        for (Message m : mgr.getDisregardedMessages()) {
            lmDisregarded.addElement(displaySummary(m));
        }
    }

    private String displaySummary(Message m) {
        return String.format("[%s] %s -> %s : %s", m.getMessageId(), m.getSender(), m.getRecipient(), truncate(m.getText(), 40));
    }

    private String displaySummaryWithHash(Message m) {
        return String.format("[%s] %s -> %s : %s (hash: %s)", m.getMessageId(), m.getSender(), m.getRecipient(), truncate(m.getText(), 28), m.getHash());
    }

    private String truncate(String s, int len) {
        if (s == null) return "";
        return s.length() <= len ? s : s.substring(0, len - 3) + "...";
    }

    // ---------- Helper to start the GUI ----------
    public static void createAndShowGui() {
        JFrame f = new JFrame("Message Management System");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add(new MainGui());
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // If run directly, create and show the GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainGui::createAndShowGui);
    }
}
