package crm.model;

public enum DealStage {
    NEW("Новая"),
    NEGOTIATION("Переговоры"),
    PROPOSAL("Предложение"),
    PAYMENT("Оплата"),
    WON("Закрыта"),
    LOST("Проиграна");

    private final String label;

    DealStage(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isActive() {
        return this != WON && this != LOST;
    }

    @Override
    public String toString() {
        return label;
    }
}
