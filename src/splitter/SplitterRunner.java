package splitter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SplitterRunner implements CommandLineRunner {
    @Autowired
    Splitter splitter;

    @Override
    public void run(String... args) {
        splitter.run();
    }
}
