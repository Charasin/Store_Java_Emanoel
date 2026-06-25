package service;

import exception.InsufficientPaymentException;
import exception.InsufficientStockException;
import exception.ProductNotFoundException;
import model.CashRegister;
import model.Cashier;
import model.Product;
import model.Receipt;
import model.ReceiptItem;
import model.SoldProducts;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Store {

    private final List<Product> products = new ArrayList<>();
    private final List<Cashier> cashiers = new ArrayList<>();
    private final List<CashRegister> cashRegisters = new ArrayList<>();
    private final List<Receipt> receipts = new ArrayList<>();
    private final Map<String, SoldProducts> soldProducts = new LinkedHashMap<>();

    private final String receiptsFolder;
    private int nextReceiptNumber = 1;

    public Store() {
        this("receipts");
    }

    public Store(String receiptsFolder) {
        this.receiptsFolder = (receiptsFolder == null || receiptsFolder.isBlank()) ? "receipts" : receiptsFolder;
    }

    public void addProduct(Product product) {
        Product existing = findProduct(product.getId());
        if (existing != null) {
            existing.increaseQuantity(product.getQuantity()); // същата стока - добавяме количество
        } else {
            products.add(product);
        }
    }

    public void addCashier(Cashier cashier) {
        if (findCashier(cashier.getId()) != null) {
            throw new IllegalArgumentException("Вече има касиер с ID: " + cashier.getId());
        }
        cashiers.add(cashier);
    }

    public void addCashRegister(CashRegister cashRegister) {
        if (findCashier(cashRegister.getCashier().getId()) == null) {
            addCashier(cashRegister.getCashier());
        }
        cashRegisters.add(cashRegister);
    }

    // продажба на стоки през дадена каса
    public Receipt sellProducts(CashRegister cashRegister, Map<String, Integer> productQuantities, double clientMoney) {
        if (productQuantities == null || productQuantities.isEmpty()) {
            throw new IllegalArgumentException("Трябва да има поне една стока за продажба.");
        }

        // първо проверяваме всичко и събираме редовете на бележката
        List<ReceiptItem> items = new ArrayList<>();
        double total = 0;
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            String productId = entry.getKey();
            int requested = entry.getValue();

            Product product = findProduct(productId);
            if (product == null) {
                throw new ProductNotFoundException("Не е намерена стока с ID: " + productId);
            }
            if (requested > product.getQuantity()) {
                int missing = requested - product.getQuantity();
                throw new InsufficientStockException("Недостатъчно количество за стока: " + product.getName()
                        + ". Търсено: " + requested + ", налично: " + product.getQuantity() + ", не достига: " + missing);
            }

            double price = product.getSellingPrice(); // хвърля грешка ако срокът е изтекъл
            ReceiptItem item = new ReceiptItem(product.getId(), product.getName(), price, requested);
            items.add(item);
            total += item.getTotalPrice();
        }
        total = Math.round(total * 100.0) / 100.0;

        // клиентът трябва да има достатъчно пари
        if (clientMoney < total) {
            throw new InsufficientPaymentException(String.format(
                    "Недостатъчно пари от клиента. Дължима сума: %.2f лв., платени: %.2f лв., не достигат: %.2f лв.",
                    total, clientMoney, total - clientMoney));
        }

        // всичко е наред - намаляваме наличностите
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            findProduct(entry.getKey()).reduceQuantity(entry.getValue());
        }

        // издаваме и записваме бележката
        Receipt receipt = new Receipt(nextReceiptNumber, cashRegister.getCashier(), items);
        nextReceiptNumber++;
        receipt.saveToTextFile(receiptsFolder);
        receipt.serialize(receiptsFolder);
        receipts.add(receipt);
        registerSoldProducts(items);
        return receipt;
    }

    // добавя продажбата към обобщението "продадени стоки"
    private void registerSoldProducts(List<ReceiptItem> items) {
        for (ReceiptItem item : items) {
            SoldProducts sold = soldProducts.get(item.getProductId());
            if (sold == null) {
                sold = new SoldProducts(item.getProductId(), item.getProductName());
                soldProducts.put(item.getProductId(), sold);
            }
            sold.add(item.getQuantity(), item.getTotalPrice());
        }
    }

    private Product findProduct(String productId) {
        for (Product product : products) {
            if (product.getId().equals(productId)) {
                return product;
            }
        }
        return null;
    }

    private Cashier findCashier(String cashierId) {
        for (Cashier cashier : cashiers) {
            if (cashier.getId().equals(cashierId)) {
                return cashier;
            }
        }
        return null;
    }

    // разходи = заплати + стойност на доставените стоки
    public double calculateExpenses() {
        double salaries = 0;
        for (Cashier cashier : cashiers) {
            salaries += cashier.getSalary();
        }
        double deliveries = 0;
        for (Product product : products) {
            deliveries += product.getPurchasePrice() * product.getDeliveredQuantity();
        }
        return Math.round((salaries + deliveries) * 100.0) / 100.0;
    }

    // приходи = оборот = сумата на всички бележки
    public double calculateRevenue() {
        double revenue = 0;
        for (Receipt receipt : receipts) {
            revenue += receipt.getTotalAmount();
        }
        return Math.round(revenue * 100.0) / 100.0;
    }

    // печалба = приходи - разходи
    public double calculateProfit() {
        return Math.round((calculateRevenue() - calculateExpenses()) * 100.0) / 100.0;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Cashier> getCashiers() {
        return cashiers;
    }

    public List<CashRegister> getCashRegisters() {
        return cashRegisters;
    }

    public List<Receipt> getReceipts() {
        return receipts;
    }

    public Map<String, SoldProducts> getSoldProductsMap() {
        return soldProducts;
    }

    public int getReceiptCount() {
        return receipts.size();
    }

    public Product findProductById(String productId) {
        return findProduct(productId);
    }
}
