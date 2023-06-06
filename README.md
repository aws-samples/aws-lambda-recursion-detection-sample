# AWS Lambda Recursion Controls Sample

This is a sample application to demonstrate preventitive controls to detect recursion in AWS Lambda functions.
You can find more details about this feature in this [blog post]().

The sample application is a simple order processing app with following architecture.

![Sample Application Architecture](images/RecursionBlog.jpg)


1. Order is sent to a Source SQS queue
2. The message is processed by Lambda function from source queue using the Event Source Mapping
3. The Lambda function sends updated order to the destination SQS queue via SQS SendMessage API calls.
4. Updated order is sent to the Source SQS queue due to mis-configuration instead of the destination queue.
5. Lambda service detects the recursion and stops invoking the lambda function after 16 tries. The message is delivered to the dead letter queue as per the redrive policy configuration.

### Deployment

#### Pre-requisites
1. AWS Account
2. Java 11
3. Maven
3. AWS [SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html) v1.82.0

#### CLI Commands

1.	Clone the git repository and change to project directory

	$ git clone https://github.com/aws-samples/aws-lambda-recursion-detection-sample

2.	Use the AWS SAM CLI to build the application

    $ sam build

3.	Use the AWS SAM CLI to deploy the resources to your AWS account.

    $ sam deploy -g

This will deploy your Lambda function with source and target SQS queues.

Please note down the Source SQS Queue URL after deployment

### Test

	$ aws sqs send-message --queue-url <SOURCE_SQS_URL> --message-body {\"orderId\":"1",\"productName\":\"Bolt\",\"orderStatus\":\"Submitted\"}

### Cleanup
    $ sam delete

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This library is licensed under the MIT-0 License. See the LICENSE file.