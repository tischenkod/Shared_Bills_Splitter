package splitter.menu;

@FunctionalInterface
public interface GetCaptionEvent {
    String handle(MenuItem sender);
}
