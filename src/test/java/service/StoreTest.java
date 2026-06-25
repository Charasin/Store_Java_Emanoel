package service;

import exception.ExpiredProductException;
import exception.InsufficientPaymentException;
import exception.InsufficientStockException;
import model.CashRegister;
import model.Cashier;
import model.Product;
import model.ProductCategory;
import model.Receipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StoreTest {
    @TempDir
    Path tempDir;

    private Store store;
    private CashRegister cashRegister;

    @BeforeEach
    void setup() {
        store = new Store(tempDir.toString());
        Cashier cashier = new Cashier("C001", "Мария Петрова", 1500.0);
        cashRegister = new CashRegister("R001", cashier);
        store.addCashRegister(cashRegister);
        store.addProduct(new Product("P001", "Хляб", 1.0, ProductCategory.FOOD, LocalDate.now().plusDays(10), 10));
        store.addProduct(new Product("P002", "Мляко", 2.0, ProductCategory.FOOD, LocalDate.now().plusDays(1), 5));
    }

    @Test
    void addingProductWithExistingIdIncreasesQuantity() {
        store.addProduct(new Product("P001", "Хляб", 1.0, ProductCategory.FOOD, LocalDate.now().plusDays(10), 3));

        assertEquals(13, store.findProductById("P001").getQuantity());
        assertEquals(13, store.findProductById("P001").getDeliveredQuantity());
    }

    @Test
    void saleCreatesReceiptFilesAndUpdatesRevenue() {
        Map<String, Integer> sale = new LinkedHashMap<>();
        sale.put("P001", 2);
        sale.put("P002", 1);

        Receipt receipt = store.sellProducts(cashRegister, sale, 20.0);

        assertNotNull(receipt);
        assertEquals(1, store.getReceiptCount());
        assertEquals(8, store.findProductById("P001").getQuantity());
        assertEquals(4, store.findProductById("P002").getQuantity());
        assertEquals(4.44, store.calculateRevenue(), 0.01);

        File txt = tempDir.resolve("receipt_1.txt").toFile();
        File ser = tempDir.resolve("receipt_1.ser").toFile();
        assertTrue(txt.exists());
        assertTrue(ser.exists());

        String text = Receipt.readFromTextFile(txt.getAbsolutePath());
        assertTrue(text.contains("Касова бележка №: 1"));
        assertTrue(text.contains("Хляб"));

        Receipt deserialized = Receipt.deserialize(ser.getAbsolutePath());
        assertEquals(receipt.getTotalAmount(), deserialized.getTotalAmount(), 0.01);
    }

    @Test
    void saleWithInsufficientClientMoneyThrowsException() {
        Map<String, Integer> sale = new LinkedHashMap<>();
        sale.put("P001", 2);

        assertThrows(InsufficientPaymentException.class, () -> store.sellProducts(cashRegister, sale, 1.0));
    }

    @Test
    void saleWithInsufficientStockThrowsException() {
        Map<String, Integer> sale = new LinkedHashMap<>();
        sale.put("P001", 20);

        InsufficientStockException exception =
                assertThrows(InsufficientStockException.class, () -> store.sellProducts(cashRegister, sale, 100.0));
        assertTrue(exception.getMessage().contains("не достига: 10"));
    }

    @Test
    void expiredProductCannotBeSold() {
        store.addProduct(new Product("P003", "Стар продукт", 2.0, ProductCategory.FOOD, LocalDate.now().minusDays(1), 2));
        Map<String, Integer> sale = new LinkedHashMap<>();
        sale.put("P003", 1);

        assertThrows(ExpiredProductException.class, () -> store.sellProducts(cashRegister, sale, 10.0));
    }

    @Test
    void expensesRevenueAndProfitAreCalculated() {
        Map<String, Integer> sale = new LinkedHashMap<>();
        sale.put("P001", 1);

        store.sellProducts(cashRegister, sale, 10.0);

        assertEquals(1520.0, store.calculateExpenses(), 0.01);
        assertEquals(1.20, store.calculateRevenue(), 0.01);
        assertEquals(-1518.80, store.calculateProfit(), 0.01);
        assertFalse(store.getSoldProductsMap().isEmpty());
    }
}
