package com.wsdy.saasops.config.amazones;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.util.StringUtils;

/**
 * @author Tony
 * @create 2019-08-22 15:05
 */
public class BatchEnvironmentVariableCredentialsProvider implements AWSCredentialsProvider {
    private String accessKey;
    private String secretKey;

    public BatchEnvironmentVariableCredentialsProvider(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Override
    public AWSCredentials getCredentials() {
        accessKey = StringUtils.trim(accessKey);
        secretKey = StringUtils.trim(secretKey);
        String sessionToken = StringUtils.trim(System.getenv("AWS_SESSION_TOKEN"));
        if (!StringUtils.isNullOrEmpty(accessKey) && !StringUtils.isNullOrEmpty(secretKey)) {
            return (AWSCredentials) (sessionToken == null ? new BasicAWSCredentials(accessKey, secretKey) : new BasicSessionCredentials(accessKey, secretKey, sessionToken));
        } else {
            throw new SdkClientException("Unable to load AWS credentials from environment variables (AWS_ACCESS_KEY_ID (or AWS_ACCESS_KEY) and AWS_SECRET_KEY (or AWS_SECRET_ACCESS_KEY))");
        }
    }

    @Override
    public void refresh() {
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }
}
