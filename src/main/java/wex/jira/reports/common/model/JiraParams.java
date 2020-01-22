package wex.jira.reports.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class JiraParams {
    private String user;
    private String token;
    private String project;
    private String[] assignees;
    private Date startDate;
    private Date endDate;
    private Integer maxResults;
}
