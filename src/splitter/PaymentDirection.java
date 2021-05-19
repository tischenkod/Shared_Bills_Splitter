package splitter;

public enum PaymentDirection {
    BORROW("borrow"),
    REPAY("repay");

    String delimiter;

    PaymentDirection(String delimiter) {
        this.delimiter = delimiter;
    }
}
