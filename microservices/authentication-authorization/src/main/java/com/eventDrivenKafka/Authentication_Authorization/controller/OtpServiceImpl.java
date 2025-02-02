package com.eventDrivenKafka.Authentication_Authorization.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.eventDrivenKafka.Authentication_Authorization.register.services.AuthService;
import com.eventDrivenKafka.Authentication_Authorization.register.services.OtpService;
import jakarta.transaction.Transactional;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.LoginRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.VerifyMobileRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.ApisResponse;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.UserRepository;

@RestController
public class OtpServiceImpl implements OtpMsgController {

	@Autowired
	private OtpService otpService;
	
	@Autowired
	private UserRepository repository;
	
	@Autowired
	private AuthService authService;
	
	@Override
	@Transactional
	public ApisResponse sendOptToMobileNumber(String number) {
		String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
		System.out.println("Otp for login is sent to user "+otp+" **********");

		String msg = "Your generated OTP is " + otp + " is valid for 5 minutes";
		try {
			HttpPost post = new HttpPost("https://api.wesender.co.ao/envio/apikey");
			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair("ApiKey","9c0f3d03d83c47c6a9d28a17c0e3039c525605f0d8fd4512a16c5f726c1ec819"));
			urlParameters.add(new BasicNameValuePair("Destino", number));
			urlParameters.add(new BasicNameValuePair("Mensagem", msg));
			urlParameters.add(new BasicNameValuePair("CEspeciais", "false"));
			post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
			System.out.println(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
			CloseableHttpClient httpClient = HttpClients.createDefault();
			CloseableHttpResponse response = httpClient.execute(post);
			if (response.getStatusLine().getStatusCode() == 200) {
				System.out.println(EntityUtils.toString(response.getEntity()));
				System.out.println(response.getEntity().hashCode());
				System.out.println(response.getStatusLine());
				otpService.putGeneratedOtpMobile(number,otp);
				return new ApisResponse(true, "Otp sent on mobile number");
			} else {
				return  new ApisResponse(false, "Otp was not sent on mobile number");
			}
		}
		catch(Exception ex) {
			return new ApisResponse(false, "Error occured while sending the otp");
		}
			

	}

	@Override
	public ApisResponse verifySentOtp(String number, String sentOtp) {
		String otpString = null;
		try {
			otpString = otpService.getGeneratedOtpMobile(number);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        	if (otpString.equals(sentOtp)) {
        		otpService.putOtpMobileVerifyCache(number);
    			System.out.println("Otp Successfully verified");
    			return new ApisResponse(true, "Otp Successfully verified");
    		} else {
    			System.out.println("Wrong Otp entered");
    			return new ApisResponse(false, "Wrong otp entered");
    		}
	}

	@Override
	public ApisResponse resetPasswordByMobile(LoginRequest loginRequest) {
		if (repository.existsByPhoneNumber(loginRequest.getPhoneNumber())) {
			if (authService.changePasswordByMobile(loginRequest.getPhoneNumber(), loginRequest.getPassword())) {
				return new ApisResponse(true, "Password changed successfully");
			} else {
				return new ApisResponse(false, "Unable to change password. Try again");
			}
		} else {
			return new ApisResponse(false, "User with this phone number does not exists");
		}
	}

	@Override
	public ApisResponse generateOtpForPassReset(VerifyMobileRequest mobileRequest) {
		if (repository.existsByPhoneNumber(mobileRequest.getPhoneNumber())) {
			String otp = new DecimalFormat("000000").format(new Random().nextInt(999999));
			System.out.println("Otp for new password creatatin  "+otp+"  ********************");
            String  number = String.valueOf(mobileRequest.getPhoneNumber());
			String msg = "Your OTP for new password creatation is " + otp + " is valid for 5 minutes";
			try {
				HttpPost post = new HttpPost("https://api.wesender.co.ao/envio/apikey");
				List<NameValuePair> urlParameters = new ArrayList<>();
				urlParameters.add(new BasicNameValuePair("ApiKey","9c0f3d03d83c47c6a9d28a17c0e3039c525605f0d8fd4512a16c5f726c1ec819"));
				urlParameters.add(new BasicNameValuePair("Destino", number));
				urlParameters.add(new BasicNameValuePair("Mensagem", msg));
				urlParameters.add(new BasicNameValuePair("CEspeciais", "false"));
				post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
				System.out.println(new UrlEncodedFormEntity(urlParameters, "UTF-8"));
				CloseableHttpClient httpClient = HttpClients.createDefault();
				CloseableHttpResponse response = httpClient.execute(post);
				if (response.getStatusLine().getStatusCode() == 200) {
					System.out.println(EntityUtils.toString(response.getEntity()));
					System.out.println(response.getEntity().hashCode());
					System.out.println(response.getStatusLine());
					otpService.putGeneratedOtpMobile(number,otp);
					return new ApisResponse(true, "Otp sent on mobile number");
				} else {
					return  new ApisResponse(false, "Otp was not sent on mobile number");
				}
			}
			catch(Exception ex) {
				return new ApisResponse(false, "Error occured while sending the otp");
			}
		}
		else {
		return new ApisResponse(false, "User with this phone number does not exists");
		}
	}

	@Override
	public ApisResponse verifyOtpForPassReset(VerifyMobileRequest mobileRequest) {
		if(repository.existsByPhoneNumber(mobileRequest.getPhoneNumber())){
			String otpString = null;
			String number = String.valueOf(mobileRequest.getPhoneNumber());
			String sentOtp = String.valueOf(mobileRequest.getMobileOtp());
			try {
				otpString = otpService.getGeneratedOtpMobile(number);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        	if (otpString.equals(sentOtp)) {
	        		otpService.putOtpMobileVerifyCache(number);
	    			System.out.println("Otp Successfully verified");
	    			return new ApisResponse(true, "Otp Successfully verified! Proceed with password change");
	    		} else {
	    			System.out.println("Wrong Otp entered");
	    			return new ApisResponse(false, "Wrong otp entered");
	    		}
		}else {
			return new ApisResponse(false, "User with this phone number does not exists");
		}
	}

}
