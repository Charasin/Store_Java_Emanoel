package model;

import exception.ExpiredProductException;
import exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProductTest {
    @Test
    void foodProductSellingPriceUsesFoodMarkup() {
        Product product = new Product("P001", "Хляб", 10.0, ProductCategory.FOOD, LocalDate.now().plusDays(10), 5);

        assertEquals(12.0, product.getSellingPrice(), 0.01);
    }

    @Test
    void nonFoodProductSellingPriceUsesNonFoodMarkup() {
        Product product = new Product("P002", "Сапун", 10.0, ProductCategory.NON_FOOD, LocalDate.now().plusDays(100), 5);

        assertEquals(13.0, product.getSellingPrice(), 0.01);
    }

    @Test
    void closeToExpirationProductReceivesDiscount() {
        Product product = new Product("P003", "Мляко", 10.0, ProductCategory.FOOD, LocalDate.now().plusDays(1), 5);

        assertEquals(10.20, product.getSellingPrice(), 0.01);
    }

    @Test
    void expiredProductCannotBeSold() {
        Product product = new Product("P004", "Кисело мляко", 10.0, ProductCategory.FOOD, LocalDate.now().minusDays(1), 5);

        assertThrows(ExpiredProductException.class, product::getSellingPrice);
    }

    @Test
    void reducingMoreThanAvailableThrowsExceptionWithMissingQuantity() {
        Product product = new Product("P005", "Ориз", 10.0, ProductCategory.FOOD, LocalDate.now().plusDays(20), 2);

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> product.reduceQuantity(5));
        assertTrue(exception.getMessage().contains("не достига: 3"));
    }

    @Test
    void negativeQuantityIsNotAllowed() {
        assertThrows(IllegalArgumentException.class,
                () -> new Product("P006", "Грешна стока", 10.0, ProductCategory.FOOD, LocalDate.now().plusDays(1), -1));
    }
}
