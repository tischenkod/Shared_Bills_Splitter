package splitter.entities;

import javax.persistence.*;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Entity
public class Person implements Comparable<Person>{
    @Id
    @GeneratedValue
    @Column(name = "person_id")
    int id;

    @Column(nullable = false, unique = true)
    String name;

    @ManyToMany(mappedBy = "members")
    List<Group> groups = new LinkedList<>();

    public Person(String name) {
        if (name == null) {
            throw new InvalidParameterException();
        }
        this.name = name;
    }

    public Person() {
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Person o) {
        return name.compareTo(o.name);
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
