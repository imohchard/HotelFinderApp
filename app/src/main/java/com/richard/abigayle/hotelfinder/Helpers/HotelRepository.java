package com.richard.abigayle.hotelfinder.Helpers;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.richard.abigayle.hotelfinder.POJO.DetailedResponse;
import com.richard.abigayle.hotelfinder.POJO.DistanceBetween;
import com.richard.abigayle.hotelfinder.POJO.MainResponse;
import com.richard.abigayle.hotelfinder.POJO.Result;
import com.richard.abigayle.hotelfinder.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by LENOVO on 3/26/2018.
 */

public class HotelRepository {
    GeoDataClient mGoogleApiClient;


    private LiveData<List<Hotels>> mHotels;
    private LiveData<Hotels>hotel;


    private  HotelDao mHotelDao;
    private Context context;
    String root = Environment.getExternalStorageDirectory().toString();
    File myDir = new File(root + "/hotel_images");
    int radius_count = 0;






    public HotelRepository(Application application) {
        HotelDatabase hotelDb = HotelDatabase.getInstance(application);

        mHotelDao = hotelDb.hotelDao();
        mHotels = mHotelDao.getAll();

        context = application;
    }
    public LiveData<List<Hotels>>getAllHotels(){
        return mHotels;
    }
    public LiveData<Hotels>getSingleHotel(int viewposition){
        hotel = mHotelDao.singleHotel(viewposition);
        return hotel;
    }







    //TODO Create method that scans with nextpage tokener
    public void scanFetch(String location) {


        String key = "AIzaSyBWKQHS39-SYUNxEEAry1FxrMET2NwhqxE";
        Map<String, String> param = new HashMap<>();
        param.put("location", location);
        param.put("key", key);
        param.put("type", "school");
        if(radius_count == 0){
            param.put("radius", "1000");
        }
        else {
            param.put("radius", "120000");
        }



        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        HotelNetworkClient networkClient = retrofit.create(HotelNetworkClient.class);
        Call<MainResponse> call = networkClient.hotelList(param);


        call.enqueue(new Callback<MainResponse>() {
            @Override
            public void onResponse(Call<MainResponse> call, Response<MainResponse> response) {
                Log.d("response",response.body().getStatus());
                if(response.body().getResult().isEmpty()){
                    radius_count = 99;
                    scanFetch(location);
                }
                else {
                    String token = response.body().getNext_page_token();

                    for (int i = 0; i < response.body().getResult().size(); i++) {
                        String place_id = response.body().getResult().get(i).getPlaceId();
                        String name = response.body().getResult().get(i).getName();
                        String vicinity = response.body().getResult().get(i).getVicinity();


                        Log.d("school ", name + "address " + vicinity);

                        if(!myDir.exists()){
                            myDir.mkdirs();

                        }
                        if(i==19){
                            Log.d("nine ", place_id + "is " + i);
                            nextPage(token);
                        }

                        getHotelDetails(place_id, i);
                    }
                }
                }


            @Override
            public void onFailure(Call<MainResponse> call, Throwable t) {

            }
        });
    }

    public void nextPage(String tokener){
        String key = "AIzaSyBWKQHS39-SYUNxEEAry1FxrMET2NwhqxE";
        Map<String,String>param = new HashMap<>();
        param.put("key",key);
        param.put("pagetoken",tokener);
        Log.d("nextPage","Enetered next page");
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory( GsonConverterFactory.create());
        Retrofit retrofit = retrofitBuilder.build();
        Log.d("nextPage","Built Url");

        HotelNetworkClient networkClient = retrofit.create(HotelNetworkClient.class);

        Call<MainResponse> call = networkClient.nextPage(param);
        Log.d("nextPage","Finished calling client");
        call.enqueue(new Callback<MainResponse>() {
            @Override
            public void onResponse(Call<MainResponse> call, Response<MainResponse> response) {
                Log.d("nextPage","Got response");
                if(response.body().getResult().isEmpty()){
                    return;
                }
                else {
                    for (int i = 0; i < response.body().getResult().size(); i++) {
                        String place_id = response.body().getResult().get(i).getPlaceId();
                        String name = response.body().getResult().get(i).getName();
                        String vicinity = response.body().getResult().get(i).getVicinity();

                        Log.d("school ", name + "address " + vicinity);

                        getHotelDetails(place_id, i+20);
                        //nextPage(response.body().getNext_page_token());


                    }
                }

            }

            @Override
            public void onFailure(Call<MainResponse> call, Throwable t) {

            }
        });


    }


