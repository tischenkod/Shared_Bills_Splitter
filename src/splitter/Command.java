package splitter;

public enum Command {
    BALANCE("balance", "balance"),
    BORROW("borrow", "borrow"),
    CASHBACK("cashBack", "cashBack"),
    EXIT("exit", "exit"),
    GROUP("group", "group\\s(create|add|remove|show)"),
    PURCHASE("purchase", "purchase"),
    REPAY("repay", "repay"),
    SECRET_SANTA("secretSanta", "secretSanta"),
    WRITE_OFF("writeOff", "writeOff"),
    HELP("help", "help");

    String keyWord;
    String signature;

    Command(String keyWord, String signature) {
        this.keyWord = keyWord;
        this.signature = signature;
    }

    @Override
    public String toString() {
        return keyWord;
    }

    public static Command recognise(String command) {
        for (Command item: Command.values()) {
            if (command.matches(".*" + item.signature + ".*")) {
                return item;
            }
        }
        return null;
    }

}
