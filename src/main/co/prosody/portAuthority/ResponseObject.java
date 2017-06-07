package co.prosody.portAuthority;

public class ResponseObject {
	private String textOutput;
	private String speechOutput;
	
	public ResponseObject(){
		this.setTextOutput("");
		this.setSpeechOutput("");
	}
	
	public ResponseObject(String textOutput, String speechOutput){
		this.setTextOutput(textOutput);
		this.setSpeechOutput(speechOutput);
	}

	public String getTextOutput() {
		return textOutput;
	}

	public void setTextOutput(String textOutput) {
		this.textOutput = textOutput;
	}

	public String getSpeechOutput() {
		return speechOutput;
	}

	public void setSpeechOutput(String speechOutput) {
		this.speechOutput = speechOutput;
	}
	
	
}
