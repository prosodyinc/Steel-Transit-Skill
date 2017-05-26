package co.prosody.portAuthority.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Model representing an item of the PaUserData table in DynamoDB 
 */
@DynamoDBTable(tableName = "PPAAS_User_Data")
public class PaUserDataItem {
	private static Logger log = LoggerFactory.getLogger(PaUserDataItem.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String customerId;

    private PaInputData inputData;

    @DynamoDBHashKey(attributeName = "CustomerId")
    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    @DynamoDBAttribute(attributeName = "Data")
    @DynamoDBMarshalling(marshallerClass = PaInputDataMarshaller.class)
    public PaInputData getInputData() {
        return inputData;
    }

	public void setInputData(PaInputData inputData) {
        this.inputData = inputData;
		
	}
  

    /**
     * A {@link DynamoDBMarshaller} that provides marshalling and unmarshalling logic for
     * {@link PaInputData} values so that they can be persisted in the database as String.
     */
    public static class PaInputDataMarshaller implements
            DynamoDBMarshaller<PaInputData> {
    	private static Logger log = LoggerFactory.getLogger(PaInputDataMarshaller.class);
        @Override
        public String marshall(PaInputData inputData) {
            try {
                return OBJECT_MAPPER.writeValueAsString(inputData);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to marshall input data", e);
            }
        }

        @Override
        public PaInputData unmarshall(Class<PaInputData> clazz, String value) {
            try {
                return OBJECT_MAPPER.readValue(value, new TypeReference<PaInputData>() {
                });
            } catch (Exception e) {
                throw new IllegalStateException("Unable to unmarshall input data value:"+value, e);
            }
        }
    }


}
