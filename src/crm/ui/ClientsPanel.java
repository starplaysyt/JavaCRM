package crm.ui;

import crm.model.Client;
import crm.storage.CrmStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class ClientsPanel extends JPanel implements CrmPanel {

    private final CrmStorage storage;
    private final MainFrame frame;
    private final TableModels.Clients model;
    private final JTable table;
    private final JLabel countLabel = new JLabel("");
    private final JButton addButton = new JButton("Добавить клиента");
    private final JButton editButton = new JButton("Изменить");
    private final JButton deleteButton = new JButton("Удалить");
    private String query = "";

    public ClientsPanel(CrmStorage storage, MainFrame frame) {
        this.storage = storage;
        this.frame = frame;
        this.model = new TableModels.Clients(storage);
        this.table = new JTable(model);

        setLayout(new BorderLayout());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureColumns();

        add(toolbar(), BorderLayout.NORTH);
        add(scrollPane(), BorderLayout.CENTER);
        add(hint(), BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(event -> {
            boolean selected = table.getSelectedRow() >= 0;
            editButton.setEnabled(selected);
            deleteButton.setEnabled(selected);
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    edit();
                }
            }
        });

        addButton.addActionListener(event -> add());
        editButton.addActionListener(event -> edit());
        deleteButton.addActionListener(event -> delete());

        refresh();
    }

    private void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(170);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(190);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
    }

    private JPanel toolbar() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(actions, BorderLayout.WEST);
        panel.add(countLabel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane scrollPane() {
        return new JScrollPane(table);
    }

    private JLabel hint() {
        return new JLabel(
                "Двойной клик по строке — редактирование. Изменения сохраняются автоматически.");
    }

    @Override
    public void refresh() {
        model.setClients(storage.getClients());
        applyFilter();
    }

    @Override
    public void filter(String query) {
        this.query = query == null ? "" : query;
        applyFilter();
    }

    private void applyFilter() {
        model.setFilter(query);
        countLabel.setText(countText());
    }

    private String countText() {
        int total = model.total();
        if (query.isEmpty()) {
            return total + " " + Format.plural(total, "клиент", "клиента", "клиентов");
        }
        return "Найдено: " + model.size() + " из " + total;
    }

    private void add() {
        ClientDialog dialog = new ClientDialog(frame, new Client());
        dialog.setVisible(true);
        Client created = dialog.getClient();
        if (created != null) {
            storage.addClient(created);
            frame.refreshAll();
        }
    }

    private void edit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        ClientDialog dialog = new ClientDialog(frame, model.getClient(row));
        dialog.setVisible(true);
        Client updated = dialog.getClient();
        if (updated != null) {
            storage.updateClient(updated);
            frame.refreshAll();
        }
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        Client client = model.getClient(row);
        int answer = JOptionPane.showConfirmDialog(frame,
                "Удалить клиента «" + client.getName() + "» и все связанные сделки?",
                "Удаление клиента", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            storage.deleteClient(client.getId());
            frame.refreshAll();
        }
    }
}
