package wex.jira.reports.common.model.report;

import lombok.Data;

import java.util.HashMap;
import java.util.Set;

@Data
public class Report {
    HashMap<Integer, Set<HashMap<Integer, Set<ReportItem>>>> items = new HashMap<>();
}
