package tadeas_musil.tv_series_tracker.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import lombok.Getter;


@Component
public class AppContextUtils implements ApplicationContextAware {
    
    @Getter
    private static ApplicationContext ctx;
   
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }



 

}