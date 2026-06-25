import model.CashRegister;
import model.Cashier;
import model.Product;
import model.ProductCategory;
import model.Receipt;
import service.Store;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Store store = new Store();

        // Касиер и каса
        Cashier cashier = new Cashier("C001", "Иван Иванов", 1800.00);
        CashRegister cashRegister = new CashRegister("R001", cashier);
        store.addCashRegister(cashRegister);

        // Доставени стоки
        store.addProduct(new Product("P001", "Хляб", 1.20, ProductCategory.FOOD, LocalDate.now().plusDays(5), 30));
        store.addProduct(new Product("P002", "Мляко", 2.00, ProductCategory.FOOD, LocalDate.now().plusDays(1), 20));
        store.addProduct(new Product("P003", "Сапун", 1.50, ProductCategory.NON_FOOD, LocalDate.now().plusYears(2), 50));

        System.out.println("Налични стоки:");
        for (Product product : store.getProducts()) {
            System.out.println("  " + product + " -> продажна цена: " + product.getSellingPrice() + " лв.");
        }

        // Примерна продажба: 2 хляба и 1 мляко, клиентът дава 50 лв.
        Map<String, Integer> sale = new LinkedHashMap<>();
        sale.put("P001", 2);
        sale.put("P002", 1);
        Receipt receipt = store.sellProducts(cashRegister, sale, 50.00);

        System.out.println();
        System.out.println("Издадена касова бележка № " + receipt.getReceiptNumber());
        System.out.println(receipt.toText());

        System.out.printf("Оборот (приходи): %.2f лв.%n", store.calculateRevenue());
        System.out.printf("Разходи:          %.2f лв.%n", store.calculateExpenses());
        System.out.printf("Печалба:          %.2f лв.%n", store.calculateProfit());
        System.out.println("Бележките са записани в папка receipts/ като .txt и .ser файлове.");
    }
}
