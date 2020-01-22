package wex.jira.reports.common.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;

@Data
public class JiraFields {
    @SerializedName("resolutiondate")
    private Date resolutionDate;

    private JiraAssignee assignee;

    private Date updated;
}
