package splitter.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import splitter.entities.Group;
import splitter.repos.GroupRepository;
import splitter.services.GroupService;

@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    GroupRepository groupRepository;

    @Override
    public Group findGroup(String name) {
        return groupRepository.findGroupByName(name);
    }

    @Override
    public void save(Group group) {
        groupRepository.save(group);
    }

    @Override
    public void remove(Group group) {
        groupRepository.delete(group);
    }
}
