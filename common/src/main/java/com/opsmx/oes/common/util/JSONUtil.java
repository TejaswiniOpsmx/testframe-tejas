package com.opsmx.oes.common.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class JSONUtil {

    static JSONObject applicationJson;

    public static JSONObject getJSON(String jsonFile) throws IOException {
        FileInputStream fileInputStream;
        BufferedReader bufferReader = null;
        JSONParser jsonParser;
        Object obj;
        try {
            fileInputStream = new FileInputStream(jsonFile);
            bufferReader = new BufferedReader(new InputStreamReader(fileInputStream));
            jsonParser = new JSONParser();
            obj = jsonParser.parse(bufferReader);
            applicationJson = (JSONObject) obj;
        } catch (Exception e) {
        	applicationJson = null;
        } finally {
            if (bufferReader != null) {
                bufferReader.close();
            }
        }
        return applicationJson;
    }

}