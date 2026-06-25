package model;

import java.io.Serializable;

// на една каса работи един касиер
public class CashRegister implements Serializable {

    private final String id;
    private final Cashier cashier;

    public CashRegister(String id, Cashier cashier) {
        this.id = id;
        this.cashier = cashier;
    }

    public String getId() {
        return id;
    }

    public Cashier getCashier() {
        return cashier;
    }
}
