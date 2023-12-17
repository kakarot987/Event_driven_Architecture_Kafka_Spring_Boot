package com.eventDrivenKafka.api.auth_auth.payload.request;

import java.math.BigInteger;

public class SignInRequest {
	
	  private BigInteger userId;
	  
	    private String password;
	    
	    private String email;
	    
	    private Long phoneNumber;

	    
		public SignInRequest() {
			super();
			// TODO Auto-generated constructor stub
		}

		public SignInRequest(BigInteger userId, String password) {
			super();
			this.userId = userId;
			this.password = password;
		}

		public String getPassword() {
	        return password;
	    }
	 
	    public void setPassword(String password) {
	        this.password = password;
	    }

	    public BigInteger getUserId() {
	        return userId;
	    }

	    public void setUserId(BigInteger userId) {
	        this.userId = userId;
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
