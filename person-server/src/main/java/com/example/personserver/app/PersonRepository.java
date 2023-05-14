package com.example.personserver.app;

import java.util.List;

public interface PersonRepository {
    List<Person> getAllPeople();
    Person getPersonById(int id);
    void addPerson(Person person);
    void updatePerson(Person updatedPerson);
    void deletePerson(int id);
    int peopleInSystem();
}
