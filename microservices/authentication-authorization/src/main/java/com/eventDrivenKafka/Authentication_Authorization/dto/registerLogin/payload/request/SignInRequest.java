package com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.math.BigInteger;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SignInRequest {

	@Schema(description = "User ID")
	private BigInteger userId;

	@Schema(description = "Password")
	private String password;

	@Schema(description = "Email")
	private String email;

	@Schema(description = "Phone number")
	private Long phoneNumber;

	@Schema(description = "Username")
	private String username;
}
