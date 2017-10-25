package com.wenganse.android.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Plus on 16.05.2017.
 */

//класса для работы с хранимым запросом
//QueryPreferences предоставляет всю функциональность долгосрочного хранения данных для PhotoGallery.
public class QueryPreferences {

    //PREF_SEARCH_QUERY используется в качестве ключа для хранения запроса.
    // Этот ключ применяется во всех операциях чтения или записи запроса.
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    //константа для хранения идентификатора последней загруженной фотографии.
    private static final String PREF_LAST_RESULT_ID = "lastResultId";
    //коснтанта для хранения состояния сигнала
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    // возвращает значение запроса, хранящееся в общих настройках.
    public static String getStoredQuery(Context context){
        // метод сначала получает объект SharedPreferences по умолчанию для заданного контекста.
        //Второй параметр .getString(PREF_SEARCH_ QUERY, null) определяет возвращаемое значение по умолчанию,
        // которое должно возвращаться при отсутствии записи с ключом PREF_SEARCH_QUERY.
        return PreferenceManager.getDefaultSharedPreferences( context ).getString( PREF_SEARCH_QUERY, null );
    }

    // записывает запрос в хранилище общих настроек для заданного контекста.
    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences( context )
                //SharedPreferences. edit() используется для получения экземпляра SharedPreferences.Editor.
                .edit()
                .putString( PREF_SEARCH_QUERY, query )
                // Метод apply() вносит изменения в память немедленно,
                // а непосредственная запись в файл осуществляется в фоновом потоке.
                .apply();
    }

    public static String getLastResultId(Context context){
        return PreferenceManager.getDefaultSharedPreferences( context ).getString(PREF_LAST_RESULT_ID, null );
    }
    public static void setLastResultId(Context context, String lastResultId){
        PreferenceManager.getDefaultSharedPreferences( context )
                .edit()
                .putString( PREF_LAST_RESULT_ID, lastResultId )
                .apply();

    }
    //Настройки для хранения сигнала.
    public static boolean isAlarmOn(Context context){
        return PreferenceManager.getDefaultSharedPreferences( context )
                .getBoolean( PREF_IS_ALARM_ON, false );
    }

    public static void setAlarmOn(Context context, boolean isON){
        PreferenceManager.getDefaultSharedPreferences( context )
                .edit()
                .putBoolean( PREF_IS_ALARM_ON, isON )
                .apply();
    }
}
