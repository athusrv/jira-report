package wex.jira.reports.common.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class JiraAssignee {
    @SerializedName("self")
    private String href;

    @SerializedName("name")
    private String username;

    @SerializedName("displayName")
    private String name;
}
