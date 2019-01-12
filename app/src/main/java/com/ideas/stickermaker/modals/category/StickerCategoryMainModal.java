
package com.ideas.stickermaker.modals.category;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StickerCategoryMainModal implements Parcelable {

    @SerializedName("data")
    @Expose
    private List<StickerDatum> data = new ArrayList<StickerDatum>();
    public final static Creator<StickerCategoryMainModal> CREATOR = new Creator<StickerCategoryMainModal>() {


        @SuppressWarnings({
                "unchecked"
        })
        public StickerCategoryMainModal createFromParcel(Parcel in) {
            return new StickerCategoryMainModal(in);
        }

        public StickerCategoryMainModal[] newArray(int size) {
            return (new StickerCategoryMainModal[size]);
        }

    };

    protected StickerCategoryMainModal(Parcel in) {
        in.readList(this.data, (StickerDatum.class.getClassLoader()));
    }

    public StickerCategoryMainModal() {
    }

    public List<StickerDatum> getData() {
        return data;
    }

    public void setData(List<StickerDatum> data) {
        this.data = data;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(data);
    }

    public int describeContents() {
        return 0;
    }

}
