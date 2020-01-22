package wex.jira.reports;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import wex.jira.reports.common.JiraReportCommand;

@SpringBootApplication
public class Application implements CommandLineRunner, ExitCodeGenerator {

	private CommandLine.IFactory factory;
	private JiraReportCommand reportCommand;
	private Integer exitCode = 0;

	public Application(CommandLine.IFactory factory, JiraReportCommand reportCommand) {
		this.factory = factory;
		this.reportCommand = reportCommand;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) {
		this.exitCode = new CommandLine(this.reportCommand, factory).execute(args);
	}

	@Override
	public int getExitCode() {
		return this.exitCode;
	}
}
