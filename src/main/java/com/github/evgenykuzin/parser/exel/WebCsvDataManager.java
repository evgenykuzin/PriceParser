package com.github.evgenykuzin.parser.exel;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.github.evgenykuzin.app.loger.Loggable;
import com.github.evgenykuzin.entities.Table;
import com.github.evgenykuzin.util.FileManager;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.*;

import static com.github.evgenykuzin.app.cnfg.TableConfig.WebDocConfig.*;

public abstract class WebCsvDataManager implements DataManager, Loggable {
    private static final String APPLICATION_NAME = "Parser";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    public static final String SPREADSHEET_ID = "1E3VpDsMzSJe1hVbzf2Kz-T3sVzQAG8P_YGOQwtrNgrc";
    public static final int GID = 0;
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private Sheets service;
    private String majorDimension;
    private String range;


    public WebCsvDataManager() {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            range = getAllRange(GID);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = new FileInputStream(FileManager.getFromResources(CREDENTIALS_FILE_PATH));
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user3");
    }

    private ValueRange getValueRange(String range) throws IOException {
        return service.spreadsheets()
                .values()
                .get(SPREADSHEET_ID, range)
                .execute();

    }

    private String getAllRange(int sheet) throws IOException {
        var r = service.spreadsheets()
                .get(SPREADSHEET_ID)
                .execute()
                .getSheets().get(1)
                .getProperties()
                .getGridProperties()
                .getRowCount();
        return String.format("A1:AZ%d", r);
    }

    private double getDiffPricesInt(Map<String, String> map) {
        var str = map.get(DIFF_PRICES_COL_NAME);
        if (str.isEmpty()) return 0;
        return Double.parseDouble(str);
    }

    @Override
    public Table parseTable() {
        ValueRange response = null;
        try {
            response = getValueRange(range);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) return Table.getEmptyTable();
        majorDimension = response.getMajorDimension();
        List<List<Object>> values = response.getValues();
        var keys = values.get(0);
        values.remove(keys);
        return new Table(ID_COL_NAME, keys, values,
                raw -> raw.putIfAbsent(ID_COL_NAME, String.valueOf(values.stream()
                        .map(objects -> Integer.parseInt(objects.get(0).toString()))
                        .max(Integer::compare)
                        .orElseGet(() -> new Random().nextInt(1000000)))),
                raw -> {
                    raw.putIfAbsent(LOWER_PRICE_COL_NAME, "");
                    raw.putIfAbsent(DIFF_PRICES_COL_NAME, "");
                    raw.putIfAbsent(CONCURRENT_URL_COL_NAME, "");
                    raw.putIfAbsent(SEARCH_BARCODE_COL_NAME, "");
                });
    }

    @Override
    public void writeAll(Table table) {
        try {
            ValueRange valueRange = new ValueRange();
            var range = getAllRange(GID);
            valueRange.setRange(range);
            valueRange.setMajorDimension(majorDimension);
            List<List<Object>> values = table.getValuesMatrix(Comparator.comparingDouble(this::getDiffPricesInt));
            valueRange.setValues(values);
            service.spreadsheets().values()
                    .update(SPREADSHEET_ID, range, valueRange)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}