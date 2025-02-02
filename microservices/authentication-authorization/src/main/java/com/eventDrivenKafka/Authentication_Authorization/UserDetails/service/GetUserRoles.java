package com.eventDrivenKafka.Authentication_Authorization.UserDetails.service;

import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class GetUserRoles {
	  
    private LoadingCache<String, String> userRole;
    private static final Integer EXPIRE_MINS = 30;

    public GetUserRoles() {
   
    	userRole = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES).build(new CacheLoader<String,String>(){
        	@Override
        	public String load(String key) throws Exception{
        		return "guest";
        	}
        });
    }
    
    public String getRoleFromCache(Long userId) throws ExecutionException {
    	return userRole.get(userId.toString());
    }

}
