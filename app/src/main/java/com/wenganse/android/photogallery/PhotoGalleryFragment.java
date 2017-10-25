package com.wenganse.android.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;



import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Plus on 14.05.2017.
 */

public class PhotoGalleryFragment extends VisibleFragment {

    public static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    //В качестве идентификатора используется PhotoHolder,
    // за одно он определяет место,
    // куда в конечном итоге поступят изображения.
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }

    //Заготовка кода
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        //регистрация фрагментов для получения обратных вызовов.
        setHasOptionsMenu( true );
        //execute для нового экземпляра FetchItemTask.
        updateItem();

        //Подключение к обработчику ответа.
        Handler responseHandler = new Handler();
        //Создание потока.
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        // Назначение  ThumbnailDownloadListener
        // для обработки загруженного изображения после завершения загрузки.
        mThumbnailDownloader.setThubnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>(){
                    @Override
                    public void onTumbnailDownoaded(PhotoHolder photoHolder, Bitmap bitmap) {
                        Drawable drawable = new BitmapDrawable( getResources(), bitmap);
                        photoHolder.bindDrawable( drawable );
                    }
                } );

        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i( TAG, "Background thread started");
    }

    //Заготовка кода
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate( R.layout.fragment_photo_gallery, container,false );
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager( new GridLayoutManager(getActivity(),3));
        setupAdapter();
        return v;
    }

    //очистка загрузчика при уничтожении View.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    //Определение метода Дестрой для завершение потока ThumbnailDownload.
    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed" );
    }
    //Переопределение меню, для добавления перечисленных элементов в разметке, в панель инструментов.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu( menu, menuInflater );
        menuInflater.inflate( R.menu.fragment_photo_gallery,menu );

        // мы получаем объект MenuItem, представляющий поле поиска, и сохраняем его в searchItem.
        MenuItem searchItem = menu.findItem( R.id.menu_item_search );
        //извлекается объект SearchView методом getActionView().
        final SearchView searchView = (SearchView)searchItem.getActionView();
        //интерфейс SearchView.OnQueryTextListener предоставляет возможность получения обратных вызовов при отправке запроса.
        searchView.setOnQueryTextListener( new SearchView.OnQueryTextListener() {
            @Override
            //выполняется при отправке запроса пользователем
            public boolean onQueryTextSubmit(String s) {
                Log.d( TAG, "QueryTextSubmit" + s );
                //Каждый раз, когда пользователь выбирает элемент Clear Search в дополнительном меню,
                // стирайте сохраненный запрос (присваиванием ему null).
                QueryPreferences.setStoredQuery( getActivity(), s);
                updateItem();
                return true;
            }

            @Override
            //выполняется при каждом изменении текста в текстовом поле SearchView
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        } );

        // когда пользователь нажимает кнопку поиска для открытия SearchView.
        // Метод View.OnClickListener.onClick() SearchView вызывается при нажатии этой кнопки.
        searchView.setOnSearchClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery( getActivity() );
                searchView.setQuery( query, false );
            }
        });

        MenuItem toggleItems = menu.findItem( R.id.menu_item_toggle_polling );
        if (PollService.isServiceAlarm(getActivity())){
            toggleItems.setTitle(R.string.stop_polling);
        }else {
            toggleItems.setTitle( R.string.start_polling );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery( getActivity(), null );
                //гарантирует, что изображения, отображаемые в RecyclerView,
                // соответствуют самому последнему поисковому запросу.
                updateItem();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarm( getActivity() );
                PollService.setServiceAlarm( getActivity(), shouldStartAlarm );
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected( item );
        }

    }

    private void updateItem(){
        String query = QueryPreferences.getStoredQuery( getActivity() );
        new FetchItemTack(query).execute();
    }
    //метод setupAdapter() проверяет текущее состояние модели
    private void setupAdapter(){
        //Проверка подтверждает, что фрагмент был присоединен к активности,
        // а следовательно, что результат getActivity() будет отличен от null.
        if (isAdded()){
            mPhotoRecyclerView.setAdapter( new PhotoAdapter( mItems ) );
        }
    }

    //Реализация ViewHolder.
    private class PhotoHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super( itemView );
            mItemImageView = (ImageView)itemView.findViewById( R.id.fragment_photo_galley_image_view );
            itemView.setOnClickListener( this );
        }
            //Метод назначающий объект Drawable виджиту ImageView.
        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable( drawable );
        }

        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            //неявный интент для запуска страницы в браузере.
            Intent i = PhotoPageActivity.newIntent( getActivity(), mGalleryItem.getPhotoPageUri() );
            startActivity( i );
        }
    }

    //Класс адаптер необходимый для предстваления объектов
    //на основании списка GalleryItem.
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        private List<GalleryItem> mGalleryItems;
        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from( getActivity() );
            View view = inflater.inflate( R.layout.gallery_item, viewGroup, false );

            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get( position );
            photoHolder.bindGalleryItem( galleryItem );
            //назначение временного изображения.
            Drawable placeholder = getResources().getDrawable( R.drawable.bill_up_close );
            photoHolder.bindDrawable( placeholder );
            //Передача данных в ДовнЛоадер Изображения и URL.
            mThumbnailDownloader.queueThumbnail( photoHolder, galleryItem.getUri());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    //Реалезация AsyncTask
    private class FetchItemTack extends AsyncTask<Void, Void, List<GalleryItem>>{

        private String mQuery;

        public FetchItemTack(String query) {
            mQuery = query;
        }
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            //Код с сохраненным запросом поиска.

            if (mQuery == null){
                //загружает результат поиска
                return new FlickrFetchr().fetchRecentPhotos();
            }else {
                //загружает последние фотографии.
                return new FlickrFetchr().searchPhoto( mQuery );
            }
        }

        //onPostExecute(…). Этот метод получает список,
        // загруженный в doInBackground(…),
        // помещает его в mItems и обновляет адаптер RecyclerView
        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }
}
