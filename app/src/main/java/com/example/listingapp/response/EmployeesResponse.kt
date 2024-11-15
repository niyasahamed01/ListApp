package com.example.listingapp.response

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

data class EmployeesResponse(
    @SerializedName("info")
    val info: Info?,
    @SerializedName("total")
    val total: Int?,
    @SerializedName("results")
    val results: List<ModelResult>
)

data class Info(
    @SerializedName("page")
    val page: Int?,
    @SerializedName("results")
    val results: Int?,
    @SerializedName("seed")
    val seed: String?,
    @SerializedName("version")
    val version: String?
)

@Parcelize
@Entity
data class ModelResult(
    @PrimaryKey(autoGenerate = false)
    @SerializedName("id")
    val id: Id,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("picture")
    val picture: Picture?,
    @SerializedName("name")
    val name: Name?,
    @SerializedName("nat")
    val nat: String?,
    @SerializedName("phone")
    val phone: String?,
    @SerializedName("dob")
    val dob: Dob?,
    @SerializedName("cell")
    val cell: String?,
    @SerializedName("email")
    val email: String?
) : Parcelable

@Parcelize
data class Dob(
    @SerializedName("age")
    val age: Int?,
    @SerializedName("date")
    val date: String?
) : Parcelable

@Parcelize
data class Id(
    @SerializedName("name")
    val name: String?,
    @SerializedName("value")
    var value: @RawValue Any? = null
) : Parcelable

@Parcelize
@Entity(tableName = "name")
data class Name(
    @PrimaryKey
    @SerializedName("first")
    val first: String,
    @SerializedName("last")
    val last: String?,
    @SerializedName("title")
    val title: String?
) : Parcelable

@Parcelize
data class Picture(
    @SerializedName("large")
    val large: String?,
    @SerializedName("medium")
    val medium: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?
) : Parcelable
