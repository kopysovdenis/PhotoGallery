package com.wenganse.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Plus on 15.05.2017.
 */
//Исходная версия кода потока (ThumbnailDownloader.java)
public class ThumbnailDownloader<T> extends HandlerThread {
    //Константы и переменные для загрузки байтов
    // и приобразовании их ва Растровые изображения.
    //MESSAGE_DOWNLOAD Используется для идентификации сообщений как запросов на загрузку.
    // ThumbnailDownloader присваивает его полю what создаваемых сообщений загрузки
    public static final int MESSAGE_DOWNLOAD = 0;
    //В переменной mRequestHandler будет храниться ссылка на объект Handler,
    // отвечающий за постановку в очередь запросов на загрузку в фоновом потоке
    // ThumbnailDownloader.
    private Handler mRequestHandler;
    //позволяет хранить и загрузить URL-адрес, связанный с конкретным запросом.
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    public static final String TAG ="ThumbnailDownloader";

    // Использование слушателя передает ответственность
    // за обработку загруженного изображения другому классу
    // (в данном случае PhotoGalleryFragment).
    public interface ThumbnailDownloadListener<T>{
        //Метод будет вызван после полной загрузки фотографии.
        void onTumbnailDownoaded(T target, Bitmap thumbnail);
    }

    public void setThubnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super( TAG);
        mResponseHandler = responseHandler;
    }
    //Метод Handler.handleMessage(…) реализуется в субклассе Handler внутри onLooperPrepared().
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD){
                    T target =(T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest( target );
                }
            }
        };
    }

    //Метод queueThumbnail() ожидает получить объект типа T,
    // выполняющий функции идентификатора загрузки, и String с URL-адресом для загрузки.
    // Этот метод будет вызываться GalleryItemAdapter в его реализации onBindViewHolder(…).
    public void queueThumbnail(T target, String url){
        Log.i( TAG, "God a URL: " +url );

        if (url == null){
            mRequestMap.remove( target);
        }else {
            mRequestMap.put( target, url );
            //mRequestHandler будет отвечать за обработку
            // сообщения при его извлечении из очереди сообщений
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
    }

    //Метод очистки очереди.
    public void clearQueue(){
        mRequestHandler.removeMessages( MESSAGE_DOWNLOAD );
    }
    //Вся загрузка осуществляется в методе handleRequest()
    // Мы проверяем существование URL-адреса,
    // после чего передаем его новому экземпляру класса FlickrFetchr

    private void handleRequest(final T target){
        try {
            final String url = mRequestMap.get( target );
            if (url == null){
                return;
            }

            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes( url );
            //мы используем класс BitmapFactory для построения растрового изображения
            // с массивом байтов, возвращенным getUrlBytes(…).
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0 , bitmapBytes.length );
            Log.i( TAG, "Bitmap created " );

            //Загрузка и вывод изображения.А поскольку mResponseHandler связывается с Looper главного потока,
            // этот код обновления пользовательского интерфейса будет выполнен в главном потоке.
            // Что делает этот код? Сначала он проверяет requestMap. Такая проверка необходима,
            // потому что RecyclerView заново использует свои представления.
            // К тому времени, когда ThumbnailDownloader завершит загрузку Bitmap, может оказаться, что
            //виджет RecyclerView уже переработал ImageView и запросил для него изображение с другого URL-адреса.
            // Эта проверка гарантирует, что каждый объект PhotoHolder получит правильное изображение,
            // даже если за прошедшее время был сделан другой запрос
            mResponseHandler.post( new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get( target ) != url){
                        return;
                    }

                    mRequestMap.remove( target );
                    mThumbnailDownloadListener.onTumbnailDownoaded( target, bitmap );
                }
            } );
        }catch (IOException ioe){
            Log.e( TAG, "Error download image", ioe);
        }
    }
}
