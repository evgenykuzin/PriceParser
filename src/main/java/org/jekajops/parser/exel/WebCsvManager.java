package org.jekajops.parser.exel;

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
import org.jekajops.app.cnfg.AppConfig;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class WebCsvManager implements DataManager {
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


    public WebCsvManager() {
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            range = getAllRange();
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
        InputStream in = WebCsvManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
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

    private String getAllRange() throws IOException {
        var r = service.spreadsheets()
                .get(SPREADSHEET_ID)
                .execute()
                .getSheets().get(0)
                .getProperties()
                .getGridProperties()
                .getRowCount();
        return String.format("A1:AC%d", r);
    }


    @Override
    public Map<String, Map<String, String>> parseMaps() {
        ValueRange response = null;
        try {
            response = getValueRange(range);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response == null) return Map.of();
        majorDimension = response.getMajorDimension();
        List<List<Object>> values = response.getValues();
        var keys = values.get(0);
        values.remove(keys);
        var indices = new ArrayList<Integer>();
        for (int i = 1; i < values.size() + 1; i++) {
            indices.add(i);
        }
        Iterator<Integer> indicesIterator = indices.iterator();
        return values.stream()
                .map((Function<List<Object>, Map<String, String>>) objects -> {
                    var map = new LinkedHashMap<String, String>();
                    map.putIfAbsent("id", String.valueOf(indicesIterator.next()));
                    for (int i = 0; i < objects.size(); i++) {
                        map.put(keys.get(i).toString(), objects.get(i).toString());
                    }
                    map.putIfAbsent(AppConfig.NEW_PRICE_COL_NAME, "");
                    map.putIfAbsent(AppConfig.DIFF_PRICES_COL_NAME, "");
                    return map;
                })
                .collect(Collectors.toMap(map -> map.get("id"), map -> map));
    }

    @Override
    public void writeAll(Collection<Map<String, String>> maps, String[] colNames) {
        try {
            ValueRange valueRange = new ValueRange();
            valueRange.setRange(range);
            valueRange.setMajorDimension(majorDimension);
            List<List<Object>> values = new ArrayList<>();
            values.add(Arrays.asList(colNames));
            var v = maps.stream()
                    .map(map -> new ArrayList<Object>(map.values()))
                    .collect(Collectors.toList());
            values.addAll(v);
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