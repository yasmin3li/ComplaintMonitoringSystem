package com.myapp.complaints;

import com.myapp.complaints.config.RSAKeyRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(RSAKeyRecord.class)
@SpringBootApplication
@ComponentScan("com.myapp.complaints")
@EnableScheduling
public class ComplaintsMonitoringSystem {

	public static void main(String[] args) {
		SpringApplication.run(ComplaintsMonitoringSystem.class, args);

	}

}

//TODO: check from existing citizen & employee not only account
//Asymmetric keys -> private and public using openSSL
