package crm.model;

public enum ClientStatus {
    LEAD("Лид"),
    CLIENT("Клиент"),
    PARTNER("Партнёр"),
    INACTIVE("Неактивен");

    private final String label;

    ClientStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
