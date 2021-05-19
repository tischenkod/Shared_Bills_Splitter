package splitter;

public enum GroupPaymentDirection {
    PURCHASE("purchase"),
    CASHBACK("cashBack");

    String delimiter;

    GroupPaymentDirection(String delimiter) {
        this.delimiter = delimiter;
    }
}
