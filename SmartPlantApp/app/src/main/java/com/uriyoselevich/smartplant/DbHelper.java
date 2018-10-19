package com.uriyoselevich.smartplant;

import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.ScanFilter;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Search;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.datatype.Document;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DbHelper {
    private static DbHelper dbHelper;//singleton
    private AmazonDynamoDBClient dbClient;


    private final CognitoCachingCredentialsProvider credentialsProvider;

    private DbHelper(Context context) {
        // Create a new credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                context, "us-east-1:XXXXXX-XXX-XXXXX-XXXXX", Regions.US_EAST_1);

        // Create a connection to DynamoDB
        dbClient = new AmazonDynamoDBClient(credentialsProvider);

    }

    public static DbHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DbHelper(context);
        }
        return dbHelper;
    }


    public void getCurrentMonthData(OnCurrentMonthDataListener currentMonthDataListener) {
        new GetCurrentMonthData(dbClient, currentMonthDataListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

//    /**
//     * create a new memo in the database
//     *
//     * @param memo the memo to create
//     */
//    public void create(Document memo) {
//        if (!memo.containsKey("userId")) {
//            memo.put("userId", credentialsProvider.getCachedIdentityId());
//        }
//        if (!memo.containsKey("noteId")) {
//            memo.put("noteId", UUID.randomUUID().toString());
//        }
//        if (!memo.containsKey("creationDate")) {
//            memo.put("creationDate", System.currentTimeMillis());
//        }
//        dbTable.putItem(memo);
//    }
//
//    /**
//     * Update an existing memo in the database
//     *
//     * @param memo the memo to save
//     */
//    public void update(Document memo) {
//        Document document = dbTable.updateItem(memo, new UpdateItemOperationConfig().withReturnValues(ReturnValue.ALL_NEW));
//    }
//
//
//    /**
//     * Retrieve a memo by noteId from the database
//     *
//     * @param noteId the ID of the note
//     * @return the related document
//     */
//    public Document getMemoById(String noteId) {
//        return dbTable.getItem(new Primitive(credentialsProvider.getCachedIdentityId()), new Primitive(noteId));
//    }


    public static class GetCurrentMonthData extends AsyncTask<Void, Void, List<Document>> {

        private Table dbTable;
        private final AmazonDynamoDBClient dbClient;
        private OnCurrentMonthDataListener currentMonthDataListener;

        GetCurrentMonthData(AmazonDynamoDBClient dbClient, OnCurrentMonthDataListener currentMonthDataListener) {
            this.dbClient = dbClient;
            this.currentMonthDataListener = currentMonthDataListener;
        }

        @Override
        protected List<Document> doInBackground(Void... voids) {
            dbTable = Table.loadTable(dbClient, "IoTData");

            // create calendar for a month ago:
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -30);

            ArrayList<AttributeValue> monthAgo = new ArrayList<>();
            monthAgo.add(new AttributeValue(String.valueOf(cal.getTimeInMillis())));

            //look in DynamoDB for last month timestamps:
            Search search = dbTable.scan(new ScanFilter("timestamp", ComparisonOperator.GT, monthAgo));

            return search.getAllResults();
        }

        @Override
        protected void onPostExecute(List<Document> documents) {
            currentMonthDataListener.onCurrentMonthData(documents);
        }
    }


    public interface OnCurrentMonthDataListener {
        void onCurrentMonthData(List<Document> documents);
    }

}
