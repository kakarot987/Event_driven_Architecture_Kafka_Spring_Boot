package com.eventDrivenKafka.api.auth_auth.payload.response;

import java.util.Date;
import java.util.List;

public class JwtResponse {
	private String token;
	private String type = "Bearer";
	private Long userId;
	private Date modifiedDate;
	private Date createdDate;
	private List<String> roles;
	
	public JwtResponse(Long userId,String accessToken, Date modifiedDate, List<String> roles) {
		this.userId = userId;
		this.token = accessToken;
		this.modifiedDate = modifiedDate;
		this.roles = roles;
	}


	public String getToken() {
		return token;
	}


	public void setToken(String token) {
		this.token = token;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public Date getModifiedDate() {
		return modifiedDate;
	}


	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}


	public Date getCreatedDate() {
		return createdDate;
	}


	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}


	public String getAccessToken() {
		return token;
	}

	public void setAccessToken(String accessToken) {
		this.token = accessToken;
	}

	public String getTokenType() {
		return type;
	}

	public void setTokenType(String tokenType) {
		this.type = tokenType;
	}


	public List<String> getRoles() {
		return roles;
	}


	public void setRoles(List<String> roles) {
		this.roles = roles;
	}


	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}