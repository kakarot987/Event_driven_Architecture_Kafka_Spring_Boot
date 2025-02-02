package com.eventDrivenKafka.Authentication_Authorization.register.services;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eventDrivenKafka.Authentication_Authorization.register.util.OtpGenerator;

@Service
public class OtpService {
	
    @Autowired
    private OtpGenerator otpGenerator;
    
    private LoadingCache<String, String> mobileOtpCache;
    private LoadingCache<String, String> otpMobileCache;
    
    private static final Integer EXPIRE_MINS = 5;

    public OtpService() {
		
	  otpMobileCache = CacheBuilder.newBuilder().expireAfterWrite(EXPIRE_MINS,
		  TimeUnit.MINUTES).build(new CacheLoader<String,String>(){
		  
		  @Override public String load(String key) throws Exception{ return
		  "notvarified"; } });
    	
    }
    
    public void putGeneratedOtpMobile(String mobile, String otp) {
    	mobileOtpCache.put(mobile, otp);
    }
   
    public String getGeneratedOtpMobile(String mobile) throws ExecutionException {
    	return mobileOtpCache.get(mobile);
    }
    
    public Boolean varifiedOrNotMobile(String key) {
    	try {
    		if(otpMobileCache.get(key).equals("Verified")){
    			return true;
    		}
    		else {
    			return false;
    		}
    	}catch(ExecutionException e) {
    		
    	}
    	return false;
    }
    
    public void putOtpMobileVerifyCache(String key) {
    	otpMobileCache.put(key, "Verified");
    }

}
