package sample.mongoDB.operations;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.bson.Document;
import sample.dataTransferObjects.Currency;
import sample.mongoDB.MongoDBClient;
import sample.mongoDB.MongoOperations;

public class CHFOperations implements MongoOperations {

    @Override
    public void insertNewRecord(MongoDBClient mongoDBClient, Currency currency) {
        MongoCollection<Document> collection = mongoDBClient.getCollection(MongoDBClient.CHF_COLLECTION);
        Document document = new Document();
        document.append("code", currency.getCode());
        document.append("date", currency.getEffectiveDate().toString());
        document.append("mid", currency.getMidRate());
        collection.findOneAndReplace(
                Filters.eq("date", currency.getEffectiveDate().toString())
                , document
                , new FindOneAndReplaceOptions().upsert(true)
        );
    }
}
