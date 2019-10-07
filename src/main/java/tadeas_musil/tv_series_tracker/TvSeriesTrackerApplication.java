package tadeas_musil.tv_series_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
@EnableScheduling
@SpringBootApplication
public class TvSeriesTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TvSeriesTrackerApplication.class, args);
    }

    @Bean
    public WebClient webClient(){                                 
        return WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                                                                                  .followRedirect(true)))
                        .build();
    }

}
