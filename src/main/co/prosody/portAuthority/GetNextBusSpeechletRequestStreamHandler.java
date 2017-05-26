package co.prosody.portAuthority;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;

public class GetNextBusSpeechletRequestStreamHandler extends
		SpeechletRequestStreamHandler {
    private static final Set<String> supportedApplicationIds = new HashSet<String>();
    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
    	//supportedApplicationIds.add("amzn1.ask.skill.cf8a7d33-3937-446a-911b-8eb17a2b649e"); //Test Transit
        supportedApplicationIds.add("amzn1.echo-sdk-ams.app.4ef08e7f-8b6c-41d9-a2e1-51e56b7f74d8");
        supportedApplicationIds.add("amzn1.echo-sdk-ams.app.247dc98a-b0bb-4d86-9d22-2959e979da3c");
        supportedApplicationIds.add("amzn1.ask.skill.71a52a86-930b-464d-a47b-b488dddf271f");
        supportedApplicationIds.add("amzn1.ask.skill.dc703d7d-92cb-48fd-a631-ce010e590079");
    }

    public GetNextBusSpeechletRequestStreamHandler() {
        super(new GetNextBusSpeechlet(), supportedApplicationIds);
    }
}
