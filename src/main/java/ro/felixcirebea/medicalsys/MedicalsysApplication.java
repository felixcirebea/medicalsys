package ro.felixcirebea.medicalsys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ro.felixcirebea.medicalsys.exception.DataMismatchException;
import ro.felixcirebea.medicalsys.service.InputFileParser;

@SpringBootApplication
public class MedicalsysApplication {

	public static void main(String[] args) throws DataMismatchException {
		ApplicationContext run = SpringApplication.run(MedicalsysApplication.class, args);
		InputFileParser fileParser = run.getBean(InputFileParser.class);
		fileParser.run();
	}

}
