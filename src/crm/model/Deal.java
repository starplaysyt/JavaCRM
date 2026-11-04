package crm.model;

import java.io.Serializable;
import java.time.LocalDate;

public final class Deal implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String title = "";
    private long clientId;
    private double amount;
    private DealStage stage = DealStage.NEW;
    private LocalDate expectedClose;
    private String notes = "";

    public Deal() {
    }

    public Deal(long id, String title, long clientId, double amount, DealStage stage,
                LocalDate expectedClose, String notes) {
        this.id = id;
        this.title = title;
        this.clientId = clientId;
        this.amount = amount;
        this.stage = stage;
        this.expectedClose = expectedClose;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public DealStage getStage() {
        return stage;
    }

    public void setStage(DealStage stage) {
        this.stage = stage;
    }

    public LocalDate getExpectedClose() {
        return expectedClose;
    }

    public void setExpectedClose(LocalDate expectedClose) {
        this.expectedClose = expectedClose;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
