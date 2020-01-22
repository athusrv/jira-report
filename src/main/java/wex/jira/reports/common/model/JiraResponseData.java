package wex.jira.reports.common.model;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JiraResponseData {
    private List<JiraIssue> issues = new ArrayList<>();
}
