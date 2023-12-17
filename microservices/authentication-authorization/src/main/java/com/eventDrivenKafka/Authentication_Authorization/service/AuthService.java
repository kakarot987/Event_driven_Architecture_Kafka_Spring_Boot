package com.eventDrivenKafka.Authentication_Authorization.service;

import com.eventDrivenKafka.Authentication_Authorization.persistence.User;
import com.eventDrivenKafka.Authentication_Authorization.persistence.UserRepository;
import com.eventDrivenKafka.Authentication_Authorization.util.JwtUtils;
import com.eventDrivenKafka.api.auth_auth.payload.response.JwtResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    JwtUtils jwtUtils;

    @Transactional
    public JwtResponse updateUserAuth(Long userId, String password){
    	JwtRes jwt = generateToken(userId,password);
        System.out.print("generated Token----" + jwt.getRoles().get(0));
        System.out.print("generated Token----" + jwt.getJwtToken());
        Date modifiedDate = updateDate();
         userRepository.updateUserSaveToken(
                userId,
                modifiedDate,
                jwt.getJwtToken()
        );
        JwtResponse jwtResponse = new JwtResponse(userId, jwt.getJwtToken(),modifiedDate,jwt.getRoles());
        return jwtResponse;
    }
    
    public boolean changePassword(String email, String password) {
        User user = findByEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        if(userRepository.save(user) != null) {
            return true;
        }
        return false;
    }
    
    public boolean changePasswordByMobile(Long phoneNumber,String password) {
    	User user = findByPhoneNumber(phoneNumber);
    	user.setPassword(passwordEncoder.encode(password));
        if(userRepository.save(user) != null) {
            return true;
        }
        return false;
    }
    

	private User findByPhoneNumber(Long phoneNumber) {
		return userRepository.findByPhoneNumber(phoneNumber).get();
	}

	public User findByEmail(String email) {
        return userRepository.findByEmail(email).get();        
    }
    
    private JwtRes generateToken(Long userId, String password){
    	String username = String.valueOf(userId);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        System.out.print("generated Token----s" + jwt);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());
        System.out.println("---------" + roles.get(0));
        JwtRes js = new JwtRes(jwt, roles);
        return js;
    }

    private Date updateDate(){
        Date updatedTime = new Date();
        return updatedTime;
    }

    class JwtRes{
    	String jwtToken;
    	 List<String> roles;
    	 
    	JwtRes(String jwtToken, List<String> roles){
    		this.jwtToken = jwtToken;
    		this.roles = roles;
    	}

    	public String getJwtToken() {
    		return jwtToken;
    	}

    	public void setJwtToken(String jwtToken) {
    		this.jwtToken = jwtToken;
    	}

    	public List<String> getRoles() {
    		return roles;
    	}

    	public void setRoles(List<String> roles) {
    		this.roles = roles;
    	}
    	
    	
    }

}