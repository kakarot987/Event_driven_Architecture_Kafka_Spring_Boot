package com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request;

public class SignupRequest {
	
    private String password;
    
    private String email;
    
    private Long phoneNumber;

	private String username;

	public SignupRequest(String password, String email, Long phoneNumber, String username) {
		super();
		this.password = password;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.username = username;
	}


	public String getPassword() {
        return password;
    }
 
    public void setPassword(String password) {
        this.password = password;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
