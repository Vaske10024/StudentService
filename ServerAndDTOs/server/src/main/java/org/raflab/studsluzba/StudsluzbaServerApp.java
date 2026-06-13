package org.raflab.studsluzba;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication(scanBasePackages = {"org.raflab"})
public class StudsluzbaServerApp {

	public static void main(String[] args) {
		SpringApplication.run(StudsluzbaServerApp.class, args);
	}
}
