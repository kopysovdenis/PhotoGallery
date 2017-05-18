package com.wenganse.android.photogallery;


import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Plus on 14.05.2017.
 */
//Основа для сетевого кода.
public class FlickrFetchr {

    public static final String TAG ="FlickrFetchr";
    public static final String API_KEY="8304d59fb2c7ff6878f709f17ecf2121";


    //Добавление констант для строки поиска и частей URL-адреса.
    private static final String FETCH_RECENTS_METHOD = "flickr.photo.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri.
            // Uri.Builder — вспомогательный класс
            // для создания параметризованных URL-адресов с правильным кодированием символов.
    parse( "https://api.flickr.com/services/rest/" )
            .buildUpon()
    //Метод Uri. Builder.appendQueryParameter(String,String)
    // автоматически кодирует строки запросов.
    .appendQueryParameter( "api_key", API_KEY )
    .appendQueryParameter( "format", "json" )
    .appendQueryParameter( "nojsoncallback", "1" )
    .appendQueryParameter( "extras", "url_s" )
    .build();

    //Метод getUrlBytes(String) получает низкоуровневые данные по URL
    // и возвращает их в виде массива байтов.
    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL( urlSpec );
        // создает объект подключения к заданному URL-адресу.
        // Вызов URL.openConnection() возвращает URLConnection,
        // но поскольку подключение осуществляется по протоколу HTTP,
        //можем преобразовать его в HttpURLConnection.
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(  );
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException( connection.getResponseMessage()
                        + ":with"
                        + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead =in.read(buffer))> 0 ){
                out.write( buffer, 0 , bytesRead );
            }
            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    //Метод getUrlString(String) преобразует результат
    // из getUrlBytes(String) в String.
    public String getUrlString (String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }
    //Метод инициализирующий загрузку, он строит URL и вызывает downloaderGalleryItems(String).
    public List<GalleryItem> fetchRecentPhotos(){
        String url = buildUrl( FETCH_RECENTS_METHOD, null );
        return downloadGalleryItems( url );
    }
    //Метод для поиска фотографий
    public List<GalleryItem> searchPhoto(String query){
        String url = buildUrl( SEARCH_METHOD, query );
        return downloadGalleryItems( url );
    }

    //метод downloadGalleryItems(String) получает URL-адрес, поэтому строить URL внутри уже не нужно
    private List<GalleryItem> downloadGalleryItems(String url){

        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString( url );
            Log.i( TAG, "Received JSON: " + jsonString );
            //чтение JSON в JSON object
            JSONObject jsonBody = new JSONObject( jsonString );
            parseItems( items, jsonBody );
        }catch (JSONException je){
            Log.e( TAG, "Failed to parse JSON", je );
        }catch (IOException ioe){
            Log.e( TAG, "Failed to fetch items" , ioe);
        }
        return items;
    }

    //Вспомогательный метод для построения URL по значениям метода и запроса.
    //Метод buildUrl(…) присоединяет необходимые параметры
    private String buildUrl(String method, String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendQueryParameter( "method", method );
        if (method.equals( SEARCH_METHOD )){
            uriBuilder.appendQueryParameter("Text", query );
        }
        return uriBuilder.build().toString();
    }

    // метод для извлечения информации каждой фотографии.
    private void parseItems(List<GalleryItem> items, JSONObject jsonBody)
        throws IOException, JSONException{
        JSONObject photosJsonObject = jsonBody.getJSONObject( "photos" );
        JSONArray photoJsonArray = photosJsonObject.getJSONArray( "photo" );

        for (int i = 0; i < photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject( i );

            GalleryItem item = new GalleryItem();
            item.setId( photoJsonObject.getString( "id" ) );
            item.setCaption( photoJsonObject.getString( "title" ) );

            //Проверка на наличие уменьшенного изображения
            if (!photoJsonObject.has( "url_s" )){
                continue;
            }

            item.setUri( photoJsonObject.getString( "url_s" ) );
            items.add( item );
        }
    }
}
