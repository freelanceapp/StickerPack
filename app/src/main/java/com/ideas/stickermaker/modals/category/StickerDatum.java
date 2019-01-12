
package com.ideas.stickermaker.modals.category;

import java.util.ArrayList;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StickerDatum implements Parcelable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("category_name")
    @Expose
    private String categoryName;
    @SerializedName("category_image")
    @Expose
    private String categoryImage;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("modify_date")
    @Expose
    private String modifyDate;
    @SerializedName("subcategory")
    @Expose
    private List<StickerSubcategory> subcategory = new ArrayList<StickerSubcategory>();
    public final static Creator<StickerDatum> CREATOR = new Creator<StickerDatum>() {


        @SuppressWarnings({
            "unchecked"
        })
        public StickerDatum createFromParcel(Parcel in) {
            return new StickerDatum(in);
        }

        public StickerDatum[] newArray(int size) {
            return (new StickerDatum[size]);
        }

    }
    ;

    protected StickerDatum(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.categoryName = ((String) in.readValue((String.class.getClassLoader())));
        this.categoryImage = ((String) in.readValue((String.class.getClassLoader())));
        this.status = ((String) in.readValue((String.class.getClassLoader())));
        this.createdDate = ((String) in.readValue((String.class.getClassLoader())));
        this.modifyDate = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.subcategory, (StickerSubcategory.class.getClassLoader()));
    }

    public StickerDatum() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
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

    public List<StickerSubcategory> getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(List<StickerSubcategory> subcategory) {
        this.subcategory = subcategory;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(categoryName);
        dest.writeValue(categoryImage);
        dest.writeValue(status);
        dest.writeValue(createdDate);
        dest.writeValue(modifyDate);
        dest.writeList(subcategory);
    }

    public int describeContents() {
        return  0;
    }

}
