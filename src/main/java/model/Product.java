package model;

import exception.ExpiredProductException;
import exception.InsufficientStockException;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Product implements Serializable {

    // надценка и намаление - тук се сменят процентите
    public static final double FOOD_MARKUP = 0.20;
    public static final double NON_FOOD_MARKUP = 0.30;
    public static final int DISCOUNT_DAYS = 3;
    public static final double DISCOUNT = 0.15;

    private final String id;
    private final String name;
    private final double purchasePrice;
    private final ProductCategory category;
    private final LocalDate expirationDate;
    private int quantity;            // налично количество
    private int deliveredQuantity;   // общо доставено (за разходите)

    public Product(String id, String name, double purchasePrice, ProductCategory category,
                   LocalDate expirationDate, int quantity) {
        if (purchasePrice < 0) {
            throw new IllegalArgumentException("Доставната цена не може да бъде отрицателна.");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("Количеството не може да бъде отрицателно.");
        }
        this.id = id;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.category = category;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
        this.deliveredQuantity = quantity;
    }

    // продажна цена = доставна + надценка, с намаление при наближаващ срок
    public double getSellingPrice() {
        if (isExpired()) {
            throw new ExpiredProductException("Стоката е с изтекъл срок и не може да бъде продадена: " + name);
        }

        double markup = (category == ProductCategory.FOOD) ? FOOD_MARKUP : NON_FOOD_MARKUP;
        double price = purchasePrice * (1 + markup);

        if (isCloseToExpiration()) {
            price = price * (1 - DISCOUNT);
        }
        return round(price);
    }

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDate.now());
    }

    public boolean isCloseToExpiration() {
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
        return daysLeft >= 0 && daysLeft < DISCOUNT_DAYS;
    }

    // доставка на още количество
    public void increaseQuantity(int amount) {
        quantity += amount;
        deliveredQuantity += amount;
    }

    // продажба - хвърля грешка ако няма достатъчно
    public void reduceQuantity(int amount) {
        if (amount > quantity) {
            int missing = amount - quantity;
            throw new InsufficientStockException("Недостатъчно количество за стока: " + name
                    + ". Търсено: " + amount + ", налично: " + quantity + ", не достига: " + missing);
        }
        quantity -= amount;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getDeliveredQuantity() {
        return deliveredQuantity;
    }

    @Override
    public String toString() {
        return id + " - " + name + " (" + category.getDisplayName() + ", " + quantity + " бр.)";
    }
}
