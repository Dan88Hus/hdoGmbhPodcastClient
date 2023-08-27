package com.hdogmbh.podcast;

public class ModelProduct {
    private int id;
    private float unit_price;
    private String to_whome; // name
    private String purpose; // demanding purpose
    private String description; // demander description
    private int number_read; // number to be read
    private String uniqueId; // order ID uniqueId

    public ModelProduct(String to_whome, String purpose, String description, int number_read) {
        // unit_price will be collected from database
        this.to_whome = to_whome;
        this.purpose = purpose;
        this.description = description;
        this.number_read = number_read;
    }

    public ModelProduct(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public int getId() {
        return id;
    }

    public float getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(float unit_price) {
        this.unit_price = unit_price;
    }

    public String getTo_whome() {
        return to_whome;
    }

    public void setTo_whome(String to_whome) {
        this.to_whome = to_whome;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumber_read() {
        return number_read;
    }

    public void setNumber_read(int number_read) {
        this.number_read = number_read;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
