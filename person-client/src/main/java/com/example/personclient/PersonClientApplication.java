package com.example.personclient;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class PersonClientApplication {

	private static final Scanner scanner = new Scanner(System.in);
	private static final String BASE_URL = "http://localhost:8080/people";
	private static final RestTemplate restTemplate = new RestTemplate();

	public static void main(String[] args) {
		try {
			MyData.myInfo();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		displayMenu();
		int choice = getUserChoice("Twój wybór: ");

		while (choice != 0) {
			switch (choice) {
				case 1:
					addPerson();
					break;
				case 2:
					getAllPeople();
					break;
				case 3:
					getPersonById();
					break;
				case 4:
					updatePerson();
					break;
				case 5:
					deletePerson();
					break;
				case 6:
					getAmountOfPeople();
					break;
				default:
					System.out.println("Zły wybór. Spróbuj ponownie");
			}

			displayMenu();
			choice = getUserChoice("Twój wybór: ");
		}
	}

	private static void displayMenu() {
		System.out.println("\n===================");
		System.out.println("=======  System =====");
		System.out.println("=====================");
		System.out.println("1 - Dodaj osobę");
		System.out.println("2 - Pokaż wszystkich");
		System.out.println("3 - Znajdź osobę po ID");
		System.out.println("4 - Zmień dane osoby");
		System.out.println("5 - Usuń osobę");
		System.out.println("6 - Liczba osób w systemie");
		System.out.println("0 - Wyjście");
		System.out.println("=====================");
	}

	private static int getUserChoice(String message) {
		while (true) {
			try {
				System.out.print(message);
				return scanner.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Nieprawidłowe dane. Spróbuj ponownie.");
				scanner.nextLine(); // Clear the input buffer
			}
		}
	}


	private static void addPerson() {
		try {
			scanner.nextLine(); // Consume newline character
			System.out.print("Wpisz imię: ");
			String name = scanner.nextLine();
			System.out.print("Wpisz wiek: ");
			int age = scanner.nextInt();
			scanner.nextLine(); // Consume newline character
			System.out.print("Wpisz adres email: ");
			String email = scanner.nextLine();

			Person newPerson = new Person(0, name, age, email);
			HttpEntity<Person> request = new HttpEntity<>(newPerson);
			ResponseEntity<Person> response = restTemplate.postForEntity(BASE_URL, request, Person.class);
			Person createdPerson = response.getBody();
			System.out.println("Osoba dodana pomyślnie");
			System.out.println(createdPerson);
		} catch (HttpClientErrorException.NotAcceptable ex) {
			System.out.println("Podana osoba już istnieje!");
		} catch (RestClientException ex) {
			System.out.println("Wystąpił błąd podczas komunikacji z serwerem: " + ex.getMessage());
		}
	}


	private static void getAllPeople() {
		Traverson traverson = new Traverson(URI.create(BASE_URL), MediaTypes.HAL_JSON);
		Traverson.TraversalBuilder builder = traverson.follow("self");
		CollectionModel<EntityModel<Person>> people = builder.toObject(new ParameterizedTypeReference<>() {});

		if (people != null && !people.getContent().isEmpty()) {
			System.out.println("Osoby zapisane w systemie:");
			for (EntityModel<Person> personResource : people.getContent()) {
				Person person = personResource.getContent();
				System.out.println(person);
			}

			// Retrieve links from the response
			List<Link> links = people.getLinks().stream().toList();
			System.out.println("Linki:");
			for (Link link : links)
				System.out.println(link.getRel() + " -> " + link.getHref());
		} else {
			System.out.println("Brak osób w systemie.");
		}
	}
	private static void getPersonById() {
		int id = getUserChoice("Podaj ID szukanej osoby: ");

		try {
			Traverson traverson = new Traverson(URI.create(BASE_URL + "/" + id), MediaTypes.HAL_JSON);
			Traverson.TraversalBuilder builder = traverson.follow("self");
			EntityModel<Person> response = builder.toObject(new ParameterizedTypeReference<>() {});

			if (response != null) {
				Person person = response.getContent();
				System.out.println("Znaleziono osobę: " + person);

				List<Link> links = response.getLinks().stream().toList();
				System.out.println("Linki:");
				for (Link link : links)
					System.out.println(link.getRel() + " -> " + link.getHref());
			} else {
				System.out.println("Nie znaleziono osoby o podanym id: " + id);
			}
		} catch (HttpClientErrorException.NotFound ex) {
			System.out.println("Nie znaleziono osoby o podanym id: " + id);
		} catch (RestClientException ex) {
			System.out.println("Wystąpił błąd podczas komunikacji z serwerem: " + ex.getMessage());
		}
	}

	private static void updatePerson() {
		System.out.print("Wpisz ID: ");
		int id = scanner.nextInt();
		scanner.nextLine(); // Consume newline character
		System.out.print("Wpisz imię: ");
		String name = scanner.nextLine();
		System.out.print("Wpisz wiek: ");
		int age = scanner.nextInt();
		scanner.nextLine(); // Consume newline character
		System.out.print("Wpisz adres email: ");
		String email = scanner.nextLine();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Person> requestEntity = new HttpEntity<>(new Person(id, name, age, email), headers);

		String url = BASE_URL + "/" + id;

		try {
			restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
			System.out.println("Osoba została zaktualizowana.");
		} catch (HttpClientErrorException.NotFound ex) {
			System.out.println("Nie znaleziono osoby o podanym ID: " + id);
		} catch (RestClientException ex) {
			System.out.println("Błąd podczas aktualizacji osoby: " + ex.getMessage());
		}
	}


	private static void deletePerson() {
		int id = getUserChoice("Podaj ID osoby do usunięcia: ");
		String url = BASE_URL + "/" + id;

		try {
			restTemplate.delete(url);
			System.out.println("Osoba została usunięta.");
		} catch (HttpClientErrorException.NotFound ex) {
			System.out.println("Nie znaleziono osoby o podanym ID: " + id);
		} catch (RestClientException ex) {
			System.out.println("Błąd podczas usuwania osoby: " + ex.getMessage());
		}
	}

	private static void getAmountOfPeople() {
		String url = BASE_URL + "/size";
		var response = restTemplate.getForObject(url, Integer.class);
		System.out.println("Osób łącznie: " + response);
	}

}
