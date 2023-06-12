// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.awscore.internal.interceptor.TracingSystemSetting;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<SQSEvent, String> {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private Gson gson = new Gson();
    private final SqsClient sqsClient = SqsClient.builder()
            .region(Region.of(System.getenv("AWS_REGION")))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .httpClient(UrlConnectionHttpClient.builder().build())
            .build();

    public String handleRequest(final SQSEvent input, final Context context) {
        try {            
            String message = input.getRecords().get(0).getBody();
            
            logger.info("MESSAGE RECEIVED :: " + message);

            //_X_AMZN_TRACE_ID is available as System property for all other runtimes except Java17.
            // The value is retrieved from SystemSettings for Java17 Runtime
            logger.info("TRACE ID : " + TracingSystemSetting._X_AMZN_TRACE_ID.getStringValue().orElse("NOT FOUND"));            
            
            Order order = gson.fromJson(message, Order.class);
            order.setOrderStatus("Processed");
            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .messageBody(gson.toJson(order))
                    .queueUrl(System.getenv("TARGET_SQS_NAME"))
                    .build();
            
            logger.info("Publishing Processed Message back to SQS ...");
            SendMessageResponse result = sqsClient.sendMessage(sendMessageRequest);
            
            return result.messageId();
        } catch (Exception e) {
            logger.error("Error while processing message!", e);
            return "Error!";
        }
    }
}
