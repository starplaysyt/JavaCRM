package crm.ui;

import crm.model.Client;
import crm.model.Deal;
import crm.model.DealStage;
import crm.storage.CrmStorage;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class DashboardPanel extends JPanel implements CrmPanel {

    private final CrmStorage storage;
    private final JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 10, 10));
    private final JPanel funnelPanel = new JPanel();
    private final RecentDealsModel recentModel;
    private final JTable recentTable;
    private String query = "";

    public DashboardPanel(CrmStorage storage) {
        this.storage = storage;
        this.recentModel = new RecentDealsModel(storage);
        this.recentTable = new JTable(recentModel);

        setLayout(new BorderLayout(0, 10));
        add(kpiPanel, BorderLayout.NORTH);

        recentTable.getColumnModel().getColumn(0).setPreferredWidth(220);
        recentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        recentTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        recentTable.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane recentScroll = new JScrollPane(recentTable);
        JPanel body = new JPanel(new GridLayout(1, 2, 10, 10));
        body.add(section("Воронка продаж", funnelPanel));
        body.add(section("Последние сделки", recentScroll));
        add(body, BorderLayout.CENTER);

        refresh();
    }

    @Override
    public void refresh() {
        rebuildKpis();
        rebuildFunnel();

        List<Deal> deals = new ArrayList<>(storage.getDeals());
        deals.sort((first, second) -> Long.compare(second.getId(), first.getId()));
        List<Deal> recent = deals.size() > 6 ? deals.subList(0, 6) : deals;
        recentModel.setDeals(recent);
        recentModel.setFilter(query);
    }

    @Override
    public void filter(String query) {
        this.query = query == null ? "" : query;
        recentModel.setFilter(this.query);
    }

    private void rebuildKpis() {
        kpiPanel.removeAll();

        int clientCount = storage.getClients().size();
        List<Deal> deals = storage.getDeals();
        int activeCount = 0;
        double activeSum = 0;
        double wonSum = 0;
        for (Deal deal : deals) {
            if (deal.getStage().isActive()) {
                activeCount++;
                activeSum += deal.getAmount();
            }
            if (deal.getStage() == DealStage.WON) {
                wonSum += deal.getAmount();
            }
        }

        kpiPanel.add(card("Клиенты", String.valueOf(clientCount), "в базе"));
        kpiPanel.add(card("Активные сделки", String.valueOf(activeCount), "в работе"));
        kpiPanel.add(card("Сумма в работе", Format.money(activeSum), "потенциал"));
        kpiPanel.add(card("Выручка", Format.money(wonSum), "закрытые сделки"));
    }

    private static JPanel card(String labelText, String valueText, String captionText) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.add(new JLabel(labelText), BorderLayout.NORTH);
        panel.add(new JLabel(valueText), BorderLayout.CENTER);
        panel.add(new JLabel(captionText), BorderLayout.SOUTH);
        return panel;
    }

    private void rebuildFunnel() {
        funnelPanel.removeAll();
        funnelPanel.setLayout(new BoxLayout(funnelPanel, BoxLayout.Y_AXIS));

        List<Deal> deals = storage.getDeals();
        int max = 1;
        for (DealStage stage : DealStage.values()) {
            int count = 0;
            for (Deal deal : deals) {
                if (deal.getStage() == stage) {
                    count++;
                }
            }
            max = Math.max(max, count);
        }

        for (DealStage stage : DealStage.values()) {
            int count = 0;
            double sum = 0;
            for (Deal deal : deals) {
                if (deal.getStage() == stage) {
                    count++;
                    sum += deal.getAmount();
                }
            }
            funnelPanel.add(funnelRow(stage, count, sum, max));
            funnelPanel.add(Box.createVerticalStrut(6));
        }
        funnelPanel.add(Box.createVerticalGlue());
    }

    private static JPanel funnelRow(DealStage stage, int count, double sum, int max) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel label = new JLabel(stage.getLabel());
        label.setPreferredSize(new Dimension(110, 20));
        row.add(label, BorderLayout.WEST);

        JProgressBar bar = new JProgressBar(0, Math.max(max, 1));
        bar.setValue(count);
        bar.setPreferredSize(new Dimension(180, 14));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        row.add(bar, BorderLayout.CENTER);

        JLabel counts = new JLabel(count + " · " + Format.money(sum));
        counts.setPreferredSize(new Dimension(150, 20));
        row.add(counts, BorderLayout.EAST);

        return row;
    }

    private static JPanel section(String title, Component content) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private static final class RecentDealsModel extends AbstractTableModel {

        private static final String[] COLUMNS = {"Сделка", "Клиент", "Сумма", "Этап"};

        private final CrmStorage storage;
        private final List<Deal> all = new ArrayList<>();
        private final List<Deal> visible = new ArrayList<>();
        private String filter = "";

        RecentDealsModel(CrmStorage storage) {
            this.storage = storage;
        }

        void setDeals(List<Deal> deals) {
            all.clear();
            all.addAll(deals);
            applyFilter();
        }

        void setFilter(String filter) {
            this.filter = filter == null ? "" : filter.trim().toLowerCase();
            applyFilter();
        }

        private void applyFilter() {
            visible.clear();
            for (Deal deal : all) {
                if (filter.isEmpty() || matches(deal)) {
                    visible.add(deal);
                }
            }
            fireTableDataChanged();
        }

        private boolean matches(Deal deal) {
            Client client = storage.getClient(deal.getClientId());
            String clientName = client == null ? "" : client.getName();
            return deal.getTitle().toLowerCase().contains(filter)
                    || clientName.toLowerCase().contains(filter)
                    || deal.getStage().toString().toLowerCase().contains(filter);
        }

        @Override
        public int getRowCount() {
            return visible.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Deal deal = visible.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return deal.getTitle();
                case 1:
                    Client client = storage.getClient(deal.getClientId());
                    return client == null ? "Клиент удалён" : client.getName();
                case 2:
                    return Format.money(deal.getAmount());
                case 3:
                    return deal.getStage();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 3) {
                return DealStage.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }
}
