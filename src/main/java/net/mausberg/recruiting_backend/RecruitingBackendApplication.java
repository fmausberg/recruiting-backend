//AuthenticationFrameworkBackendApplication.java
package net.mausberg.recruiting_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "net.mausberg.recruiting_backend")
@EnableJpaRepositories(basePackages = "net.mausberg.recruiting_backend.repository")
@EntityScan(basePackages = "net.mausberg.recruiting_backend.model")
public class RecruitingBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(RecruitingBackendApplication.class, args);
	}

}