    private void getHotelDetails(String id, int i) {
        //TODO Scrap this details task use retrofit to get the hotel details.
        Log.d("int", "here    " +i);




        mGoogleApiClient = Places.getGeoDataClient(context, null);

        mGoogleApiClient.getPlaceById(id).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                if (task.isSuccessful()) {
                    PlaceBufferResponse bufferResponse = task.getResult();
                    Place place = bufferResponse.get(0);


                    String place_id = place.getId();

                    String place_name = place.getName().toString();
                    String telephone = place.getPhoneNumber().toString();
                    String address = place.getAddress().toString();
                    String website;
                    if (place.getWebsiteUri() != null) {
                        website = place.getWebsiteUri().toString();
                    } else {
                        website = "www.bookings.com/places";
                    }


                    String latlong = place.getLatLng().toString();
                    Log.d("latlng",latlong);
                    int pricelevel = place.getPriceLevel();
                    float rating = place.getRating();
                    bufferResponse.release();


//                    getPhoto(place_id,i);
                    getPhoto(place_id);

                    Hotels hotels = new Hotels(i,place_id,place_name,telephone,address,website,pricelevel,rating,latlong,"","","",null,null);

                    insertToDb(hotels);




                } else {
                    return;

                }
            }
        });

    }


    public void getPhoto(String place_id,int id ) {
        mGoogleApiClient = Places.getGeoDataClient(context, null);
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGoogleApiClient.getPlacePhotos(place_id);
        getDistance("place_id:ChIJQbZ_dzGSOxARugpHaf3JWN0",place_id,place_id);


        photoMetadataResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                PlacePhotoMetadataResponse metadataResponse = task.getResult();
                PlacePhotoMetadataBuffer photoMetadata = metadataResponse.getPhotoMetadata();

                //Map<String,String> imagestring = new HashMap<>();
                List<String> imagestring = new ArrayList<>();



                        for (int i = 0; i < 3; i++) {

                            final int a = i;
                            final int b = photoMetadata.getCount();
                            if(b>a){
                                PlacePhotoMetadata placePhotoMetadata = photoMetadata.get(i);
                                Task<PlacePhotoResponse> responseTask = mGoogleApiClient.getPhoto(placePhotoMetadata);
                                responseTask.addOnCompleteListener(task1 -> {
                                    PlacePhotoResponse photo = task1.getResult();
                                   // Bitmap raw = photo.getBitmap();
                                    Bitmap image1 = (b<a)? BitmapFactory.decodeResource(context.getResources(), R.drawable.markar):photo.getBitmap();

                                    imagestring.add(saveToInternalStorage(image1, place_id, a));

                                });
                            }
                            else {
                                Bitmap image1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.markar);
                                imagestring.add(saveToInternalStorage(image1,place_id,a));
                            }

                        }
                photoMetadata.release();
                    }
        });


    }

    private String saveToInternalStorage(Bitmap bitmap, String place_id, int a) {
        ContextWrapper cw = new ContextWrapper(context);
        String fname = place_id+a + ".jpg";
        File mypath = cw.getDir("hotel_images",Context.MODE_PRIVATE);

        File imagepath = new File(mypath, "place_id"+fname );


        new AsyncTask<String,Void,String>(){
            @Override
            protected String doInBackground(String... voids) {
                try {
                    FileOutputStream fos = new FileOutputStream(imagepath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                insertImageToDb(place_id,imagepath.getAbsolutePath(),a);
                return null;


            }
        }.execute();

        return imagepath.getAbsolutePath();


        }

    private void getDistance(String origins,String destinations,String place_id){
        String key = "AIzaSyAvXgkHdw8StiKTkbiI2sMip1D_Ru37ZTE";
        Map<String, String> param = new HashMap<>();
        param.put("origins", origins);
        param.put("destinations", "place_id:" + destinations);
        param.put("key", key);


        Retrofit.Builder retrofitBuildr = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = retrofitBuildr.build();

        HotelNetworkClient networkClient = retrofit.create(HotelNetworkClient.class);
        Call<DistanceBetween> call = networkClient.distance(param);
        call.enqueue(new Callback<DistanceBetween>() {
            @Override
            public void onResponse(Call<DistanceBetween> call, Response<DistanceBetween> response) {
                String distance="Some km",duration = "Some km";
                if(response.body().getRows().get(0).getElement().get(0).getStatus().equals("OK")){
                    distance = response.body().getRows().get(0).getElement().get(0).getDistance().getText();
                    duration = response.body().getRows().get(0).getElement().get(0).getDuration().getText();
                }



                updateDistance(place_id,distance,duration);

            }

            @Override
            public void onFailure(Call<DistanceBetween> call, Throwable t) {

            }
        });

    }

    private void insertToDb(Hotels hotels){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                mHotelDao.insertAll(hotels);
                return null;
            }
        }.execute();
    }



    private void insertImageToDb(String place_id, String image,int id){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                switch (id){
                    case 0:
                        mHotelDao.insertImage1(place_id,image);
                    case 1:
                        mHotelDao.insertImage2(place_id,image);
                    case 2:
                        mHotelDao.insertImage3(place_id,image);


                }

                return null;
            }
        }.execute();
    }
    void updateDistance(String place_id,String distance,String duration){
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                mHotelDao.insertDistance(place_id,distance,duration);

                return null;
            }
        }.execute();
    }




    void getPhoto(String  place_id){
            String photo_url = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=";
            String key = "AIzaSyBWKQHS39-SYUNxEEAry1FxrMET2NwhqxE";
            Map<String, String> param = new HashMap<>();
            param.put("place_id",place_id);
            param.put("key", key);
        getDistance("place_id:ChIJQbZ_dzGSOxARugpHaf3JWN0",place_id,place_id);
        Log.d("count",place_id);



        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory( GsonConverterFactory.create());
        Retrofit retrofit = retrofitBuilder.build();


        HotelNetworkClient networkClient = retrofit.create(HotelNetworkClient.class);

        Call<DetailedResponse> call = networkClient.details(param);
        call.enqueue(new Callback<DetailedResponse>(){
            @Override
            public void onResponse(Call<DetailedResponse> call, Response<DetailedResponse> response) {
                int count = response.body().getResults().getPhotos().size();
                String name = response.body().getResults().getName();
                for (int i = 0; i<3;i++){
                    int a = i;

                    String photo_ref = (count > i)? response.body().getResults().getPhotos().get(i).getPhotoReference():null;


                    Glide.with(context)
                            .load(photo_url + photo_ref + "&key=" + key)
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>(300,300) {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation)  {
                            saveToInternalStorage(resource,place_id,a);
                        }
                    });

                }
            }

            @Override
            public void onFailure(Call<DetailedResponse> call, Throwable t) {

            }
        });


    }

    void getDetail(String place_id, int i){
        Map<String,String>param = new HashMap<>();
        String key = "AIzaSyBWKQHS39-SYUNxEEAry1FxrMET2NwhqxE";
        param.put("place_id",place_id);
        param.put("key",key);

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        HotelNetworkClient networkClient = retrofit.create(HotelNetworkClient.class);
        Call<DetailedResponse> call = networkClient.details(param);
        call.enqueue(new Callback<DetailedResponse>() {
            @Override
            public void onResponse(Call<DetailedResponse> call, Response<DetailedResponse> response) {
                String website;

               String place_name = response.body().getResults().getName();
                String telephone = response.body().getResults().getTelephone();
                String address = response.body().getResults().getVicinity();
                int pricelevel = response.body().getResults().getPriceLevel();
                float rating = response.body().getResults().getRating();
                Double lat = response.body().getResults().getGeometry().getLocation().getLat();
                Double lng = response.body().getResults().getGeometry().getLocation().getLng();
                String latlong = lat+","+lng;




                if (response.body().getResults().getWebsite() != null) {
                    website = response.body().getResults().getWebsite();
                } else {
                    website = response.body().getResults().getUrl();
                }

                getPhoto(place_id);

                Hotels hotels = new Hotels(i,place_id,place_name,telephone,address,website,pricelevel,rating,latlong,null,null,null,null,null);

                insertToDb(hotels);

            }

            @Override
            public void onFailure(Call<DetailedResponse> call, Throwable t) {

            }
        });



    }




}



















