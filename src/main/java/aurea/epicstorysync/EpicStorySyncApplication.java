package aurea.epicstorysync;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import aurea.epicstorysync.service.EpicStoryStatusSynchronizer;

@SpringBootApplication
public class EpicStorySyncApplication implements CommandLineRunner {

    @Autowired
    private EpicStoryStatusSynchronizer syncronizer;

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(EpicStorySyncApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        ConfigurableApplicationContext context = app.run(args);
        context.close();
    }

    @Override
    public void run(String... args) throws Exception {
        syncronizer.start();
    }
}
