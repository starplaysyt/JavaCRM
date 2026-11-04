package crm.ui;

import crm.model.Client;
import crm.model.ClientStatus;
import crm.model.Deal;
import crm.model.DealStage;
import crm.storage.CrmStorage;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Модели таблиц с фильтрацией по строке поиска.
 */
public final class TableModels {

    private TableModels() {
    }

    public static final class Clients extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "Клиент", "Компания", "Телефон", "Email", "Статус", "Сделок"
        };

        private final CrmStorage storage;
        private final List<Client> all = new ArrayList<>();
        private final List<Client> visible = new ArrayList<>();
        private String filter = "";

        public Clients(CrmStorage storage) {
            this.storage = storage;
        }

        public void setClients(List<Client> clients) {
            all.clear();
            all.addAll(clients);
            applyFilter();
        }

        public void setFilter(String filter) {
            this.filter = filter == null ? "" : filter.trim().toLowerCase();
            applyFilter();
        }

        private void applyFilter() {
            visible.clear();
            for (Client client : all) {
                if (filter.isEmpty() || matches(client)) {
                    visible.add(client);
                }
            }
            fireTableDataChanged();
        }

        private boolean matches(Client client) {
            return contains(client.getName())
                    || contains(client.getCompany())
                    || contains(client.getPhone())
                    || contains(client.getEmail())
                    || contains(client.getStatus().toString());
        }

        private boolean contains(String value) {
            return value != null && value.toLowerCase().contains(filter);
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
            Client client = visible.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return client.getName();
                case 1:
                    return client.getCompany();
                case 2:
                    return client.getPhone();
                case 3:
                    return client.getEmail();
                case 4:
                    return client.getStatus();
                case 5:
                    return storage.getDealsForClient(client.getId()).size();
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 4) {
                return ClientStatus.class;
            }
            if (columnIndex == 5) {
                return Integer.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public Client getClient(int row) {
            return visible.get(row);
        }

        public int size() {
            return visible.size();
        }

        public int total() {
            return all.size();
        }
    }

    public static final class Deals extends AbstractTableModel {

        private static final String[] COLUMNS = {
                "Сделка", "Клиент", "Сумма", "Этап", "Закрытие"
        };

        private final CrmStorage storage;
        private final List<Deal> all = new ArrayList<>();
        private final List<Deal> visible = new ArrayList<>();
        private String filter = "";
        private DealStage stageFilter;

        public Deals(CrmStorage storage) {
            this.storage = storage;
        }

        public void setDeals(List<Deal> deals) {
            all.clear();
            all.addAll(deals);
            applyFilter();
        }

        public void setFilter(String filter, DealStage stage) {
            this.filter = filter == null ? "" : filter.trim().toLowerCase();
            this.stageFilter = stage;
            applyFilter();
        }

        private void applyFilter() {
            visible.clear();
            for (Deal deal : all) {
                if (matches(deal)) {
                    visible.add(deal);
                }
            }
            fireTableDataChanged();
        }

        private boolean matches(Deal deal) {
            if (stageFilter != null && deal.getStage() != stageFilter) {
                return false;
            }
            if (filter.isEmpty()) {
                return true;
            }
            Client client = storage.getClient(deal.getClientId());
            return contains(deal.getTitle())
                    || contains(client == null ? "" : client.getName())
                    || contains(client == null ? "" : client.getCompany())
                    || contains(deal.getStage().toString());
        }

        private boolean contains(String value) {
            return value != null && value.toLowerCase().contains(filter);
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
                case 4:
                    return Format.date(deal.getExpectedClose());
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

        public Deal getDeal(int row) {
            return visible.get(row);
        }

        public int size() {
            return visible.size();
        }

        public int total() {
            return all.size();
        }
    }
}
