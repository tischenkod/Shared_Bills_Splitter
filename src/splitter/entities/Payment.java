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

//    @Transient
//    PersonPair pair;

    BigDecimal amount;

    public Payment() {
    }

    public Payment(LocalDate date, Person sender, Person receiver, BigDecimal amount) {
        this.date = date;
        if (sender.getId() < receiver.getId()) {
//            this.pair = new PersonPair(sender, receiver);
            this.sender = sender;
            this.receiver = receiver;
            this.amount = amount;
        } else {
//            this.pair = new PersonPair(receiver, sender);
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
//    public PersonPair getPair() {
//        if (pair == null) {
//            try {
//                pair = new PersonPair(sender, receiver);
//            } catch (InvalidParameterException e) {
//                return null;
//            }
//        }
//        return pair;
//    }
//
//    public void setPair(PersonPair pair) {
//        this.pair = pair;
//        sender = pair.sender;
//        receiver = pair.receiver;
//    }

    public BigDecimal getAmount() {
        return amount;
    }
}
