package com.carddemo.cardservice.migration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    private String carddataPath;
    private String cardxrefPath;

    public String getCarddataPath() {
        return carddataPath;
    }

    public void setCarddataPath(String carddataPath) {
        this.carddataPath = carddataPath;
    }

    public String getCardxrefPath() {
        return cardxrefPath;
    }

    public void setCardxrefPath(String cardxrefPath) {
        this.cardxrefPath = cardxrefPath;
    }
}
