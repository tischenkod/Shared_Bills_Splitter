package splitter.entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class Payment {

    @Id
    @GeneratedValue
    @Column(name = "payment_id")
    int id;

    LocalDate date;

    @ManyToOne
    @JoinColumn(name = "sender")
    Person sender;

    @ManyToOne
    @JoinColumn(name = "receiver")
    Person receiver;

    BigDecimal amount;

    public Payment() {
    }

    public Payment(LocalDate date, Person sender, Person receiver, BigDecimal amount) {
        this.date = date;
        if (sender.getId() < receiver.getId()) {
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
        } else {
            this.sender = receiver;
            this.receiver = sender;
            this.amount = amount.negate();
        }
    }

    public LocalDate getDate() {
        return date;
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
