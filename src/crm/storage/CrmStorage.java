package crm.storage;

import crm.model.Client;
import crm.model.ClientStatus;
import crm.model.Deal;
import crm.model.DealStage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CrmStorage {

    public static final String DATA_FILE = "crm-data.dat";

    private final List<Client> clients = new ArrayList<>();
    private final List<Deal> deals = new ArrayList<>();

    private CrmStorage() {
    }

    public static CrmStorage load() {
        CrmStorage storage = new CrmStorage();
        File file = new File(System.getProperty("user.dir"), DATA_FILE);
        boolean loaded = false;

        if (file.isFile()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
                Data data = (Data) in.readObject();
                storage.clients.addAll(data.clients);
                storage.deals.addAll(data.deals);
                loaded = true;
            } catch (Exception exception) {
                System.err.println("Не удалось прочитать " + DATA_FILE + ": " + exception.getMessage());
            }
        }

        if (!loaded) {
            storage.seedDemoData();
            storage.save();
        }
        return storage;
    }

    public void save() {
        File file = new File(System.getProperty("user.dir"), DATA_FILE);
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new Data(clients, deals));
        } catch (Exception exception) {
            System.err.println("Не удалось сохранить " + DATA_FILE + ": " + exception.getMessage());
        }
    }

    public List<Client> getClients() {
        return Collections.unmodifiableList(clients);
    }

    public List<Deal> getDeals() {
        return Collections.unmodifiableList(deals);
    }

    public Client getClient(long id) {
        for (Client client : clients) {
            if (client.getId() == id) {
                return client;
            }
        }
        return null;
    }

    public long addClient(Client client) {
        client.setId(nextClientId());
        clients.add(client);
        save();
        return client.getId();
    }

    public void updateClient(Client client) {
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getId() == client.getId()) {
                clients.set(i, client);
                save();
                return;
            }
        }
        clients.add(client);
        save();
    }

    public void deleteClient(long id) {
        clients.removeIf(client -> client.getId() == id);
        deals.removeIf(deal -> deal.getClientId() == id);
        save();
    }

    public long addDeal(Deal deal) {
        deal.setId(nextDealId());
        deals.add(deal);
        save();
        return deal.getId();
    }

    public void updateDeal(Deal deal) {
        for (int i = 0; i < deals.size(); i++) {
            if (deals.get(i).getId() == deal.getId()) {
                deals.set(i, deal);
                save();
                return;
            }
        }
        deals.add(deal);
        save();
    }

    public void deleteDeal(long id) {
        deals.removeIf(deal -> deal.getId() == id);
        save();
    }

    public List<Deal> getDealsForClient(long clientId) {
        List<Deal> result = new ArrayList<>();
        for (Deal deal : deals) {
            if (deal.getClientId() == clientId) {
                result.add(deal);
            }
        }
        return result;
    }

    private long nextClientId() {
        long max = 0;
        for (Client client : clients) {
            max = Math.max(max, client.getId());
        }
        return max + 1;
    }

    private long nextDealId() {
        long max = 0;
        for (Deal deal : deals) {
            max = Math.max(max, deal.getId());
        }
        return max + 1;
    }

    private void seedDemoData() {
        LocalDate today = LocalDate.now();

        clients.add(new Client(1, "Алексей Ковалёв", "ООО «Белтех»", "+375 (29) 123-45-67",
                "a.kovalev@beltech.by", ClientStatus.CLIENT, "Основной контакт по закупкам."));
        clients.add(new Client(2, "Марина Сидорова", "ИП Сидорова", "+375 (33) 765-43-21",
                "m.sidorova@mail.by", ClientStatus.LEAD, "Интересуется сайтом под ключ."));
        clients.add(new Client(3, "Дмитрий Литвин", "ООО «Полесье Агро»", "+375 (25) 998-11-22",
                "d.litvin@agro.by", ClientStatus.CLIENT, "Годовой контракт на поддержку."));
        clients.add(new Client(4, "Ирина Мельник", "ЧТУП «СтройДом»", "+375 (29) 444-55-66",
                "i.melnik@stroy.by", ClientStatus.PARTNER, "Партнёр по направлению ремонта."));
        clients.add(new Client(5, "Сергей Гончар", "ООО «Логистик Плюс»", "+375 (33) 321-00-99",
                "s.gonchar@logplus.by", ClientStatus.LEAD, "Запрос на аудит логистики."));
        clients.add(new Client(6, "Ольга Петрова", "ООО «Медика»", "+375 (29) 777-88-99",
                "o.petrova@medica.by", ClientStatus.INACTIVE, "Клиент с прошлого года, на паузе."));

        deals.add(new Deal(1, "Внедрение CRM для «Белтех»", 1, 12000, DealStage.PAYMENT,
                today.plusDays(5), "Счёт отправлен, ждём оплату."));
        deals.add(new Deal(2, "Поставка оборудования", 3, 4500, DealStage.NEGOTIATION,
                today.plusDays(12), "Согласовать спецификацию."));
        deals.add(new Deal(3, "Годовая поддержка", 1, 2400, DealStage.WON,
                today.minusDays(10), "Договор подписан."));
        deals.add(new Deal(4, "Разработка сайта", 2, 3200, DealStage.NEW,
                today.plusDays(20), "Ждём бриф от клиента."));
        deals.add(new Deal(5, "Ремонт офиса", 4, 8000, DealStage.PROPOSAL,
                today.plusDays(7), "Отправили смету."));
        deals.add(new Deal(6, "Логистический аудит", 5, 1800, DealStage.NEW,
                today.plusDays(25), ""));
        deals.add(new Deal(7, "Обучение сотрудников", 3, 900, DealStage.WON,
                today.minusDays(20), "Проведено два тренинга."));
        deals.add(new Deal(8, "Модернизация оборудования", 4, 5600, DealStage.LOST,
                today.minusDays(5), "Клиент выбрал другого подрядчика."));
    }

    private static final class Data implements Serializable {

        private static final long serialVersionUID = 1L;

        private final List<Client> clients;
        private final List<Deal> deals;

        Data(List<Client> clients, List<Deal> deals) {
            this.clients = new ArrayList<>(clients);
            this.deals = new ArrayList<>(deals);
        }
    }
}
