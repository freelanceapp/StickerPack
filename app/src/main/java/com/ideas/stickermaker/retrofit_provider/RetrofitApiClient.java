package com.ideas.stickermaker.retrofit_provider;

import com.ideas.stickermaker.constant.Constant;
import com.ideas.stickermaker.modals.category.StickerCategoryMainModal;
import com.ideas.stickermaker.modals.category.StickerSubcategory;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface RetrofitApiClient {

    @Multipart
    @POST(Constant.CONTENT_API)
    Call<ResponseBody> userRegistration(@Part("name") RequestBody name, @Part MultipartBody.Part file,
                                        @Part("email") RequestBody email, @Part("password") RequestBody password,
                                        @Part("mobile_number") RequestBody mobile_number);

    @FormUrlEncoded
    @POST(Constant.CONTENT_API)
    Call<ResponseBody> contentData(@Field("contentid") String contentid);

    @FormUrlEncoded
    @POST(Constant.LIKES_API)
    Call<ResponseBody> sendLike(@Field("mid") String mid, @Field("sid") String sid);

    @FormUrlEncoded
    @POST(Constant.DOWNLOAD_API)
    Call<ResponseBody> downloadCount(@Field("mid") String mid, @Field("sid") String sid);

    @FormUrlEncoded
    @POST(Constant.STICKER_PACK_API)
    Call<StickerSubcategory> stickerPackData(@Field("sid") String sid);

    @GET(Constant.CATEGORY_API)
    Call<StickerCategoryMainModal> categoryList();

    @GET
    Call<ResponseBody> getImageDetails(@Url String fileUrl);
}