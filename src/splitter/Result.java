package splitter;

public enum Result {
    OK("Ok"),
    UNKNOWN_COMMAND("Unknown command. Print help to show commands list"),
    ILLEGAL_ARGUMENT("Illegal command arguments");

    String message;

    Result(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}
