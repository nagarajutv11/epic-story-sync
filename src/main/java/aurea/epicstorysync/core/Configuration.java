package aurea.epicstorysync.core;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import aurea.epicstorysync.exception.EpicException;

@Component
@ConfigurationProperties(prefix = "config")
public class Configuration {

    private List<JiraStatus> statuses;

    private String jql;

    private String jiraHost;

    private String jiraRestPath;

    private Map<String, String[]> actions;

    private Map<String, JiraStatus> statusesByName;

    public void setActions(Map<String, String[]> actions) {
        this.actions = actions;
    }

    public Map<String, String[]> getActions() {
        return actions;
    }

    public void setStatuses(List<JiraStatus> statuses) {
        this.statuses = statuses;
    }

    public List<JiraStatus> getStatuses() {
        return statuses;
    }

    public void setJql(String jql) {
        this.jql = jql;
    }

    public String getJql() {
        return jql;
    }

    public void setJiraRestPath(String jiraRestPath) {
        this.jiraRestPath = jiraRestPath;
    }

    public String getJiraRestPath() {
        return jiraRestPath;
    }

    public void setJiraHost(String jiraHost) {
        this.jiraHost = jiraHost;
    }

    public String getJiraHost() {
        return jiraHost;
    }

    public JiraStatus findHigherPriorityEpicStatus(List<JiraTicket> linkedTickets) throws EpicException {
        JiraStatus higherStatus = null;
        for (JiraTicket ticket : linkedTickets) {
            JiraStatus status = findStatus(ticket.getStatus());
            if (status == null) {
                throw new EpicException(String.format("Invalid status: %s in ticket: %s", ticket.getStatus(), ticket));
            }
            if (higherStatus == null) {
                higherStatus = status;
                continue;
            }
            if (status.getNumber() < higherStatus.getNumber()) {
                higherStatus = status;
            }
        }
        return higherStatus;
    }

    private JiraStatus findStatus(String status) {
        return getStatusByName().get(status.toLowerCase());
    }

    private Map<String, JiraStatus> getStatusByName() {
        if (statusesByName == null) {
            statusesByName = statuses.stream().collect(Collectors.toMap(s -> s.getStoryStatus().toLowerCase(), s -> s));
        }
        return statusesByName;
    }

    public String[] getAction(JiraTicket ticket, String status) {
        String actionString = ticket.getType() + "." + ticket.getStatus() + "." + status;
        actionString = actionString.replaceAll(" ", "_").toLowerCase();
        return actions.get(actionString);
    }
}
