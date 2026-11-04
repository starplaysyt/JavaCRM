package crm.ui;

import crm.model.Client;
import crm.model.ClientStatus;

import javax.swing.*;
import java.awt.*;

public final class ClientDialog extends JDialog {

    private final Client result;
    private final JTextField nameField = new JTextField(24);
    private final JTextField companyField = new JTextField(24);
    private final JTextField phoneField = new JTextField(24);
    private final JTextField emailField = new JTextField(24);
    private final JComboBox<ClientStatus> statusBox = new JComboBox<>(ClientStatus.values());
    private final JTextArea notesArea = new JTextArea(3, 24);
    private final JLabel errorLabel = new JLabel(" ");
    private final JButton saveButton = new JButton("Сохранить");
    private boolean saved;

    public ClientDialog(Window owner, Client source) {
        super(owner, source.getId() == 0 ? "Новый клиент" : "Клиент: " + source.getName(),
                ModalityType.APPLICATION_MODAL);

        result = new Client();
        result.setId(source.getId());
        result.setName(source.getName());
        result.setCompany(source.getCompany());
        result.setPhone(source.getPhone());
        result.setEmail(source.getEmail());
        result.setStatus(source.getStatus());
        result.setNotes(source.getNotes());

        build(source);
    }

    private void build(Client source) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        nameField.setText(source.getName());
        companyField.setText(source.getCompany());
        phoneField.setText(source.getPhone());
        emailField.setText(source.getEmail());
        statusBox.setSelectedItem(source.getStatus());
        notesArea.setText(source.getNotes());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        int row = 0;
        addRow(form, row++, "Имя *", nameField);
        addRow(form, row++, "Компания", companyField);
        addRow(form, row++, "Телефон", phoneField);
        addRow(form, row++, "Email", emailField);
        addRow(form, row++, "Статус", statusBox);

        GridBagConstraints notesLabelConstraints = new GridBagConstraints();
        notesLabelConstraints.gridx = 0;
        notesLabelConstraints.gridy = row;
        notesLabelConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        notesLabelConstraints.insets = new Insets(0, 0, 0, 12);
        form.add(label("Заметки"), notesLabelConstraints);

        GridBagConstraints notesConstraints = new GridBagConstraints();
        notesConstraints.gridx = 1;
        notesConstraints.gridy = row;
        notesConstraints.weightx = 1;
        notesConstraints.fill = GridBagConstraints.BOTH;
        form.add(new JScrollPane(notesArea), notesConstraints);

        errorLabel.setForeground(Color.RED);

        JButton cancelButton = new JButton("Отмена");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(cancelButton);
        buttons.add(saveButton);

        JPanel south = new JPanel(new BorderLayout(12, 0));
        south.add(errorLabel, BorderLayout.WEST);
        south.add(buttons, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(form, BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(saveButton);

        saveButton.setToolTipText("Сохранить клиента");
        cancelButton.setToolTipText("Закрыть без сохранения");
        saveButton.addActionListener(event -> save());
        cancelButton.addActionListener(event -> dispose());

        pack();
        setMinimumSize(new Dimension(460, getHeight()));
        setLocationRelativeTo(getOwner());
    }

    private void addRow(JPanel panel, int row, String labelText, Component field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_START;
        labelConstraints.insets = new Insets(0, 0, 10, 12);
        panel.add(label(labelText), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(0, 0, 10, 0);
        panel.add(field, fieldConstraints);
    }

    private static JLabel label(String text) {
        return new JLabel(text);
    }

    private void save() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("Укажите имя клиента.");
            nameField.requestFocus();
            return;
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            showError("Проверьте адрес электронной почты.");
            emailField.requestFocus();
            return;
        }

        result.setName(name);
        result.setCompany(companyField.getText().trim());
        result.setPhone(phoneField.getText().trim());
        result.setEmail(email);
        result.setStatus((ClientStatus) statusBox.getSelectedItem());
        result.setNotes(notesArea.getText().trim());
        saved = true;
        dispose();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        pack();
    }

    public Client getClient() {
        return saved ? result : null;
    }
}
