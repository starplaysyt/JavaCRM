package crm.ui;

import crm.storage.CrmStorage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Главное окно CRM. Используются стандартные компоненты Swing
 * и системное оформление, без собственной темы.
 */
public final class MainFrame extends JFrame {

    private final CrmStorage storage;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final DashboardPanel dashboard;
    private final ClientsPanel clients;
    private final DealsPanel deals;
    private final JLabel titleLabel = new JLabel("Обзор");
    private final JLabel subtitleLabel = new JLabel("");
    private final JTextField searchField = new JTextField(16);
    private final JToggleButton[] navButtons = new JToggleButton[3];
    private String query = "";

    public MainFrame(CrmStorage storage) {
        super("CRM Lite - клиенты и сделки");
        this.storage = storage;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1180, 760);
        setLocationRelativeTo(null);

        dashboard = new DashboardPanel(storage);
        clients = new ClientsPanel(storage, this);
        deals = new DealsPanel(storage, this);

        content.add(dashboard, "dashboard");
        content.add(clients, "clients");
        content.add(deals, "deals");

        JPanel root = new JPanel(new BorderLayout());
        root.add(sidebar(), BorderLayout.WEST);
        root.add(mainArea(), BorderLayout.CENTER);
        setContentPane(root);

        updateSubtitle();
        showCard("dashboard");
    }

    private JPanel sidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(232, 10));

        JLabel appName = new JLabel("CRM Lite");
        JLabel tagline = new JLabel("sales workspace");

        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        brand.add(appName, BorderLayout.NORTH);
        brand.add(tagline, BorderLayout.SOUTH);

        navButtons[0] = new JToggleButton("Обзор");
        navButtons[1] = new JToggleButton("Клиенты");
        navButtons[2] = new JToggleButton("Сделки");

        JPanel nav = new JPanel(new GridLayout(0, 1, 0, 4));
        nav.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

        ButtonGroup group = new ButtonGroup();
        for (JToggleButton button : navButtons) {
            group.add(button);
            nav.add(button);
        }
        navButtons[0].setSelected(true);

        navButtons[0].addActionListener(event -> showCard("dashboard"));
        navButtons[1].addActionListener(event -> showCard("clients"));
        navButtons[2].addActionListener(event -> showCard("deals"));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(brand);
        top.add(nav);
        panel.add(top, BorderLayout.NORTH);

        JLabel fileLabel = new JLabel("Данные: crm-data.dat");
        JLabel saveLabel = new JLabel("Автосохранение включено");
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 4));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        footer.add(fileLabel);
        footer.add(saveLabel);
        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel mainArea() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(header(), BorderLayout.NORTH);
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel header() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.add(titleLabel);
        titles.add(subtitleLabel);

        panel.add(titles, BorderLayout.WEST);
        panel.add(searchBox(), BorderLayout.EAST);
        return panel;
    }

    private JPanel searchBox() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                applySearch(searchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                applySearch(searchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                applySearch(searchField.getText());
            }
        });

        JPanel box = new JPanel(new BorderLayout(8, 0));
        box.add(new JLabel("Поиск:"), BorderLayout.WEST);
        box.add(searchField, BorderLayout.CENTER);
        return box;
    }

    private void applySearch(String text) {
        query = text == null ? "" : text;
        dashboard.filter(query);
        clients.filter(query);
        deals.filter(query);
    }

    private void showCard(String key) {
        cards.show(content, key);
        switch (key) {
            case "clients":
                titleLabel.setText("Клиенты");
                break;
            case "deals":
                titleLabel.setText("Сделки");
                break;
            default:
                titleLabel.setText("Обзор");
                break;
        }
        searchField.setText("");
        applySearch("");
    }

    public void refreshAll() {
        dashboard.refresh();
        clients.refresh();
        deals.refresh();
        applySearch(query);
        updateSubtitle();
    }

    private void updateSubtitle() {
        int clientCount = storage.getClients().size();
        int dealCount = storage.getDeals().size();
        subtitleLabel.setText(clientCount + " " + Format.plural(clientCount, "клиент", "клиента", "клиентов")
                + " · " + dealCount + " " + Format.plural(dealCount, "сделка", "сделки", "сделок"));
    }
}
