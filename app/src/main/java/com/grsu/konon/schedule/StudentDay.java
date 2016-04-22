package com.grsu.konon.schedule;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentDay implements Parcelable {
    public Date date = new Date();
    public List<StudentLesson> lessons = new ArrayList<>();


    public StudentDay() {

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(date.getTime());
        parcel.writeList(lessons);
    }

    public static final Parcelable.Creator<StudentDay> CREATOR
            = new Parcelable.Creator<StudentDay>() {
        public StudentDay createFromParcel(Parcel in) {
            return new StudentDay(in);
        }

        public StudentDay[] newArray(int size) {
            return new StudentDay[size];
        }
    };

    private StudentDay(Parcel parcel) {
        date = new Date(parcel.readLong());
        lessons = parcel.readArrayList(null);
    }
}
