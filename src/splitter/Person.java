package splitter;

import java.security.InvalidParameterException;
import java.util.Objects;

public class Person implements Comparable<Person>{
    static int nextId = 0;

    int id;
    String name;

    public Person(String name) {
        if (name == null) {
            throw new InvalidParameterException();
        }
        id = nextId++;
        this.name = name;
    }

    @Override
    public int compareTo(Person o) {
        return Integer.compare(id, o.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id && name.equals(person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
