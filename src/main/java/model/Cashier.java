package model;

import java.io.Serializable;

public class Cashier implements Serializable {

    private final String id;
    private final String name;
    private final double salary;

    public Cashier(String id, String name, double salary) {
        if (salary < 0) {
            throw new IllegalArgumentException("Заплатата не може да бъде отрицателна.");
        }
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getSalary() {
        return salary;
    }
}
