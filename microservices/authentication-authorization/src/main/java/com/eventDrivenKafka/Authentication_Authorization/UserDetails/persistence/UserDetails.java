package com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "userDetails")
public class UserDetails {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(unique = true)
	private Long userId;
	private String email;
	private String userName;
	private String firstName;
	private String lastName;
	private String gender;
	private String country;
	private Date created;
	private Long phoneNumber;
	private String generatedUserName;
	
	public UserDetails() {
		super();
		// TODO Auto-generated constructor stub
	}
	public UserDetails(int id, Long userid, String email, 
			String userName, String firstName, String lastName,
			String gender, String country, String device_id, 
			String devicetype, String devicetoken, Date created,
			String generatedUserName) {
		super();
		this.id = id;
		this.userId = userid;
		this.email = email;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.gender = gender;
		this.country = country;
		this.created = created;
		this.generatedUserName = generatedUserName;
	}
	
	public UserDetails(Long userId, String email, Date created, String userName) {
		this.userId = userId;
		this.email = email;
		this.created = created;
		this.userName = userName;
	}
	
	public UserDetails(Long userId, Long phoneNumber, Date created, String userName) {
		this.userId = userId;
		this.phoneNumber = phoneNumber;
		this.created = created;
		this.userName = userName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
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
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Long getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(Long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getGeneratedUserName() {
		return generatedUserName;
	}
	public void setGeneratedUserName(String generatedUserName) {
		this.generatedUserName = generatedUserName;
	}

	
}

