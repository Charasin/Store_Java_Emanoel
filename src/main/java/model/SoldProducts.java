package model;

import java.io.Serializable;

// Обобщение колко общо е продадено от дадена стока и за каква сума.
public class SoldProducts implements Serializable {

    private final String productId;
    private final String productName;
    private int quantity;
    private double totalPrice;

    public SoldProducts(String productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    /** Добавя още една продажба към обобщението. */
    public void add(int amount, double lineTotal) {
        this.quantity += amount;
        this.totalPrice = Math.round((this.totalPrice + lineTotal) * 100.0) / 100.0;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
