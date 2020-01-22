package wex.jira.reports.common;

public enum JiraReportAction {
    GENERATE("generate");

    private String action;

    JiraReportAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
