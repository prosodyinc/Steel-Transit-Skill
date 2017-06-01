package co.prosody.portAuthority.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.speechlet.Session;

/**
 * Contains the methods to interact with the persistence layer for Port Authority in DynamoDB.
 */
public class PaDao {
	private static Logger log = LoggerFactory.getLogger(PaDao.class);
    private final PaDynamoDbClient dynamoDbClient;

    public PaDao(PaDynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void deletePaInput(String id){
    	PaUserDataItem item = new PaUserDataItem();
        item.setCustomerId(id);
        dynamoDbClient.deleteItem(item);
    }
    
    /**
     * Reads and returns the user information from the session.
     * <p>
     * Returns null if the item could not be found in the database.
     * 
     * @param id The user ID
     * @return the input data, if it exists, associated with this id
     */
    public PaInputData getPaInputData(String id) {
        PaUserDataItem item = new PaUserDataItem();
        item.setCustomerId(id);

        item = dynamoDbClient.loadItem(item);

        if (item == null) {
            return null;
        }

        return item.getInputData();
    }

    /**
     * Saves the {@link PaInputData} into the database.
     * 
     * @param input
     */
    public void savePaInput(PaInputData input) {
        PaUserDataItem item = new PaUserDataItem();
        item.setCustomerId(input.getID());
        item.setInputData(input);
        dynamoDbClient.saveItem(item);
    }
}
