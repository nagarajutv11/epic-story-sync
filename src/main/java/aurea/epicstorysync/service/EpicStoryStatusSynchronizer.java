package aurea.epicstorysync.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import aurea.epicstorysync.core.Configuration;
import aurea.epicstorysync.core.JiraStatus;
import aurea.epicstorysync.core.JiraTicket;
import aurea.epicstorysync.exception.EpicException;

@Service
public class EpicStoryStatusSynchronizer {

    private static final Logger logger = LoggerFactory.getLogger(EpicStoryStatusSynchronizer.class);

    private static final String TYPE_BASELINE = "Faster Baseline";
    private static final String TYPE_STORY = "Faster Story";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String STATUS_TO_DEFINE = "To Define";
    private static final String STATUS_BACKLOG = "Backlog";
    private static final String STATUS_NEW = "New";

    @Autowired
    private JiraInterface jiraInterface;

    @Autowired
    private Configuration configuration;

    public void start() throws EpicException {
        logger.info("===========================================================");
        List<JiraTicket> tickets = jiraInterface.getAllEpicTickets();
        logger.info("Loaded {} tickets", tickets.size());

        long count = tickets.parallelStream().filter(this::updateStatus).count();

        logger.info("{} Epic was Synchronized", count);
        logger.info("===========================================================");
    }

    private boolean updateStatus(JiraTicket epicTicket) {
        logger.info("-----------------------------------------------------------");
        try {
            logger.info("Checking ticket: \t\t{}", epicTicket);
            if (epicTicket.getLinkedStatusCount() == 0) {
                logger.debug("No linked issues for this ticket: {}", epicTicket);
                return false;
            }
            List<JiraTicket> linkedStories = epicTicket.getLinkedTicketsByType(TYPE_STORY);
            if (linkedStories.isEmpty()) {
                logger.debug("No stories for this ticket: {}", epicTicket);
                return false;
            }

            JiraStatus resultStoryStatus = configuration.findHigherPriorityEpicStatus(linkedStories);
            logger.info("Result Story status: \t{}", resultStoryStatus);
            if (STATUS_TO_DEFINE.equalsIgnoreCase(resultStoryStatus.getStoryStatus())
                    && STATUS_BACKLOG.equalsIgnoreCase(epicTicket.getStatus())) {
                return applyReverseSync(epicTicket);
            } else if (STATUS_TO_DEFINE.equalsIgnoreCase(resultStoryStatus.getStoryStatus())
                    && STATUS_NEW.equalsIgnoreCase(epicTicket.getStatus()) && hasOnlyCompletedBaseLine(epicTicket)) {
                return applyBaselineStorySync(epicTicket);
            } else {
                return applyStandardSync(epicTicket, resultStoryStatus);
            }
        } catch (EpicException e) {
            logger.error("Error while process ticket: " + epicTicket, e);
        } finally {
            logger.info("-----------------------------------------------------------");
        }
        return false;
    }

    private boolean hasOnlyCompletedBaseLine(JiraTicket epicTicket) {
        List<JiraTicket> baseLineStories = epicTicket.getLinkedTicketsByType(TYPE_BASELINE);
        if (baseLineStories.isEmpty()) {
            return false;
        }
        for (JiraTicket ticket : baseLineStories) {
            if (!STATUS_COMPLETED.equalsIgnoreCase(ticket.getStatus())) {
                return false;
            }
        }
        return true;
    }

    private boolean applyBaselineStorySync(JiraTicket epicTicket) throws EpicException {
        logger.info("Applying: \t\t\tBaseline Story Sync");
        boolean applied = updateTicketStatus(epicTicket, STATUS_BACKLOG);
        boolean updated = updateToDefineStoryStatusToBacklog(epicTicket);
        return applied || updated;
    }

    private boolean applyReverseSync(JiraTicket epicTicket) throws EpicException {
        logger.info("Applying: \t\t\tReverse Sync");
        return updateToDefineStoryStatusToBacklog(epicTicket);
    }

    private boolean applyStandardSync(JiraTicket epicTicket, JiraStatus status) throws EpicException {
        logger.info("Applying: \t\t\tStandard Sync");
        return updateTicketStatus(epicTicket, status.getEpicStatus());
    }

    private boolean updateToDefineStoryStatusToBacklog(JiraTicket epicTicket) throws EpicException {
        List<JiraTicket> collect = epicTicket.getLinkedTicketsByType(TYPE_STORY);
        boolean applied = false;
        for (JiraTicket ticket : collect) {
            if (STATUS_TO_DEFINE.equalsIgnoreCase(ticket.getStatus())) {
                boolean updated = updateTicketStatus(ticket, STATUS_BACKLOG);
                if (updated) {
                    applied = true;
                }
            }
        }
        return applied;
    }

    private boolean updateTicketStatus(JiraTicket ticket, String status) throws EpicException {
        if (ticket.getStatus().equalsIgnoreCase(status)) {
            return false;
        }

        String[] actionRule = configuration.getAction(ticket, status);
        if (actionRule == null || actionRule.length < 2) {
            logger.error("Action Rule not defined ticket: {} , for status: {}", ticket, status);
            return false;
        }

        jiraInterface.updateTicketStatus(ticket, actionRule[0]);
        logger.info("Updated status: \t\t{} to status {}", ticket, actionRule[1]);
        ticket.setStatus(actionRule[1]);
        updateTicketStatus(ticket, status);
        return true;
    }
}
