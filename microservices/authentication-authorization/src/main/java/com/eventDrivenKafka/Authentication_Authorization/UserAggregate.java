package com.eventDrivenKafka.Authentication_Authorization;

import java.util.*;

public class UserAggregate {
	
	private Long userId;
	private String authToken;
	private Date created;
	private Date modified;
    private String serviceAddress;
	private List<String> roles = new ArrayList<>();

    private String username;
    
	public UserAggregate() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserAggregate(Long userId,String accessToken,String username, List<String> roles) {
		this.userId = userId;
		this.authToken = accessToken;
		this.username = username;
		this.roles = roles;
	}
	public UserAggregate(Long userId,String username ,String authToken, Date created, Date modified) {
		this.userId = userId;
		this.authToken = authToken;
		this.created = created;
		this.modified = modified;
		this.username = username;
	}


	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getAuthToken() {
		return authToken;
	}
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
}
