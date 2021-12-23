package com.example.listingapp.paging

import androidx.room.TypeConverter
import com.example.listingapp.response.Dob
import com.example.listingapp.response.Id
import com.example.listingapp.response.Name
import com.example.listingapp.response.Picture
import com.google.gson.Gson


class RoomTypeConvertor {

    @TypeConverter
    fun nameToString(name: Name): String {
        return Gson().toJson(name)
    }

    @TypeConverter
    fun stringToName(str: String): Name {
        return Gson().fromJson(str, Name::class.java)
    }

    @TypeConverter
    fun idToString(id: Id): String {
        return Gson().toJson(id)
    }

    @TypeConverter
    fun stringToId(str: String): Id {
        return Gson().fromJson(str, Id::class.java)
    }

    @TypeConverter
    fun pictureToString(picture: Picture): String {
        return Gson().toJson(picture)
    }

    @TypeConverter
    fun stringToPicture(str: String): Picture {
        return Gson().fromJson(str, Picture::class.java)
    }

    @TypeConverter
    fun dobToString(dob: Dob): String {
        return Gson().toJson(dob)
    }

    @TypeConverter
    fun stringToDob(str: String): Dob {
        return Gson().fromJson(str, Dob::class.java)
    }
}