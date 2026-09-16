package com.eventDrivenKafka.Authentication_Authorization.register.services;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import com.eventDrivenKafka.Authentication_Authorization.register.util.OtpGenerator;

@Service
public class OtpService {
	private final OtpGenerator otpGenerator;
    private LoadingCache<String, String> mobileOtpCache;
    private LoadingCache<String, String> otpMobileCache;
    
    private static final Integer EXPIRE_MINS = 5;

    public OtpService(OtpGenerator otpGenerator) {
		this.otpGenerator = otpGenerator;
		
		mobileOtpCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
				.build(new CacheLoader<String, String>(){
					@Override 
					public String load(String key) throws Exception { 
						return "notverified"; 
					}
				});
    	
		otpMobileCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_MINS, TimeUnit.MINUTES)
				.build(new CacheLoader<String, String>(){
					@Override 
					public String load(String key) throws Exception { 
						return "notverified"; 
					}
				});
    }
    
    public void putGeneratedOtpMobile(String mobile, String otp) {
    	mobileOtpCache.put(mobile, otp);
    }
   
    public String getGeneratedOtpMobile(String mobile) throws ExecutionException {
    	return mobileOtpCache.get(mobile);
    }
    
    public Boolean verifiedOrNotMobile(String key) {
    	try {
    		if(otpMobileCache.get(key).equals("Verified")){
    			return true;
    		}
    		else {
    			return false;
    		}
    	}catch(ExecutionException e) {
    		return false;
    	}
    }
    
    public void putOtpMobileVerifyCache(String key) {
    	otpMobileCache.put(key, "Verified");
    }

}
