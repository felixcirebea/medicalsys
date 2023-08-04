package ro.felixcirebea.medicalsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ro.felixcirebea.medicalsys.service.InputFileParser;

@SpringBootApplication
public class MedicalsysApplication {

	//TODO in the next commit include input files and also postman test cases

	public static void main(String[] args) {
		ApplicationContext run = SpringApplication.run(MedicalsysApplication.class, args);
		InputFileParser fileParser = run.getBean(InputFileParser.class);
		fileParser.run();
	}

}
