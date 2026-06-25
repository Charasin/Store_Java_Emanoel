package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Receipt implements Serializable {

    private final int receiptNumber;
    private final Cashier cashier;
    private final LocalDateTime dateTime;
    private final List<ReceiptItem> items;
    private final double totalAmount;

    public Receipt(int receiptNumber, Cashier cashier, List<ReceiptItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Касовата бележка трябва да съдържа поне една стока.");
        }
        this.receiptNumber = receiptNumber;
        this.cashier = cashier;
        this.dateTime = LocalDateTime.now();
        this.items = new ArrayList<>(items);

        double sum = 0;
        for (ReceiptItem item : items) {
            sum += item.getTotalPrice();
        }
        this.totalAmount = Math.round(sum * 100.0) / 100.0;
    }

    // записва бележката в текстов файл - receipt_<номер>.txt
    public File saveToTextFile(String folderPath) {
        File file = new File(folder(folderPath), "receipt_" + receiptNumber + ".txt");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(toText());
        } catch (IOException e) {
            throw new RuntimeException("Грешка при запис на касова бележка: " + e.getMessage(), e);
        }
        return file;
    }

    // сериализация - записва целия обект в receipt_<номер>.ser
    public File serialize(String folderPath) {
        File file = new File(folder(folderPath), "receipt_" + receiptNumber + ".ser");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException("Грешка при сериализация: " + e.getMessage(), e);
        }
        return file;
    }

    // десериализация - чете обратно обекта от .ser файл
    public static Receipt deserialize(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (Receipt) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Грешка при десериализация: " + e.getMessage(), e);
        }
    }

    // чете текстовата бележка от файл
    public static String readFromTextFile(String filename) {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Грешка при четене на касова бележка: " + e.getMessage(), e);
        }
        return builder.toString();
    }

    // текстовият вид на бележката
    public String toText() {
        StringBuilder b = new StringBuilder();
        b.append("Касова бележка №: ").append(receiptNumber).append(System.lineSeparator());
        b.append("Касиер: ").append(cashier.getName())
                .append(" (ID: ").append(cashier.getId()).append(")").append(System.lineSeparator());
        b.append("Дата и час: ")
                .append(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append(System.lineSeparator());
        b.append("--------------------------------------------------").append(System.lineSeparator());
        b.append(String.format("%-25s %8s %6s %10s%n", "Стока", "Цена", "Брой", "Общо"));
        for (ReceiptItem item : items) {
            b.append(String.format("%-25s %8.2f %6d %10.2f%n",
                    item.getProductName(), item.getUnitPrice(), item.getQuantity(), item.getTotalPrice()));
        }
        b.append("--------------------------------------------------").append(System.lineSeparator());
        b.append(String.format("Обща сума: %.2f лв.%n", totalAmount));
        return b.toString();
    }

    // създава папката за бележки, ако още я няма
    private static File folder(String folderPath) {
        File folder = new File(folderPath == null || folderPath.isBlank() ? "receipts" : folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public int getReceiptNumber() {
        return receiptNumber;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public List<ReceiptItem> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
