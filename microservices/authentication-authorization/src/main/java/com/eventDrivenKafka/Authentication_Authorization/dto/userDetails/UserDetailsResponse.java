package com.eventDrivenKafka.Authentication_Authorization.dto.userDetails;

public class UserDetailsResponse {

	private Long userId;
	private String email;
	private String username;
	private String firstname;
	private String lastname;
	private String gender;
	private String country;
	public UserDetailsResponse() {
		super();
	}
	
	public UserDetailsResponse(Long userId, String email, String username, 
			String firstname, String lastname, String gender, String country) {
		super();
		this.userId = userId;
		this.email = email;
		this.username = username;
		this.firstname = firstname;
		this.lastname = lastname;
		this.gender = gender;
		this.country = country;
	}


	public Long getUserId() {
		return userId;
	}
	public void setUserid(Long userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
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

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
}
