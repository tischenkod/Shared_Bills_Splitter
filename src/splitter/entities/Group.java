package splitter.entities;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Stream;

@Entity(name = "groups")
public class Group implements Iterable<Person>{

    @Id
    @GeneratedValue
    @Column(name = "group_id")
    int id;

    @Column(name = "group_name")
    String name;

    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name = "membership",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "person_id"))
    List<Person> members = new LinkedList<>();

    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    public void add(Person member) {
        members.add(member);
    }

    public int size() {
        return members.size();
    }

    @Override
    public Iterator<Person> iterator() {
        return members.iterator();
    }

    public Stream<Person> stream() {
        return members.stream();
    }

    public void addAll(Collection<Person> c) {
        members.addAll(c);
    }

    public void removeAll(Collection<Person> c) {
        members.removeAll(c);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public List<Person> getMembers() {
        return members;
    }
}
