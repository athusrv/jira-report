package wex.jira.reports.common;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import wex.jira.reports.common.converter.String2DateConverter;
import wex.jira.reports.common.model.JiraParams;
import wex.jira.reports.common.model.JiraResponseData;
import wex.jira.reports.common.service.JiraService;
import wex.jira.reports.common.service.ReportService;

import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Component
@Command(name = "jira-report",
        sortOptions = false)
public class JiraReportCommand implements Callable<Integer> {

    @Parameters(index = "0"/*, defaultValue = "generate"*/, arity = "0")
    private String command;

    @Option(names = {"-u", "--user"}, required = true, description = "User email used to login into Jira"/*, defaultValue = "athus.vieira@wexinc.com"*/)
    private String user;

    @Option(names = {"-t", "--token"}, required = true, description = "Token to access Jira API"/*, defaultValue = "UeUH5BsiB9HYkKXa2xV75AD8"*/)
    private String token;

    @Option(names = {"-p", "--project"}, description = "Project name in Jira"/*, defaultValue = "EDGE"*/)
    private String project;

    @Option(names = {"-a", "--assignees"}, description = "Users to look up")
    String[] assignees;

    @Option(names = {"--from"}, description = "Start date (YYYY-MM-DD)", converter = String2DateConverter.class/*, defaultValue = "2020-01-01"*/)
    private Date startDate;

    @Option(names = {"--to"}, description = "End date (YYYY-MM-DD)", converter = String2DateConverter.class/*, defaultValue = "2020-01-31"*/)
    private Date endDate;

    @Option(names = {"-l", "--max"}, description = "Max results to fetch from the Jira Api", defaultValue = "1000")
    private Integer maxResults;

    @Option(names = {"-o", "--out"}, description = "Where to save the generated report"/*, defaultValue = "C:\\Users\\avieira\\Documents"*/)
    private String outputPath;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display a help message")
    private boolean helpRequested = true;

    private final JiraService jiraService;
    private final ReportService reportService;
    private final Scanner scanner;

    public JiraReportCommand(JiraService jiraService, ReportService reportService) {
        this.jiraService = jiraService;
        this.reportService = reportService;
        this.scanner = new Scanner(System.in);
    }


    @Override
    public Integer call() throws Exception {
        if (JiraReportAction.GENERATE.getAction().equalsIgnoreCase(command)) {
            if (project == null) {
                System.out.print("What is the project name? ");
                project = scanner.nextLine();
            }

            if (assignees == null || assignees.length == 0) {
                System.out.print("What are the assignees you are looking for? (separated by comma (,)) ");
                assignees = scanner.nextLine().replace(" ", "").split(",");
            }

            if (startDate == null) {
                System.out.print("Start date (YYYY-MM-DD): ");
                startDate = new String2DateConverter().convert(scanner.nextLine());
            }

            if (endDate == null) {
                System.out.print("End date (YYYY-MM-DD): ");
                endDate = new String2DateConverter().convert(scanner.nextLine());
            }

            if (outputPath == null) {
                System.out.print("Where do you want to save the report? ");
                outputPath = scanner.nextLine();
            }

            System.out.println("\nGenerating report...");
            JiraResponseData data = this.jiraService.fetchData(new JiraParams(user, token, project, assignees, startDate, endDate, maxResults));
            this.reportService.createReport(data, project, outputPath);

            System.out.println("\nDone! Your report was saved to " + outputPath);
        } else {
            System.out.println("You must specify a command to be done. E.g.: jira-report generate ... ");
            return -1;
        }
        return 0;
    }
}
