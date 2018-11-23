package aurea.epicstorysync.core;

public class JiraStatus {

    private String epicStatus;
    private String storyStatus;
    private int number;

    public void setEpicStatus(String epicStatus) {
        this.epicStatus = epicStatus;
    }

    public String getEpicStatus() {
        return epicStatus;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public void setStoryStatus(String storyStatus) {
        this.storyStatus = storyStatus;
    }

    public String getStoryStatus() {
        return storyStatus;
    }

    @Override
    public String toString() {
        return storyStatus;
    }
}
