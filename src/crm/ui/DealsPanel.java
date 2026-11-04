package crm.ui;

import crm.model.Deal;
import crm.model.DealStage;
import crm.storage.CrmStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class DealsPanel extends JPanel implements CrmPanel {

    private final CrmStorage storage;
    private final MainFrame frame;
    private final TableModels.Deals model;
    private final JTable table;
    private final JLabel countLabel = new JLabel("");
    private final JButton addButton = new JButton("Добавить сделку");
    private final JButton editButton = new JButton("Изменить");
    private final JButton deleteButton = new JButton("Удалить");
    private final JComboBox<DealStage> stageBox = new JComboBox<>();
    private String query = "";

    public DealsPanel(CrmStorage storage, MainFrame frame) {
        this.storage = storage;
        this.frame = frame;
        this.model = new TableModels.Deals(storage);
        this.table = new JTable(model);

        setLayout(new BorderLayout());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureColumns();
        configureStageFilter();

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
        stageBox.addActionListener(event -> applyFilter());

        refresh();
    }

    private void configureColumns() {
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
    }

    private void configureStageFilter() {
        stageBox.addItem(null);
        for (DealStage stage : DealStage.values()) {
            stageBox.addItem(stage);
        }
        stageBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Все этапы" : value.toString());
                return this;
            }
        });
    }

    private JPanel toolbar() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.add(addButton);
        actions.add(editButton);
        actions.add(deleteButton);

        JLabel filterLabel = new JLabel("Этап:");
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.add(filterLabel);
        filterPanel.add(stageBox);
        filterPanel.add(countLabel);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(actions, BorderLayout.WEST);
        panel.add(filterPanel, BorderLayout.EAST);
        return panel;
    }

    private JScrollPane scrollPane() {
        return new JScrollPane(table);
    }

    private JLabel hint() {
        return new JLabel(
                "Двойной клик по строке - редактирование. Сумма указывается в BYN.");
    }

    @Override
    public void refresh() {
        model.setDeals(storage.getDeals());
        applyFilter();
    }

    @Override
    public void filter(String query) {
        this.query = query == null ? "" : query;
        applyFilter();
    }

    private void applyFilter() {
        DealStage stage = (DealStage) stageBox.getSelectedItem();
        model.setFilter(query, stage);
        countLabel.setText(countText());
    }

    private String countText() {
        int total = model.total();
        if (query.isEmpty() && stageBox.getSelectedItem() == null) {
            return total + " " + Format.plural(total, "сделка", "сделки", "сделок");
        }
        return "Показано: " + model.size() + " из " + total;
    }

    private void add() {
        if (storage.getClients().isEmpty()) {
            JOptionPane.showMessageDialog(frame,
                    "Сначала добавьте хотя бы одного клиента.",
                    "Нет клиентов", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        DealDialog dialog = new DealDialog(frame, storage, new Deal());
        dialog.setVisible(true);
        Deal created = dialog.getDeal();
        if (created != null) {
            storage.addDeal(created);
            frame.refreshAll();
        }
    }

    private void edit() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        DealDialog dialog = new DealDialog(frame, storage, model.getDeal(row));
        dialog.setVisible(true);
        Deal updated = dialog.getDeal();
        if (updated != null) {
            storage.updateDeal(updated);
            frame.refreshAll();
        }
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        Deal deal = model.getDeal(row);
        int answer = JOptionPane.showConfirmDialog(frame,
                "Удалить сделку «" + deal.getTitle() + "»?",
                "Удаление сделки", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            storage.deleteDeal(deal.getId());
            frame.refreshAll();
        }
    }
}
