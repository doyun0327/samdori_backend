package com.consult.reservation;

import com.consult.reservation.config.DatasourceEnvListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReservationApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ReservationApplication.class);
		app.addListeners(new DatasourceEnvListener());
		app.run(args);
	}
}
