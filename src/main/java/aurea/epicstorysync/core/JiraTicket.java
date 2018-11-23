package aurea.epicstorysync.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JiraTicket {

    private String id;
    private String key;
    private String status;
    private String type;
    private List<JiraTicket> linkedTickets = new ArrayList<>();

    public JiraTicket(String id, String key, String status, String type) {
        this.id = id;
        this.key = key;
        this.status = status;
        this.type = type;
    }

    public void addLinkedTicket(JiraTicket ticket) {
        linkedTickets.add(ticket);
    }

    public List<JiraTicket> getLinkedTicketsByType(String ticketType) {
        return linkedTickets.stream().filter(t -> t.getType().equalsIgnoreCase(ticketType))
                .collect(Collectors.toList());
    }

    public int getLinkedStatusCount() {
        return linkedTickets.size();
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return type + " #" + key + " @" + status;
    }
}
