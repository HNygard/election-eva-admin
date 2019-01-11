package no.valg.eva.admin.common;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;

import org.joda.time.LocalDate;

/**
 * Represents a person with name, address
 */
public class Person implements Serializable {

	private final PersonId personId;
	private final LocalDate dateOfBirth;
	protected String firstName;
	protected String middleName;
	protected String lastName;
	protected Address address;
	protected String email;
	protected String telephoneNumber;

	public Person(PersonId personId, LocalDate dateOfBirth, String firstName, String middleName, String lastName, Address address) {
		this.personId = personId;
		this.dateOfBirth = dateOfBirth;
		this.firstName = requireNonNull(firstName);
		this.middleName = middleName;
		this.lastName = requireNonNull(lastName);
		this.address = address;
	}

	protected Person(Person person) {
		this.personId = person.getPersonId();
		this.dateOfBirth = person.getDateOfBirth();
		this.firstName = requireNonNull(person.getFirstName());
		this.middleName = person.getMiddleName();
		this.lastName = requireNonNull(person.getLastName());
		this.address = person.getAddress();
		this.email = person.getEmail();
		this.telephoneNumber = person.getTelephoneNumber();
	}

	public String nameLine() {
		StringBuilder nameLine = new StringBuilder(lastName);
		nameLine.append(", ");
		nameLine.append(firstName);
		nameLine.append(hasMiddleName() ? " " + middleName : "");
		return nameLine.toString().trim();
	}

	private boolean hasMiddleName() {
		return middleName != null && middleName.length() > 0;
	}

	public Address getAddress() {
		return address;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public PersonId getPersonId() {
		return personId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public String getMiddleName() {
		return middleName;
	}

	public Person withEmail(String email) {
		Person person = new Person(this);
		person.email = email;
		return person;
	}

	public Person withTelephoneNumber(String telephoneNumber) {
		Person person = new Person(this);
		person.telephoneNumber = telephoneNumber;
		return person;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
}
