package com.eventDrivenKafka.Authentication_Authorization.register.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eventDrivenKafka.Authentication_Authorization.register.persistence.User;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.UserRepository;
import com.eventDrivenKafka.Authentication_Authorization.register.util.JwtUtils;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.JwtResponse;

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
        Date modifiedDate = updateDate();
         userRepository.updateUserSaveToken(
                userId,
                modifiedDate,
                 jwt.getJwt()

        );
         System.out.println("pioip" + jwt.getRoles().get(0));
        JwtResponse jwtResponse = new JwtResponse(userId, jwt.getJwt(),jwt.getUsername(),jwt.getRoles());
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
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtRes(jwt,roles,userDetails.getUsername());
    }

    private Date updateDate(){
        Date updatedTime = new Date();
        return updatedTime;
    }

}
class JwtRes{
    private String jwt;
    private  List<String> roles;

    private String username;

    public JwtRes(String jwt, List<String> roles, String username) {
        this.jwt = jwt;
        this.roles = roles;
        this.username = username;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}