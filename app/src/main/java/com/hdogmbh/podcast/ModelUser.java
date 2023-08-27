package com.hdogmbh.podcast;

public class ModelUser {
    private int id;
    private String email;
    private String name;
    private String surname;
    private String uniqueId;

    public ModelUser(String email, String name, String surname, String uniqueId) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.uniqueId = uniqueId;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
