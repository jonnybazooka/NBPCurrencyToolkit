package sample.mongoDB;

import sample.dataTransferObjects.Currency;

public interface MongoOperations {

    void insertNewRecord(MongoDBClient mongoDBClient, Currency currency);
}
