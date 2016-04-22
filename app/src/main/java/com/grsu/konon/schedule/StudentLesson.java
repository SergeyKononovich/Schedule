package com.grsu.konon.schedule;

import android.os.Parcel;
import android.os.Parcelable;

public class StudentLesson implements Parcelable {
    public String timeStart;
    public String timeEnd;
    public String type;
    public String title;
    public String address;
    public String room;
    public String subgroup;
    public String teacherFullName;


    public StudentLesson() {

    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(timeStart);
        parcel.writeString(timeEnd);
        parcel.writeString(type);
        parcel.writeString(title);
        parcel.writeString(address);
        parcel.writeString(room);
        parcel.writeString(subgroup);
        parcel.writeString(teacherFullName);
    }

    public static final Parcelable.Creator<StudentLesson> CREATOR
            = new Parcelable.Creator<StudentLesson>() {
        public StudentLesson createFromParcel(Parcel in) {
            return new StudentLesson(in);
        }

        public StudentLesson[] newArray(int size) {
            return new StudentLesson[size];
        }
    };

    private StudentLesson(Parcel parcel) {
        timeStart = parcel.readString();
        timeEnd = parcel.readString();
        type = parcel.readString();
        title = parcel.readString();
        address = parcel.readString();
        room = parcel.readString();
        subgroup = parcel.readString();
        teacherFullName = parcel.readString();
    }
}
