package com.eventDrivenKafka.api.auth_auth.payload.request;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserAggregate {

    private Long userId;
    private String authToken;
    private Date created;
    private Date modified;
    private List<String> roles;


    public UserAggregate() {
        super();
        // TODO Auto-generated constructor stub
    }
    public UserAggregate(Long userId, String authToken, Date created, Date modified,
                         List<String> roleString) {
        super();
        this.userId = userId;
        this.authToken = authToken;
        this.created = created;
        this.modified = modified;
        this.roles = roleString;
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
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }



}
