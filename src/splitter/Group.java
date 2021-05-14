package splitter;

import java.util.*;
import java.util.stream.Stream;

public class Group implements Iterable<Person>{
    String name;
    List<Person> members;

    public Group(String name) {
        this.name = name;
        members = new LinkedList<>();

    }

    public void add(Person member) {
        members.add(member);
        Collections.sort(members);
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
}
