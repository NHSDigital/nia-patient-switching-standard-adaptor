package uk.nhs.adaptors.gp2gpmhstestenv.mockspinemhsoutbound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class MockSpineMhsOutboundApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockSpineMhsOutboundApplication.class, args);
    }
}
