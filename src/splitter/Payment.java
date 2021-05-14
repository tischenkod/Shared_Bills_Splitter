package splitter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {
    LocalDate date;
    PersonPair pair;
    BigDecimal amount;

    public Payment(LocalDate date, Person sender, Person receiver, BigDecimal amount) {
        this.date = date;
        if (sender.id < receiver.id) {
            this.pair = new PersonPair(sender, receiver);
            this.amount = amount;
        } else {
            this.pair = new PersonPair(receiver, sender);
            this.amount = amount.negate();
        }
    }
}
