package splitter;

public enum Direction {
    BORROW("borrow"),
    REPLAY("repay");

    String delimiter;

    Direction(String delimiter) {
        this.delimiter = delimiter;
    }
}
