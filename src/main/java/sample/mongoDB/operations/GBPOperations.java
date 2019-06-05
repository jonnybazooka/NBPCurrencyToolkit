package sample.mongoDB.operations;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import sample.dataTransferObjects.Currency;
import sample.mongoDB.MongoDBClient;
import sample.mongoDB.MongoOperations;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GBPOperations implements MongoOperations {

    @Override
    public void insertNewRecord(MongoDBClient mongoDBClient, Currency currency) {
        MongoCollection<Document> collection = mongoDBClient.getCollection(MongoDBClient.GBP_COLLECTION);
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

    @Override
    public List<Document> findAllRecords(MongoDBClient mongoDBClient) {
        MongoCollection<Document> collection = mongoDBClient.getCollection(MongoDBClient.GBP_COLLECTION);
        List<Document> documents = collection.find().into(new ArrayList<>());
        return documents;
    }

    @Override
    public List<Document> findRecordsInDateRange(MongoDBClient mongoDBClient, String start, String end) {
        MongoCollection<Document> collection = mongoDBClient.getCollection(MongoDBClient.GBP_COLLECTION);
        List<Document> documents = collection.find(new Document("date", new Document("$gte", start).append("$lte", end)))
                .into(new ArrayList<>());
        return documents;
    }
}
