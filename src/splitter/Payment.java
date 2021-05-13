package splitter;

import java.time.LocalDate;

public class Payment {
    LocalDate date;
    PersonPair pair;
    int amount;

    public Payment(LocalDate date, Person sender, Person receiver, int amount) {
        this.date = date;
        if (sender.id < receiver.id) {
            this.pair = new PersonPair(sender, receiver);
            this.amount = amount;
        } else {
            this.pair = new PersonPair(receiver, sender);
            this.amount = -amount;
        }
    }
}
