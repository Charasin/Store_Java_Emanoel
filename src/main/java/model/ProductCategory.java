package model;

public enum ProductCategory {
    FOOD("Хранителна"),
    NON_FOOD("Нехранителна");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
