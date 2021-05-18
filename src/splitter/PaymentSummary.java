package splitter;

import splitter.entities.Person;

import java.math.BigDecimal;

public class PaymentSummary {
    Person sender;
    Person receiver;
    BigDecimal amount;

    public PaymentSummary(Person sender, Person receiver, BigDecimal amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public PaymentSummary() {
    }

    public Person getSender() {
        return sender;
    }

    public Person getReceiver() {
        return receiver;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
