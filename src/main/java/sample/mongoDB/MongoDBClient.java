package sample.mongoDB;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sample.exceptions.NoSuchCurrencyException;
import sample.mongoDB.operations.CHFOperations;
import sample.mongoDB.operations.EUROperations;
import sample.mongoDB.operations.JPYOperations;
import sample.mongoDB.operations.USDOperations;

public class MongoDBClient {
    private static final String DATABASE_NAME = "nbpCurrencyDatabase";
    public static final String USD_COLLECTION = "usdRecords";
    public static final String EUR_COLLECTION = "eurRecords";
    public static final String JPY_COLLECTION = "jpyRecords";
    public static final String CHF_COLLECTION = "chfRecords";

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDBClient() {
        this.mongoClient = new MongoClient("localhost", 27017);
        this.mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        return mongoDatabase.getCollection(collectionName);
    }

    public MongoOperations getOperation(String currencyCode) throws NoSuchCurrencyException {
        switch (currencyCode) {
            case "USD":
                return new USDOperations();
            case "EUR":
                return new EUROperations();
            case "JPY":
                return new JPYOperations();
            case "CHF":
                return new CHFOperations();
            default:
                throw new NoSuchCurrencyException("Currency code not recognized.");
        }
    }
}
