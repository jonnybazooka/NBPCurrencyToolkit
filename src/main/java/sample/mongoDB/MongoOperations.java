package sample.mongoDB;

import org.bson.Document;
import sample.dataTransferObjects.Currency;
import java.util.List;

public interface MongoOperations {

    void insertNewRecord(MongoDBClient mongoDBClient, Currency currency);

    List<Document> findAllRecords(MongoDBClient mongoDBClient);

    List<Document> findRecordsInDateRange(MongoDBClient mongoDBClient, String start, String end);
}
