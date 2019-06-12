package sample.mongoDB;

import com.google.gson.Gson;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import sample.exceptions.NoSuchCurrencyException;
import sample.mongoDB.operations.*;
import sample.neuralNetwork.Network;

import java.util.ArrayList;
import java.util.List;

public class MongoDBClient {
    private static final Logger LOGGER = LogManager.getLogger(MongoDBClient.class.getName());
    private static final String DATABASE_NAME = "nbpCurrencyDatabase";
    public static final String GBP_COLLECTION = "gbpRecords";
    public static final String USD_COLLECTION = "usdRecords";
    public static final String EUR_COLLECTION = "eurRecords";
    public static final String JPY_COLLECTION = "jpyRecords";
    public static final String CHF_COLLECTION = "chfRecords";

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoDBClient() {
        this.mongoClient = new MongoClient("localhost", 27017);
        this.mongoDatabase = mongoClient.getDatabase(DATABASE_NAME);
        LOGGER.warn("Trying to create new mongo database client. Host: localhost, Port: 27017, Database Name: " + DATABASE_NAME);
    }

    public MongoCollection<Document> getCollection(String collectionName) {
        LOGGER.debug("Getting collection from database: " + collectionName);
        return mongoDatabase.getCollection(collectionName);
    }

    public MongoOperations getOperation(String currencyCode) throws NoSuchCurrencyException {
        LOGGER.debug("Getting proper operation class for: " + currencyCode);
        switch (currencyCode) {
            case "GBP":
                return new GBPOperations();
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

    public void saveNetwork(Network network, TextField networkNameField) {
        LOGGER.debug("Attempting to save network: " + networkNameField.getText() + " to database.");
        Gson gson = new Gson();
        String json = gson.toJson(network);
        MongoCollection<Document> collection = this.getCollection("savedDatabases");
        Document document = new Document();
        document.append("networkName", networkNameField.getText());
        document.append("network", json);
        collection.findOneAndReplace(Filters.eq("networkName", networkNameField.getText())
                , document
                , new FindOneAndReplaceOptions().upsert(true));
    }

    public Network loadNetwork(TextField networkNameField) {
        LOGGER.debug("Attempting to load network: " + networkNameField.getText() + " from database.");
        MongoCollection<Document> collection = this.getCollection("savedDatabases");
        List<Document> documents = collection.find(Filters.eq("networkName", networkNameField.getText())).into(new ArrayList<>());
        String json = documents.get(0).getString("network");
        Gson gson = new Gson();
        return gson.fromJson(json, Network.class);
    }
}
