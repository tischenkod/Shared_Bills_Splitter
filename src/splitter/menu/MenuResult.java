package splitter.menu;

public enum MenuResult {
    MR_BACK,
    MR_NORMAL;

    int stepCount = 1;

    public MenuResult stepCount(int count) {
        stepCount = count;
        return this;
    }
}
