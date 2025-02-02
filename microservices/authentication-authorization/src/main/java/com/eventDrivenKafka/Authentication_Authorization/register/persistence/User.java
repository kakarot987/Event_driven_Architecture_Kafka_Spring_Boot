package com.eventDrivenKafka.Authentication_Authorization.register.persistence;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user", uniqueConstraints = {
		@UniqueConstraint(columnNames = "userId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "userAuthId")
	@Schema(description = "User ID")
	private Long id;

	@Column(name = "userId")
	@Schema(description = "User ID")
	private Long userId;

	@Size(max = 200)
	@Schema(description = "Authentication token")
	private String authToken;

	@Email
	@Column
	@Schema(description = "User email")
	private String email;

	@Column
	@Schema(description = "User phone number")
	private Long phoneNumber;

	@Size(max = 200)
	@Schema(description = "User password")
	private String password;

	@Column
	@Schema(description = "Email verification status")
	private Boolean emailVerified;

	@Column
	@Schema(description = "Phone number verification status")
	private Boolean phoneNumberVerified;

	@Schema(description = "Date of creation")
	private Date created;

	@Schema(description = "Date of modification")
	private Date modified;

	@Schema(description = "Username")
	private String username;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_roles",
			joinColumns = @JoinColumn(name = "user_id"),
			inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles = new HashSet<>();

	public User(Long userId, @Size(max = 200) String authToken, @Email String email, Long phoneNumber,
				@Size(max = 200) String password, Boolean emailVerified,
				Boolean phoneNumberVerified, Date created, String username) {
		this.userId = userId;
		this.authToken = authToken;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.password = password;
		this.emailVerified = emailVerified;
		this.phoneNumberVerified = phoneNumberVerified;
		this.created = created;
		this.username = username;
	}

	// Getters and setters
}
