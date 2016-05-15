# Purpose

An AWS Lambda to demonstrate round trip encryption without having to 
specify AWS credentials in the Lambda code.

# Prerequisites

Lambda is deployed with a role which has policy to allow kms:Encrypt, 
kms:Decrypt and kms:CreateKey permissions.

# Details
In order to achieve secret-less Lambda, Lambda function is running with 
the role which has the necessary permissions to perform encryption and 
decryption tasks. 

In reality, the real AWS credentials can be encrypted through 
out-of-band process and the encrypted information can then safely be put 
into a file (such as configuration.yml) or on S3 bucket. When Lambda 
function is in need of the AWS credentials for other purposes (such as 
communicating with other AWS services through SDK Client), then the 
encrypted information can be decrypted by using AWS KMS client.

Since Lambda function assumes the role to perform decryption, there is 
no need to provide AWS credentials when using AWS KMS client.
