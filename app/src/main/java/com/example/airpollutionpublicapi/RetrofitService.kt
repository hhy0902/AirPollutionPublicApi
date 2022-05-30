package com.example.airpollutionpublicapi

import com.example.airpollutionpublicapi.AirPollutionData.Pollution
import com.example.airpollutionpublicapi.stationData.Station
import com.example.airpollutionpublicapi.tmData.TmCordinatesResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface RetrofitService {

    @Headers("Authorization: KakaoAK ${BuildConfig.KAKAO_API_KEY}")
    @GET("v2/local/geo/transcoord.json?output_coord=TM")
    fun getTmCoordinates(
        @Query("x") longitude : Double,
        @Query("y") latitude : Double
    ) : Call<TmCordinatesResponse>

    @GET("B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList?serviceKey=JCrJa4%2F4eF07FKbnkSi7BDDUvnJXCE1CTiyt%2FfnxJ%2B7jewHaXTp5hrKQzOKdWYctQB%2B3a%2FHLuUHkTPq4hqrxvA%3D%3D&returnType=json")
    fun getStation(
        @Query("tmX") tmX : Double,
        @Query("tmY") tmY : Double
    ) : Call<Station>

    @GET("B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=JCrJa4%2F4eF07FKbnkSi7BDDUvnJXCE1CTiyt%2FfnxJ%2B7jewHaXTp5hrKQzOKdWYctQB%2B3a%2FHLuUHkTPq4hqrxvA%3D%3D&returnType=json&numOfRows=100&pageNo=1&dataTerm=DAILY&ver=1.3")
    fun getPollution(
        @Query("stationName") stationName : String
    ) : Call<Pollution>

}