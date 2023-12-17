package com.eventDrivenKafka.Authentication_Authorization.persistence;


import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(	name = "userAuthLudo",
		uniqueConstraints = { 
			@UniqueConstraint(columnNames = "userId")
		})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userAuthId")
	private Long id;

	@Column(name = "userId")
	private Long userId;

	@Column(name = "authToken")
	private String authToken;

	@Column
	private String email;
	
	@Column
	private Long phoneNumber;
	
	private String password;
	
	@Column
	private Boolean emailVerified;

	@Column
	private String emailOtp;

	@Column
	private Date emailOtpCreatedDate;
	
	@Column
	private Date emailOtpModifiedDate;
	
	@Column
	private String mobileOtp;
    
	@Column
	private Date mobileOtpCreatedDate;
	
	@Column
	private Date mobileOtpModifiedDate;
	
	@Column
	private Boolean phoneNumberVerified;

	private Date created;

	private Date modified;
	
	private int flagActive;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(	name = "user_roles", 
				joinColumns = @JoinColumn(name = "user_id"), 
				inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();
	
	public User() {
		super();
	}

	public User(Long userId,  String authToken, String email, Long phoneNumber,
			String password, Boolean emailVerified, String emailOtp, Date emailOtpCreatedDate,
			Date emailOtpModifiedDate,String mobileOtp,
			Date mobileOtpCreatedDate, Date mobileOtpModifiedDate,
			Boolean phoneNumberVerified,Date created, int flagActive) {
		super();
		this.userId = userId;
		this.authToken = authToken;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.emailVerified = emailVerified;
		this.emailOtp = emailOtp;
		this.emailOtpCreatedDate = emailOtpCreatedDate;
		this.emailOtpModifiedDate = emailOtpModifiedDate;
		this.mobileOtp = mobileOtp;
		this.mobileOtpCreatedDate = mobileOtpCreatedDate;
		this.mobileOtpModifiedDate = mobileOtpModifiedDate;
	    this.phoneNumberVerified = phoneNumberVerified;
		this.created = created;
		this.flagActive = flagActive;
	}

	public User(Long userId, String email, String jwt, Date createdDate) {
		this.userId = userId;
		this.email = email;
		this.authToken = jwt;
		this.created = createdDate;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}



	public Set<Role> getRoles() {
		return roles;
	}



	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}



	public String getEmail() {
		return email;
	}



	public void setEmail(String email) {
		this.email = email;
	}



	public Long getPhoneNumber() {
		return phoneNumber;
	}



	public void setPhoneNumber(Long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getEmailOtp() {
		return emailOtp;
	}

	public void setEmailOtp(String emailOtp) {
		this.emailOtp = emailOtp;
	}

	public Date getEmailOtpCreatedDate() {
		return emailOtpCreatedDate;
	}

	public void setEmailOtpCreatedDate(Date emailOtpCreatedDate) {
		this.emailOtpCreatedDate = emailOtpCreatedDate;
	}

	public Date getEmailOtpModifiedDate() {
		return emailOtpModifiedDate;
	}

	public void setEmailOtpModifiedDate(Date emailOtpModifiedDate) {
		this.emailOtpModifiedDate = emailOtpModifiedDate;
	}

	public String getMobileOtp() {
		return mobileOtp;
	}

	public void setMobileOtp(String mobileOtp) {
		this.mobileOtp = mobileOtp;
	}

	public Date getMobileOtpCreatedDate() {
		return mobileOtpCreatedDate;
	}

	public void setMobileOtpCreatedDate(Date mobileOtpCreatedDate) {
		this.mobileOtpCreatedDate = mobileOtpCreatedDate;
	}

	public Date getMobileOtpModifiedDate() {
		return mobileOtpModifiedDate;
	}

	public void setMobileOtpModifiedDate(Date mobileOtpModifiedDate) {
		this.mobileOtpModifiedDate = mobileOtpModifiedDate;
	}

	public Boolean isPhoneNumberVerified() {
		return phoneNumberVerified;
	}

	public void setPhoneNumberVerified(Boolean phoneNumberVerified) {
		this.phoneNumberVerified = phoneNumberVerified;
	}

	public int getFlagActive() {
		return flagActive;
	}

	public void setFlagActive(int flagActive) {
		this.flagActive = flagActive;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public Boolean getPhoneNumberVerified() {
		return phoneNumberVerified;
	}
	
	
	
}


