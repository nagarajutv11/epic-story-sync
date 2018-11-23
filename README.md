#Epic-Story-Sync Tool 

The Epic-Story-Sync Tool helps to sync `Faster Epic` tickets status based on their linked issues. Current work process 
implies to update it directly by hand. So Epic status depends on stories statuses in most of the cases it will be out of date. 
This tool will help to update statuses automatically.

For more information, see [wiki](https://confluence.devfactory.com/display/EN/Epic-Story+sync%3A+DEV+documentation) 

##Requirements
1. JDK version >=1.8
2. maven

##Installing
- Clone repository

```git clone git@github.com:trilogy-group/faster-scripts.git```
- Move to Epic-Story-Sync root folder 

```cd faster-scripts/epic-story-sync```

##Running
- Change JIRA Credentials in **src\main\resources\application.properties**
 ```
 jira.username=YOUR_JIRA_USERNAME
 jira.password=YOUR_JIRA_PASSWORD
 ```
- Build jar file from **faster-scripts\epic-story-sync**

```mvn clean package -DskipTests```
- Execute from **faster-scripts\epic-story-sync**

```java -jar target\epic-story-sync.jar```
 
 
 
 
 
 
 
 
