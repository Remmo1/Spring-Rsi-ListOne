package com.example.personserver.app;

import lombok.AllArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/people")
@AllArgsConstructor
public class PersonController {
    private final PersonRepository personRepository;

    @GetMapping
    public CollectionModel<EntityModel<Person>> getAllPeople() {
        List<EntityModel<Person>> people = personRepository.getAllPeople().stream().map(this::getPersonResource).toList();
        Link selfLink = WebMvcLinkBuilder.linkTo(this.getClass()).withSelfRel();
        return CollectionModel.of(people, selfLink);
    }

    @GetMapping("/{id}")
    public EntityModel<Person> getPersonById(@PathVariable int id) {
        Person person = personRepository.getPersonById(id);
        if (person != null) {
            return getPersonResource(person);
        } else {
            throw new PersonNotFoundException("Person not found with ID: " + id);
        }
    }

    @PostMapping
    public EntityModel<Person> addPerson(@RequestBody Person person) {
        personRepository.addPerson(person);
        return getPersonResource(person);
    }

    @PutMapping("/{id}")
    public EntityModel<Person> updatePerson(@PathVariable int id, @RequestBody Person updatedPerson) {
        personRepository.updatePerson(updatedPerson);
        return getPersonResource(personRepository.getPersonById(id));
    }

    @DeleteMapping("/{id}")
    public void deletePerson(@PathVariable int id) {
        personRepository.deletePerson(id);
    }

    @GetMapping("/size")
    public ResponseEntity<Integer> getSize() {
        return ResponseEntity.ok(personRepository.peopleInSystem());
    }

    private EntityModel<Person> getPersonResource(Person person) {
        Link selfLink = WebMvcLinkBuilder.linkTo(PersonController.class).slash(person.getId()).withSelfRel();
        Link updateLink = WebMvcLinkBuilder.linkTo(PersonController.class).slash(person.getId()).withRel("update");
        Link deleteLink = WebMvcLinkBuilder.linkTo(PersonController.class).slash(person.getId()).withRel("delete");
        Link everybodyLink = WebMvcLinkBuilder.linkTo(PersonController.class).withRel("everybody");
        return EntityModel.of(person, selfLink, updateLink, deleteLink, everybodyLink);
    }
}
