package splitter.repos;

import org.springframework.data.repository.PagingAndSortingRepository;
import splitter.entities.Person;

public interface PersonRepository extends PagingAndSortingRepository<Person, Integer> {
    Person findPersonByName(String name);
}
