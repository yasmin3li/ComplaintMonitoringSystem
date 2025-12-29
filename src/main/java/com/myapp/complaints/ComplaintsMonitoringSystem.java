package com.myapp.complaints;

import com.myapp.complaints.config.RSAKeyRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@EnableConfigurationProperties(RSAKeyRecord.class)
@SpringBootApplication
@ComponentScan("com.myapp.complaints")

public class ComplaintsMonitoringSystem {

	public static void main(String[] args) {
		SpringApplication.run(ComplaintsMonitoringSystem.class, args);

	}

}


//Asymmetric keys -> private and public using openSSL
