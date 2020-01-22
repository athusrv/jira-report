package wex.jira.reports.common.model.report;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class ReportItem {
    private String assignee;
    private Integer numOfTasks = 0;
    private Integer averageStoryPoints = 0;
    private Integer sumOfStoryPoints = 0;
    List<String> issueIds = new ArrayList<>();

    public void addNumOfTasks() {
        this.numOfTasks++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReportItem)) return false;
        ReportItem that = (ReportItem) o;
        return getAssignee().equals(that.getAssignee());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAssignee());
    }
}
