package splitter.menu;

public class DynamicMenuItem extends ListMenuItem {
    EventHandler onEnter;

    public void setOnEnter(EventHandler onEnter) {
        this.onEnter = onEnter;
    }

    public DynamicMenuItem(int key, String name, EventHandler onEnter) {
        super(key, name);
        this.onEnter = onEnter;
    }

    @Override
    public MenuResult enter() {
        try {
            if (onEnter != null) {
                onEnter.handle(this);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return MenuResult.MR_NORMAL;
        }
        return super.enter();
    }
}
