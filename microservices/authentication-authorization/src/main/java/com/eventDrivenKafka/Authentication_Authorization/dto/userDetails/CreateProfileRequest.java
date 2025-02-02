package com.eventDrivenKafka.Authentication_Authorization.dto.userDetails;

public class CreateProfileRequest {
	
	private String username;
	private String firstname;
	private String lastname;
	private String gender;
	private String country;
	public CreateProfileRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	public CreateProfileRequest(String firstname, String lastname, String gender,
			String country, int avatarId) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.gender = gender;
		this.country = country;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
	
}
