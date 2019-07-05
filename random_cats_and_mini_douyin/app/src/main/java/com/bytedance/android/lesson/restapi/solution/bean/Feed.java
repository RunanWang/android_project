package com.bytedance.android.lesson.restapi.solution.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Xavier.S
 * @date 2019.01.20 14:18
 */
public class Feed {
    /**
     * {
     * "feeds": [
     * {
     * "student_id": "2220186666",
     * "user_name": "doudou",
     * "image_url": "https://sf6-hscdn-tos.pstatp.com/obj/developer-baas/baas/tt7217xbo2wz3cem41/9c6bbc2aa5355504_1560563154279.jpg",
     * "_id": "5d044dd222e26f0024157401",
     * "video_url": "https://lf1-hscdn-tos.pstatp.com/obj/developer-baas/baas/tt7217xbo2wz3cem41/a8efa55c5c22de69_1560563154288.mp4",
     * "createdAt": "2019-06-15T01:45:54.368Z",
     * "updatedAt": "2019-06-15T01:45:54.368Z",
     * "__v": 0
     * }
     * }
     */

    // TODO-C2 (1) Implement your Feed Bean here according to the response json

    @SerializedName("student_id")
    private String student_id;
    @SerializedName("user_name")
    private String user_name;
    @SerializedName("image_url")
    private String image_url;
    @SerializedName("video_url")
    private String video_url;

    public String getStudent_id() {
        return student_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setStudent_id(String student_id) {
        this.student_id = student_id;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    @Override
    public String toString() {
        return "feesInfo{" +
                "student_id='" + student_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", image_url='" + image_url + '\'' +
                ", video_url='" + video_url + '\'' +
                '}';
    }


}
