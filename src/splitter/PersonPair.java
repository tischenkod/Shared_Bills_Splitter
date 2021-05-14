package splitter;

import java.util.Comparator;
import java.util.Objects;

public class PersonPair implements Comparable<PersonPair>{
    Person sender;
    Person receiver;

    public PersonPair(PersonPair pair) {
        sender = pair.sender;
        receiver = pair.receiver;
    }

    public Person getSender() {
        return sender;
    }

    public Person getReceiver() {
        return receiver;
    }

    public PersonPair(Person sender, Person receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public int compareTo(PersonPair p) {
        return Comparator.comparing(PersonPair::getReceiver).thenComparing(PersonPair::getSender).compare(this, p);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonPair that = (PersonPair) o;
        return sender.equals(that.sender) && receiver.equals(that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, receiver);
    }

    public void swap() {
        Person temp = sender;
        sender = receiver;
        receiver = temp;
    }
}
