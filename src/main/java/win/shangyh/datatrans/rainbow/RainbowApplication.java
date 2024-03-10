package win.shangyh.datatrans.rainbow;

import java.sql.Types;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import win.shangyh.datatrans.rainbow.transfer.ColumnTransferRegister;
import win.shangyh.datatrans.rainbow.transfer.DateTimeTransfer;
import win.shangyh.datatrans.rainbow.transfer.DateTransfer;
import win.shangyh.datatrans.rainbow.transfer.TimeTransfer;
import win.shangyh.datatrans.rainbow.util.DateUtil;

@SpringBootApplication
public class RainbowApplication {

	public static void main(String[] args) {
		SpringApplication.run(RainbowApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDateProcessor(DateUtil dateUtil){
		return args->{
			Class.forName("win.shangyh.datatrans.rainbow.util.DBUtils");
			// DateTransfer dateTransfer = new DateTransfer(dateUtil);
			
			DateTimeTransfer dateTimeTransfer = new DateTimeTransfer(dateUtil);
			ColumnTransferRegister.registerColumnTransfer(Types.TIMESTAMP, dateTimeTransfer);
			ColumnTransferRegister.registerColumnTransfer(Types.DATE, dateTimeTransfer);
			
			TimeTransfer timeTransfer = new TimeTransfer(dateUtil);
			ColumnTransferRegister.registerColumnTransfer(Types.TIME, timeTransfer);
		};
	}
}
