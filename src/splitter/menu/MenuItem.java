package splitter.menu;

import java.util.Map;

import static java.lang.String.format;

public abstract class MenuItem {
    public MenuItem setData(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    public Map<String, Object> data;
    int key;
    String name;

    public MenuItem(int key, String name) {
        this.name = name;
        this.key = key;
    }

    @Override
    public String toString() {
        return format("%d. %s", key, name);
    }

    abstract MenuResult enter();

}
