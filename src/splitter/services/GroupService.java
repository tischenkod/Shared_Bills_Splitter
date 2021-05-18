package splitter.services;

import splitter.entities.Group;

public interface GroupService {
    Group findGroup(String name);

    void add(Group group);

    void remove(Group group);
}
