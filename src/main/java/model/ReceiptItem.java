package model;

import java.io.Serializable;

// Един ред от касовата бележка: коя стока, на каква цена и колко броя.
public class ReceiptItem implements Serializable {

    private final String productId;
    private final String productName;
    private final double unitPrice;
    private final int quantity;

    public ReceiptItem(String productId, String productName, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return Math.round(unitPrice * quantity * 100.0) / 100.0;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
