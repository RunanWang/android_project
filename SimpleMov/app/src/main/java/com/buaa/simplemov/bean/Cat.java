package com.buaa.simplemov.bean;

import com.google.gson.annotations.SerializedName;

/**
 * @author Xavier.S
 * @date 2019.01.17 18:08
 */
public class Cat {

        @SerializedName("id") private  String id;
        @SerializedName("url") private String url;
        @SerializedName("width") private int width;
        @SerializedName("height") private int height;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public String toString() {
            return "Cat{" +
                    "id='" + id + '\'' +
                    ", url='" + url + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }

/**[
 * {
 *         "breeds": [],
 *         "id": "cbv",
 *         "url": "https://cdn2.thecatapi.com/images/cbv.jpg",
 *         "width": 900,
 *         "height": 675
 * }
 * ]
 */
    // Implement your Cat Bean here according to the response json


}
