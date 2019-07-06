package com.buaa.simplemov.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.18 17:53
 */
public class PostVideoResponse {

    // TODO-C2 (3) Implement your PostVideoResponse Bean here according to the response json
    // response
    // {
    //    "result": {},
    //    "url": "https://lf1-hscdn-tos.pstatp
    //    .com/obj/developer-baas/baas/tt7217xbo2wz3cem41/a8efa55c5c22de69_1560563154288.mp4",
    //    "success": true
    //}
    @SerializedName("success")
    private boolean success;

    @SerializedName("error")
    private String error;

    @SerializedName("item")
    private item item;

    @SerializedName("url")
    private String url;

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "PostVideoResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", item=" + item +
                ", url='" + url + '\'' +
                '}';
    }

    public static class item {
        @SerializedName("student_id")
        private String student_id;
        @SerializedName("user_name")
        private String user_name;
        @SerializedName("image_url")
        private String image_url;
        @SerializedName("video_url")
        private String video_url;

        @Override
        public String toString() {
            return "item{" +
                    "student_id='" + student_id + '\'' +
                    ", user_name='" + user_name + '\'' +
                    ", image_url='" + image_url + '\'' +
                    ", video_url='" + video_url + '\'' +
                    '}';
        }
    }
}
