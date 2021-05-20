package splitter;

public enum BalanceType {
    OPEN("open"),
    CLOSE("close");

    String keyword;


    BalanceType(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return keyword;
    }
}
