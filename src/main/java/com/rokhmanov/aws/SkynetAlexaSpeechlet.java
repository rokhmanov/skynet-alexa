package com.rokhmanov.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;

public class SkynetAlexaSpeechlet implements Speechlet {
	
    private static final String SLOT_SERVER = "server";
    private static final String MOUNT = "/";

    private static final Logger log = LoggerFactory.getLogger(SkynetAlexaSpeechlet.class);

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = intent.getName();
        log.info("INTENT:" + intentName);
        if ("DiskUsageIntent".equals(intentName)) {
            return handleDiskUsageEventRequest(intent, session);
        } else if ("SupportedServersIntent".equals(intentName)) {
        	String serversSpeech = "Supported are server1, server2, server3."; 
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText(serversSpeech);
            return SpeechletResponse.newTellResponse(outputSpeech);            
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            // Create the plain text output.
            String speechOutput =
                    "With Skynet, you can get"
                            + " a current disk usage for servers in your cloud."
                            + " For example, you could say disk usage on server1."
			    + "For a list of supported servers, ask what servers are supported. Now, which server do you want?";

            String repromptText = "Which server do you want?";

            return newAskResponse(speechOutput, false, repromptText, false);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText("Goodbye");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            throw new SpeechletException("Invalid Intent");
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any session cleanup logic would go here
    }

    
    private SpeechletResponse handleDiskUsageEventRequest(final Intent intent, final Session session) {
    	RequestHandlerEx r = new RequestHandlerEx();
    	String speechOutput = "Sorry, I do not understand this. Which server would you like disk usage information for?";
    	String repromptOutput = "Once again please?";
    	try {
    		String serverFromIntent = getServerFromIntent(intent);
    		log.info("Server from intent: ===>" + serverFromIntent + "<===");
    		String serverSystem = getSystemName(serverFromIntent);
    		log.info("Server from system: ===>" + serverSystem + "<===");
        	speechOutput = "The root mount on " + serverFromIntent + " is occupied on " + r.getMountUsageByNode(serverSystem, MOUNT);
        	log.info("FINAL:" + speechOutput);
        } catch (Exception e) {
        	log.error(e.getMessage());
            return newAskResponse(speechOutput, repromptOutput);
        }
        SimpleCard card = new SimpleCard();
        card.setTitle("Skynet");
        card.setContent(speechOutput);

        PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
        outputSpeech.setText(speechOutput);

        return SpeechletResponse.newTellResponse(outputSpeech, card);
    }

    private String getServerFromIntent(Intent intent) throws Exception {
    	Slot serverSlot = intent.getSlot(SLOT_SERVER);
    	if ((null == serverSlot) || (null == serverSlot.getValue())){
    		throw new Exception("SLOT_SERVER undefined");
    	}
    	String server = serverSlot.getValue();
    	return server;
    }
    
    private String getSystemName(String serverFromIntent){
    	String serverId = "unknown";
    	switch (serverFromIntent) {
		case "serverone":
			serverId = "server_1";
			break;

		case "servertwo":
			serverId = "server_2";
			break;
			
		case "serverthree":
			serverId = "server_3";
			break;

		default:
			break;
		}
    	return serverId;
    }
    
    /**
     * Function to handle the onLaunch skill behavior.
     * 
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechOutput = "Skynet. What server you want disk usage for?";
        // If the user either does not reply to the welcome message or says something that is not
        // understood, they will be prompted again with this text.
        String repromptText =
                "With Skynet, you can get a server disk usage. "
                        + " For example, you could say server1, or server2."
                        + " Now, which server do you want?";

        return newAskResponse(speechOutput, false, repromptText, false);
    }


    /**
     * Wrapper for creating the Ask response from the input strings with
     * plain text output and reprompt speeches.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, String repromptText) {
        return newAskResponse(stringOutput, false, repromptText, false);
    }

    /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param isOutputSsml
     *            whether the output text is of type SSML
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml
     *            whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
            String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(stringOutput);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }

        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

}
