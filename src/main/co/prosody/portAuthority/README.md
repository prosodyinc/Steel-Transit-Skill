#AWS Lambda function for Alexa
A simple [AWS Lambda](http://aws.amazon.com/lambda) function that implements an Amazon Echo skill using the Alexa SDK that returns the anticipated arrival time of a bus in Pittsburgh. 

## Concepts
This Alexa Skill uses:

 - Web service: communicate with an external web service to get realtiem bus data from Port Authority (TODO: NAME API)
 - Multiple optional slots: has 3 slots (busline, stop, direction), where the user can provide 0, 1, or 2 values, and assumes defaults for the unprovided values
 - Custom slot type: demonstrates using custom slot types to handle a finite set of known values
 - Dialog and Session state: Handles two models, both a one-shot ask and tell model, and a multi-turn dialog model.
   If the user provides an incorrect slot in a one-shot model, it will direct to the dialog model. 
 - SSML: Using SSML tags to control how Alexa renders the text-to-speech.
 - Pre-recorded audio: Uses the SSML 'audio' tag to include a sound in the response.

## Setup
To run this example skill you need to do two things. The first is to deploy the example code in lambda, and the second is to configure the Alexa skill to use Lambda.

### AWS Lambda Setup
1. Go to the AWS Console and click on the Lambda link. Note: ensure you are in us-east or you wont be able to use Alexa with Lambda.
2. Click on the Create a Lambda Function or Get Started Now button.
3. Skip the blueprint
4. Name the Lambda Function "Port-Authority-Skill".
5. Select the runtime as Java 8
6. Go to the the samples/ directory containing pom.xml, and run 'mvn assembly:assembly -DdescriptorId=jar-with-dependencies package'. This will generate a zip file named "alexa-skills-kit-samples-1.0-jar-with-dependencies.jar" in the target directory.
7. Select Code entry type as "Upload a .ZIP file" and then upload the "alexa-skills-kit-samples-1.0-jar-with-dependencies.jar" file from the build directory to Lambda
8. Set the Handler as com.maya.portAuthority.GetNextBusSpeechletRequestStreamHandler (this refers to the Lambda RequestStreamHandler file in the zip).
9. Create a basic execution role and click create.
10. Leave the Advanced settings as the defaults.
11. Click "Next" and review the settings then click "Create Function"
12. Click the "Event Sources" tab and select "Add event source"
13. Set the Event Source type as Alexa Skills kit and Enable it now. Click Submit.
14. Copy the ARN from the top right to be used later in the Alexa Skill Setup.

### Alexa Skill Setup
1. Go to the [Alexa Console](https://developer.amazon.com/edw/home.html) and click Add a New Skill.
2. Set "PortAuthority" as the skill name and "port authority" as the invocation name, this is what is used to activate your skill. For example you would say: "Alexa, Ask port authority when my bus is coming."
3. Select the Lambda ARN for the skill Endpoint and paste the ARN copied from above. Click Next.
4. Copy the custom slot types from the customSlotTypes folder. Each file in the folder represents a new custom slot type. The name of the file is the name of the custom slot type, and the values in the file are the values for the custom slot.
5. Copy the Intent Schema from the included IntentSchema.json.
6. Copy the Sample Utterances from the included SampleUtterances.txt. Click Next.
7. Go back to the skill Information tab and copy the appId. Paste the appId into the PortAuthoritySpeechletRequestStreamHandler.java file for the variable supportedApplicationIds,
   then update the lambda source zip file with this change and upload to lambda again, this step makes sure the lambda function only serves request from authorized source.
8. You are now able to start testing your sample skill! You should be able to go to the [Echo webpage](http://echo.amazon.com/#skills) and see your skill enabled.
9. In order to test it, try to say some of the Sample Utterances from the Examples section below.
10. Your skill is now saved and once you are finished testing you can continue to publish your skill.

## Examples
### One-shot model:

