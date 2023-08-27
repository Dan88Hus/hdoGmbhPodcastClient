package com.hdogmbh.podcast;

public class ModelVoiceRecord {
    private int id;
    private String filePath;
    private int unitRecordNo;
    private int readBy;
    private Integer ProductId;
    private Float rating;


    public ModelVoiceRecord(Integer ProductId) {
        this.ProductId = ProductId;
    }

    public ModelVoiceRecord(int id) {
        this.id = id;
    }

    public ModelVoiceRecord(int id, Float rating) {
        this.id = id;
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getUnitRecordNo() {
        return unitRecordNo;
    }

    public int getReadBy() {
        return readBy;
    }

    public void setProductId(Integer productId) {
        ProductId = productId;
    }

    public int getProductId() {
        return ProductId;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }
}
