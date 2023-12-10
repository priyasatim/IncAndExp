package com.example.incndex.data

import android.os.Parcel
import android.os.Parcelable

data class RestoreData(
    var id : Int = 0,
    var ref_id : Int,
    var name : String,
    var category : String,
    var price : Double,
    var date : Long,
    var payment_mode : String,
    var isIncome : Boolean) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()?:"",
        parcel.readString()?:"",
        parcel.readDouble(),
        parcel.readLong(),
        parcel.readString()?:"",
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(ref_id)
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeDouble(price)
        parcel.writeLong(date)
        parcel.writeString(payment_mode)
        parcel.writeByte(if (isIncome) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RestoreData> {
        override fun createFromParcel(parcel: Parcel): RestoreData {
            return RestoreData(parcel)
        }

        override fun newArray(size: Int): Array<RestoreData?> {
            return arrayOfNulls(size)
        }
    }
}
