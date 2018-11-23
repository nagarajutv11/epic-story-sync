package aurea.epicstorysync.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import aurea.epicstorysync.core.Configuration;
import aurea.epicstorysync.core.JiraTicket;
import aurea.epicstorysync.exception.EpicException;

@Service
public class JiraInterface {

    private static final Logger logger = LoggerFactory.getLogger(JiraInterface.class);

    @Autowired
    private Configuration configuration;

    @Autowired
    private RestTemplate restTemplate;

    public boolean updateTicketStatus(JiraTicket ticket, String action) throws EpicException {
        Map<String, String> transactions = loadTransactionNo(ticket);
        if (!transactions.containsKey(action.toLowerCase())) {
            throw new EpicException("Invalid status '" + action + "' updation for ticket: " + ticket);
        }
        String transactionId = transactions.get(action.toLowerCase());
        String query = prepareTransactionQuery(ticket);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String requestJson = "{\"transition\":{\"id\":" + transactionId + "}}";
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            restTemplate.postForEntity(query, entity, String.class);
            return true;
        } catch (HttpClientErrorException e) {
            throw new EpicException(e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new EpicException("Error while fetching transactions for ticket: " + ticket, e);
        }
    }

    public List<JiraTicket> getAllEpicTickets() throws EpicException {
        StringBuilder query = new StringBuilder();
        query.append(configuration.getJiraHost()).append(configuration.getJiraRestPath()).append("/search")
                .append("?jql=").append(configuration.getJql()).append("&fields=status,issuetype,issuelinks");
        try {
            logger.info("Requesting: {}", query);
            logger.info("-----------------------------------------------------------");
            ResponseEntity<String> response = restTemplate.getForEntity(query.toString(), String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            return readTickets(root.path("issues"));
        } catch (HttpClientErrorException e) {
            throw new EpicException(e.getResponseBodyAsString(), e);
        } catch (RestClientException | IOException e) {
            throw new EpicException("Querying JIRA Issues: " + query, e);
        }
    }

    private String prepareTransactionQuery(JiraTicket ticket) {
        StringBuilder query = new StringBuilder();
        query.append(configuration.getJiraHost()).append(configuration.getJiraRestPath()).append("/issue/")
                .append(ticket.getKey()).append("/transitions?expand=transitions.fields");
        return query.toString();
    }

    private Map<String, String> loadTransactionNo(JiraTicket ticket) throws EpicException {
        String query = prepareTransactionQuery(ticket);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(query, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            Map<String, String> transactions = new HashMap<>();
            JsonNode path = root.path("transitions");
            path.forEach(tr -> transactions.put(tr.path("name").textValue().toLowerCase(), tr.path("id").textValue()));
            return transactions;
        } catch (HttpClientErrorException e) {
            throw new EpicException(e.getResponseBodyAsString(), e);
        } catch (RestClientException | IOException e) {
            throw new EpicException("Error while fetching transactions for ticket: " + ticket, e);
        }
    }

    private static List<JiraTicket> readTickets(JsonNode jsonTickets) {
        List<JiraTicket> tickets = new ArrayList<>();
        jsonTickets.forEach(node -> readTicket(node).ifPresent(tickets::add));
        return tickets;
    }

    private static Optional<JiraTicket> readTicket(JsonNode node) {
        String key = node.path("key").textValue();
        if (key == null) {
            return Optional.empty();
        }
        String id = node.path("id").textValue();
        JsonNode fields = node.path("fields");
        String status = fields.path("status").path("name").textValue();
        String type = fields.path("issuetype").path("name").textValue();
        JiraTicket ticket = new JiraTicket(id, key, status, type);
        JsonNode issuelinks = fields.path("issuelinks");
        issuelinks.forEach(link -> link.forEach(issue -> readTicket(issue).ifPresent(ticket::addLinkedTicket)));
        return Optional.of(ticket);
    }
}
