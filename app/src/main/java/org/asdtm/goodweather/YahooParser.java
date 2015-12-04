package org.asdtm.goodweather;

import android.util.Log;

import org.asdtm.goodweather.model.CitySearch;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class YahooParser
{
    private static final String TAG = "YahooParser";
    private static String BASE_URL = "http://where.yahooapis.com/v1/";
    private static String APPID = "";
    private static int COUNT_CITY = 10;

    public static List<CitySearch> getCity(String city)
    {
        List<CitySearch> resultSearch = new ArrayList<CitySearch>();
        HttpURLConnection whereConnection = null;
        try {
            String query = buildSearchQuery(city);
            URL urlQuery = new URL(query);
            whereConnection = (HttpURLConnection) urlQuery.openConnection();
            InputStream inputStream = whereConnection.getInputStream();

            XmlPullParser pullParser = XmlPullParserFactory.newInstance().newPullParser();
            pullParser.setInput(new InputStreamReader(inputStream));
            int event = pullParser.getEventType();

            CitySearch citySearch = null;
            String tagName = null;
            String currentTag = null;

            while (event != XmlPullParser.END_DOCUMENT) {
                tagName = pullParser.getName();

                if (event == XmlPullParser.START_TAG) {
                    if (tagName.equals("place")) {
                        citySearch = new CitySearch();
                    }
                    currentTag = tagName;
                } else if (event == XmlPullParser.TEXT) {
                    if ("name".equals(currentTag)) {
                        assert citySearch != null;
                        citySearch.setCityName(pullParser.getText());
                    } else if ("country".equals(currentTag)) {
                        assert citySearch != null;
                        citySearch.setCountry(pullParser.getText());
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if ("place".equals(tagName)) {
                        resultSearch.add(citySearch);
                    }
                }

                event = pullParser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        } finally {
            assert whereConnection != null;
            whereConnection.disconnect();
        }
        return resultSearch;
    }

    private static String buildSearchQuery(String city)
    {
        city = city.replaceAll(" ", "%20");
        return BASE_URL + "places.q(" + city + "%2A);count=" + COUNT_CITY + "?appid=" + APPID;
    }
}