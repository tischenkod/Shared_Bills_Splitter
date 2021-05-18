package splitter.repos;

import org.springframework.data.repository.PagingAndSortingRepository;
import splitter.entities.Group;

public interface GroupRepository extends PagingAndSortingRepository<Group, Integer> {
    Group findGroupByName(String name);
}
