package com.cemla;
import java.io.Serializable;

public class PassengerRecord implements Serializable {
	private static final long serialVersionUID = 5725655672763534548L;

	String surname = null;
	String name = null;
	String age = null;
	String civilStatus = null;
	String profession = null;
	String religion = null;
	String nationality = null;
	String ship = null;
	String departure = null;
	String arrivalDate = null;
	String arrivalPort = null;
	String placeOfBirth = null;

	public PassengerRecord(final String surname, final String name, final String age, final String civilStatus, final String profession, final String religion, final String nationality, final String ship, final String departure, final String arrivalDate, final String arrivalPort, final String placeOfBirth) {
		super();
		this.surname = this.adapt(surname);
		this.name = this.adapt(name);
		this.age = this.adapt(age);
		this.civilStatus = this.adapt(civilStatus);
		this.profession = this.adapt(profession);
		this.religion = this.adapt(religion);
		this.nationality = this.adapt(nationality);
		this.ship = this.adapt(ship);
		this.departure = this.adapt(departure);
		this.arrivalDate = this.adapt(arrivalDate);
		this.arrivalPort = this.adapt(arrivalPort);
		this.placeOfBirth = this.adapt(placeOfBirth);
	}

	private String adapt(final String s) {
		if (s == null) {
			return null;
		}

		return s.replaceAll("'", "''");
	}

	public String getSurname() {
		return this.surname;
	}

	public void setSurname(final String surname) {
		this.surname = surname;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getAge() {
		return this.age;
	}

	public void setAge(final String age) {
		this.age = age;
	}

	public String getCivilStatus() {
		return this.civilStatus;
	}

	public void setCivilStatus(final String civilStatus) {
		this.civilStatus = civilStatus;
	}

	public String getProfession() {
		return this.profession;
	}

	public void setProfession(final String profession) {
		this.profession = profession;
	}

	public String getReligion() {
		return this.religion;
	}

	public void setReligion(final String religion) {
		this.religion = religion;
	}

	public String getNationality() {
		return this.nationality;
	}

	public void setNationality(final String nationality) {
		this.nationality = nationality;
	}

	public String getShip() {
		return this.ship;
	}

	public void setShip(final String ship) {
		this.ship = ship;
	}

	public String getDeparture() {
		return this.departure;
	}

	public void setDeparture(final String departure) {
		this.departure = departure;
	}

	public String getArrivalDate() {
		return this.arrivalDate;
	}

	public void setArrivalDate(final String arrivalDate) {
		this.arrivalDate = arrivalDate;
	}

	public String getArrivalPort() {
		return this.arrivalPort;
	}

	public void setArrivalPort(final String arrivalPort) {
		this.arrivalPort = arrivalPort;
	}

	public String getPlaceOfBirth() {
		return this.placeOfBirth;
	}

	public void setPlaceOfBirth(final String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}
}