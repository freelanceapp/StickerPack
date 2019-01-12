
package com.ideas.stickermaker.modals.category;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StickerList implements Parcelable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("main_cat_id")
    @Expose
    private String mainCatId;
    @SerializedName("sub_cat_id")
    @Expose
    private String subCatId;
    @SerializedName("stickers")
    @Expose
    private String stickers;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("modify_date")
    @Expose
    private String modifyDate;
    public final static Creator<StickerList> CREATOR = new Creator<StickerList>() {


        @SuppressWarnings({
            "unchecked"
        })
        public StickerList createFromParcel(Parcel in) {
            return new StickerList(in);
        }

        public StickerList[] newArray(int size) {
            return (new StickerList[size]);
        }

    }
    ;

    protected StickerList(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.mainCatId = ((String) in.readValue((String.class.getClassLoader())));
        this.subCatId = ((String) in.readValue((String.class.getClassLoader())));
        this.stickers = ((String) in.readValue((String.class.getClassLoader())));
        this.status = ((String) in.readValue((String.class.getClassLoader())));
        this.createdDate = ((String) in.readValue((String.class.getClassLoader())));
        this.modifyDate = ((String) in.readValue((String.class.getClassLoader())));
    }

    public StickerList() {
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

    public String getSubCatId() {
        return subCatId;
    }

    public void setSubCatId(String subCatId) {
        this.subCatId = subCatId;
    }

    public String getStickers() {
        return stickers;
    }

    public void setStickers(String stickers) {
        this.stickers = stickers;
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

    public String getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(mainCatId);
        dest.writeValue(subCatId);
        dest.writeValue(stickers);
        dest.writeValue(status);
        dest.writeValue(createdDate);
        dest.writeValue(modifyDate);
    }

    public int describeContents() {
        return  0;
    }

}
