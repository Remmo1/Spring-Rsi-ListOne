package com.example.personserver.app;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class PersonRepositoryImpl implements PersonRepository {
    private final List<Person> people;

    public PersonRepositoryImpl() {
        people = new ArrayList<>();
        // Initialize with some data
        people.add(new Person(1, "Maria", 22, "maria@example.com"));
        people.add(new Person(2, "Remigiusz", 22, "remek@example.com"));
        people.add(new Person(3, "Izabela", 20, "iza@example.com"));
    }

    public List<Person> getAllPeople() {
        log.info("Getting all people");
        return people;
    }

    public Person getPersonById(int id) {
        log.info("Getting person with id {}", id);
        var found = people.stream().filter(p -> p.getId() == id).findAny();
        if (found.isPresent())
            return found.get();
        else
            throw new PersonNotFoundException("Person with id " + id + " doesn't exists!");
    }

    public void addPerson(Person person) {
        boolean personExists = people.stream()
                .anyMatch(
                        p -> p.getName().equals(person.getName()) &&
                        p.getAge() == person.getAge() &&
                        p.getEmail().equals(person.getEmail())
                );

        if (personExists) {
            throw new PersonAlreadyExistsException("Ta osoba już istnieje w systemie!");
        }

        var id = people.size();
        person.setId(id + 1);
        people.add(person);
    }


    public void updatePerson(Person updatedPerson) {
        log.info("Updating person with id {}", updatedPerson.getId());
        var found = people.stream().filter(p -> p.getId() == updatedPerson.getId()).findAny();
        if (found.isPresent()) {
            var person = found.get();
            person.setName(updatedPerson.getName());
            person.setAge(updatedPerson.getAge());
            person.setEmail(updatedPerson.getEmail());
        } else {
            throw new PersonNotFoundException("Person with id " + updatedPerson.getId() + " doesn't exists!");
        }
    }

    public void deletePerson(int id) {
        log.info("Deleting person with id {}", id);
        var found = people.stream().filter(p -> p.getId() == id).findAny();
        if (found.isPresent()) {
            people.remove(found.get());
        } else {
            throw new PersonNotFoundException("Person with id " + id + " doesn't exists!");
        }
    }

    public int peopleInSystem() {
        return people.size();
    }
}

