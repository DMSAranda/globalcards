package com.santander.globalcards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {
    
    private String bucket;
    private String inputFolder;
    private String outputFolder;
    
    // Getters and Setters
    public String getBucket() {
        return bucket;
    }
    
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    
    public String getInputFolder() {
        return inputFolder;
    }
    
    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }
    
    public String getOutputFolder() {
        return outputFolder;
    }
    
    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
}
