package com.wsdy.saasops.config.amazones;

import com.amazonaws.auth.*;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

/**
 * @author Tony
 * @create 2019-08-22 15:09
 */
public class BatchDefaultAWSCredentialsProviderChain extends AWSCredentialsProviderChain {

    private static final DefaultAWSCredentialsProviderChain INSTANCE = new DefaultAWSCredentialsProviderChain();

    public BatchDefaultAWSCredentialsProviderChain(String accessKey, String secretKey) {
        super(new AWSCredentialsProvider[]{new BatchEnvironmentVariableCredentialsProvider(accessKey, secretKey), new SystemPropertiesCredentialsProvider(), new ProfileCredentialsProvider(), new EC2ContainerCredentialsProviderWrapper()});
    }

    public static DefaultAWSCredentialsProviderChain getInstance() {
        return INSTANCE;
    }
}
