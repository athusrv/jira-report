package wex.jira.reports.common.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class JiraIssue {
    @SerializedName("self")
    private String url;

    @SerializedName("key")
    private String id;

    private JiraFields fields;
}
