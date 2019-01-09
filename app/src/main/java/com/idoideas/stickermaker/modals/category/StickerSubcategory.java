
package com.idoideas.stickermaker.modals.category;

import java.util.ArrayList;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StickerSubcategory implements Parcelable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("main_cat_id")
    @Expose
    private String mainCatId;
    @SerializedName("subcat_name")
    @Expose
    private String subcatName;
    @SerializedName("count")
    @Expose
    private Object count;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("modify_date")
    @Expose
    private Object modifyDate;
    @SerializedName("sticker")
    @Expose
    private List<StickerList> sticker = new ArrayList<StickerList>();
    public final static Creator<StickerSubcategory> CREATOR = new Creator<StickerSubcategory>() {


        @SuppressWarnings({
            "unchecked"
        })
        public StickerSubcategory createFromParcel(Parcel in) {
            return new StickerSubcategory(in);
        }

        public StickerSubcategory[] newArray(int size) {
            return (new StickerSubcategory[size]);
        }

    }
    ;

    protected StickerSubcategory(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.mainCatId = ((String) in.readValue((String.class.getClassLoader())));
        this.subcatName = ((String) in.readValue((String.class.getClassLoader())));
        this.count = ((Object) in.readValue((Object.class.getClassLoader())));
        this.status = ((String) in.readValue((String.class.getClassLoader())));
        this.createdDate = ((String) in.readValue((String.class.getClassLoader())));
        this.modifyDate = ((Object) in.readValue((Object.class.getClassLoader())));
        in.readList(this.sticker, (StickerList.class.getClassLoader()));
    }

    public StickerSubcategory() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMainCatId() {
        return mainCatId;
    }

    public void setMainCatId(String mainCatId) {
        this.mainCatId = mainCatId;
    }

    public String getSubcatName() {
        return subcatName;
    }

    public void setSubcatName(String subcatName) {
        this.subcatName = subcatName;
    }

    public Object getCount() {
        return count;
    }

    public void setCount(Object count) {
        this.count = count;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public Object getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Object modifyDate) {
        this.modifyDate = modifyDate;
    }

    public List<StickerList> getSticker() {
        return sticker;
    }

    public void setSticker(List<StickerList> sticker) {
        this.sticker = sticker;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(mainCatId);
        dest.writeValue(subcatName);
        dest.writeValue(count);
        dest.writeValue(status);
        dest.writeValue(createdDate);
        dest.writeValue(modifyDate);
        dest.writeList(sticker);
    }

    public int describeContents() {
        return  0;
    }

}
