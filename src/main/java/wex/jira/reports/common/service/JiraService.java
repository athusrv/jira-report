package wex.jira.reports.common.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import wex.jira.reports.common.config.JiraApiProperties;
import wex.jira.reports.common.converter.String2DateConverter;
import wex.jira.reports.common.model.JiraAssignee;
import wex.jira.reports.common.model.JiraIssue;
import wex.jira.reports.common.model.JiraParams;
import wex.jira.reports.common.model.JiraResponseData;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JiraService {
    private RestTemplate restTemplate = new RestTemplate();

    private final JiraApiProperties apiProperties;

    public JiraService(JiraApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    public JiraResponseData fetchData(JiraParams jiraParams) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(HttpHeaders.AUTHORIZATION, "Basic " + buildAuthorization(jiraParams.getUser(), jiraParams.getToken()));

        HttpEntity<?> request = new HttpEntity<>(headers);

        JiraResponseData responseData = new JiraResponseData();

        for (String assignee : jiraParams.getAssignees()) {
            String getUserUrl = apiProperties.getUrl() + "/user?username=" + assignee;
            ResponseEntity<JiraAssignee> getUserResponse = restTemplate.exchange(getUserUrl, HttpMethod.GET, request, new ParameterizedTypeReference<JiraAssignee>() {});

            String searchIssueUrl = apiProperties.getUrl() + "/search/" + buildSearchIssueUriParams(jiraParams.getProject(), assignee, jiraParams.getStartDate(), jiraParams.getEndDate(), jiraParams.getMaxResults());
            ResponseEntity<JiraResponseData> response = restTemplate.exchange(searchIssueUrl, HttpMethod.GET, request, new ParameterizedTypeReference<JiraResponseData>() {});

            if(response.getStatusCode() != HttpStatus.OK) {
                throw new HttpClientErrorException(response.getStatusCode());
            }

            List<JiraIssue> issues = response.getBody().getIssues()
                    .stream()
                    .peek(issue -> issue.getFields().setAssignee(getUserResponse.getBody()))
                    .collect(Collectors.toList());

            responseData.getIssues().addAll(issues);
        }

        return responseData;
    }

    private static String buildSearchIssueUriParams(String project, String assignee, Date startDate, Date endDate, Integer maxResults) {
        SimpleDateFormat formatter = new SimpleDateFormat(String2DateConverter.DATE_FORMAT);

        return new StringBuilder()
                .append("?jql=project = \"")
                .append(project)
                .append("\" AND (assignee was \"")
                .append(assignee)
                .append("\" OR assignee = \"")
                .append(assignee)
                .append("\") AND status changed to (\"CLOSED\", \"DONE\", \"READY FOR QA\", \"READY FOR MIGRATION\", \"MIGRATED\", \"DEPENDENT\") during(")
                .append(formatter.format(startDate))
                .append(", ")
                .append(formatter.format(endDate))
                .append(")&maxResults=")
                .append(maxResults)
                .toString();
    }

    private static String buildAuthorization(String user, String token) {
        String authentication = user + ":" + token;
        return Base64.getEncoder().encodeToString(authentication.getBytes());
    }
}
