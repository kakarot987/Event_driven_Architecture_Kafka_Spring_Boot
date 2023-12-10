package se.magnus.springcloud.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import se.magnus.springcloud.gateway.persistence.UserDetailsImpl;
import se.magnus.springcloud.gateway.persistence.UserDetailsServiceImpl;
import se.magnus.springcloud.gateway.persistence.UserRepository;

/**
 * @author Shivani Sharma
 *
 */
@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {
	private static final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);

	@Autowired
	public AuthFilter() {
		super(Config.class);
	}

	public static class Config {
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {
			true;
		};
	}
}
