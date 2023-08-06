package ro.felixcirebea.medicalsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ro.felixcirebea.medicalsys.service.InputFileParser;

@SpringBootApplication
public class MedicalsysApplication {

	//TODO mandatory check and log all the cascading delete operations because they delete multiple rows in different tables
	//TODO mark delete specialty as risk zone operation or practice soft delete

	public static void main(String[] args) {
		ApplicationContext run = SpringApplication.run(MedicalsysApplication.class, args);
		InputFileParser fileParser = run.getBean(InputFileParser.class);
		fileParser.run();
	}

}
