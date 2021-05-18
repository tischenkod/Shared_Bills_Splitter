package splitter.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import splitter.entities.Person;
import splitter.repos.PersonRepository;
import splitter.services.PersonService;

@Service
public class PersonServiceImpl implements PersonService {
    @Autowired
    PersonRepository personRepository;

    @Override
    public Person getPerson(String name) {
        Person result = personRepository.findPersonByName(name);
        if (result == null) {
            result = new Person(name);
            personRepository.save(result);
        }
        return result;
    }
}
