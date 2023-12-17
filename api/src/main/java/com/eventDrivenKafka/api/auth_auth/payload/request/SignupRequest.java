package com.eventDrivenKafka.api.auth_auth.payload.request;

public class SignupRequest {
	
    private String password;
    
    private String email;
    
    private Long phoneNumber;
    
    public SignupRequest() {
		// TODO Auto-generated constructor stub
	}

	
	public SignupRequest(String password, String email, Long phoneNumber) {
		super();
		this.password = password;
		this.email = email;
		this.phoneNumber = phoneNumber;
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

    
}
