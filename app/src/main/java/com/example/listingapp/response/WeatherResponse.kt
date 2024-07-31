package com.example.listingapp.response

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("count")
    val count: Int?,
    @SerializedName("data")
    val data: List<WeatherData>?
)

data class WeatherData(
    @SerializedName("city_name")
    val cityName: String?,
    @SerializedName("clouds")
    val clouds: Int?,
    @SerializedName("weather")
    val weather: Weather?,
    @SerializedName("app_temp")
    val appTemp: Float?,
    @SerializedName("aqi")
    val aqi: Int?,
    @SerializedName("country_code")
    val countryCode: String?,
    @SerializedName("datetime")
    val datetime: String?,
    @SerializedName("dewpt")
    val dewpt: Double? = 0.0,
    @SerializedName("dhi")
    val dhi: Double? = 0.0,
    @SerializedName("dni")
    val dni: Double? = 0.0,
    @SerializedName("elev_angle")
    val elevAngle: Double? = 0.0,
    @SerializedName("ghi")
    val ghi: Double? = 0.0,
    @SerializedName("h_angle")
    val hAngle: Int?,
    @SerializedName("lat")
    val lat: Double? = 0.0,
    @SerializedName("lon")
    val lon: Double? = 0.0,
    @SerializedName("ob_time")
    val obTime: String?,
    @SerializedName("pod")
    val pod: String?,
    @SerializedName("precip")
    val precip: Int?,
    @SerializedName("pres")
    val pres: Double? = 0.0,
    @SerializedName("rh")
    val rh: Double? = 0.0,
    @SerializedName("slp")
    val slp: Double? = 0.0,
    @SerializedName("snow")
    val snow: Int?,
    @SerializedName("solar_rad")
    val solarRad: Double? = 0.0,
    @SerializedName("state_code")
    val stateCode: String?,
    @SerializedName("station")
    val station: String?,
    @SerializedName("sunrise")
    val sunrise: String?,
    @SerializedName("sunset")
    val sunset: String?,
    @SerializedName("temp")
    val temp: Double? = 0.0,
    @SerializedName("timezone")
    val timezone: String?,
    @SerializedName("ts")
    val ts: Int?,
    @SerializedName("uv")
    val uv: Double?,
    @SerializedName("vis")
    val vis: Int?,
    @SerializedName("wind_cdir")
    val windCdir: String?,
    @SerializedName("wind_cdir_full")
    val windCdirFull: String?,
    @SerializedName("wind_dir")
    val windDir: Int?,
    @SerializedName("wind_spd")
    val windSpd: Double? = 0.0
)

data class Minutely(
    @SerializedName("precip")
    val precip: Int?,
    @SerializedName("snow")
    val snow: Int?,
    @SerializedName("temp")
    val temp: Int?,
    @SerializedName("timestamp_local")
    val timestampLocal: String?,
    @SerializedName("timestamp_utc")
    val timestampUtc: String?,
    @SerializedName("ts")
    val ts: Int?
)

data class Weather(
    @SerializedName("code")
    val code: Int?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("icon")
    val icon: String?
)