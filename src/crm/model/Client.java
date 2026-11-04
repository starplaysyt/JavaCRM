package crm.model;

import java.io.Serializable;

public final class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String name = "";
    private String company = "";
    private String phone = "";
    private String email = "";
    private ClientStatus status = ClientStatus.LEAD;
    private String notes = "";

    public Client() {
    }

    public Client(long id, String name, String company, String phone, String email,
                  ClientStatus status, String notes) {
        this.id = id;
        this.name = name;
        this.company = company;
        this.phone = phone;
        this.email = email;
        this.status = status;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ClientStatus getStatus() {
        return status;
    }

    public void setStatus(ClientStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        if (company == null || company.isBlank()) {
            return name;
        }
        return name + " : " + company;
    }
}
