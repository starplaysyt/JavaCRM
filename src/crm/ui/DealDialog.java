package crm.ui;

import crm.model.Client;
import crm.model.Deal;
import crm.model.DealStage;
import crm.storage.CrmStorage;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DealDialog extends JDialog {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Deal result;
    private final JComboBox<Client> clientBox = new JComboBox<>();
    private final JTextField titleField = new JTextField(26);
    private final JTextField amountField = new JTextField(26);
    private final JTextField dateField = new JTextField(26);
    private final JComboBox<DealStage> stageBox = new JComboBox<>(DealStage.values());
    private final JTextArea notesArea = new JTextArea(3, 26);
    private final JLabel errorLabel = new JLabel(" ");
    private final JButton saveButton = new JButton("Сохранить");
    private boolean saved;

    public DealDialog(Window owner, CrmStorage storage, Deal source) {
        super(owner, source.getId() == 0 ? "Новая сделка" : "Сделка: " + source.getTitle(),
                ModalityType.APPLICATION_MODAL);

        result = new Deal();
        result.setId(source.getId());
        result.setTitle(source.getTitle());
        result.setClientId(source.getClientId());
        result.setAmount(source.getAmount());
        result.setStage(source.getStage());
        result.setExpectedClose(source.getExpectedClose());
        result.setNotes(source.getNotes());

        build(storage, source);
    }

    private void build(CrmStorage storage, Deal source) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        for (Client client : storage.getClients()) {
            clientBox.addItem(client);
        }
        for (int i = 0; i < clientBox.getItemCount(); i++) {
            Client client = clientBox.getItemAt(i);
            if (client.getId() == source.getClientId()) {
                clientBox.setSelectedIndex(i);
                break;
            }
        }

        titleField.setText(source.getTitle());
        amountField.setText(amountToText(source.getAmount()));
        dateField.setText(source.getExpectedClose() == null ? "" : DATE_FORMAT.format(source.getExpectedClose()));
        stageBox.setSelectedItem(source.getStage());
        notesArea.setText(source.getNotes());
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        int row = 0;
        addRow(form, row++, "Название *", titleField);
        addRow(form, row++, "Клиент *", clientBox);
        addRow(form, row++, "Сумма, BYN *", amountField);
        addRow(form, row++, "Этап", stageBox);
        addRow(form, row++, "Закрытие", dateField);

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

        saveButton.setToolTipText("Сохранить сделку");
        cancelButton.setToolTipText("Закрыть без сохранения");
        saveButton.addActionListener(event -> save());
        cancelButton.addActionListener(event -> dispose());

        pack();
        setMinimumSize(new Dimension(480, getHeight()));
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

    private static String amountToText(double value) {
        if (value == 0) {
            return "";
        }
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void save() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showError("Укажите название сделки.");
            titleField.requestFocus();
            return;
        }

        Client client = (Client) clientBox.getSelectedItem();
        if (client == null) {
            showError("Выберите клиента.");
            clientBox.requestFocus();
            return;
        }

        String amountText = amountField.getText().trim().replace(" ", "").replace(",", ".");
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException exception) {
            showError("Сумма должна быть числом.");
            amountField.requestFocus();
            return;
        }
        if (amount < 0) {
            showError("Сумма не может быть отрицательной.");
            amountField.requestFocus();
            return;
        }

        LocalDate expectedClose = null;
        String dateText = dateField.getText().trim();
        if (!dateText.isEmpty()) {
            try {
                expectedClose = LocalDate.parse(dateText, DATE_FORMAT);
            } catch (DateTimeParseException exception) {
                showError("Дата должна быть в формате дд.мм.гггг.");
                dateField.requestFocus();
                return;
            }
        }

        result.setTitle(title);
        result.setClientId(client.getId());
        result.setAmount(amount);
        result.setStage((DealStage) stageBox.getSelectedItem());
        result.setExpectedClose(expectedClose);
        result.setNotes(notesArea.getText().trim());
        saved = true;
        dispose();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        pack();
    }

    public Deal getDeal() {
        return saved ? result : null;
    }
}
