package com.alzzz.detail;

/**
 * @Description RoomType
 * @Date 2019-07-12
 * @Author sz
 */
public class RoomType {
    private String coverUrl;
    private int coverRes;
    private String houseTypeName;


    public int getCoverRes() {
        return coverRes;
    }

    public void setCoverRes(int coverRes) {
        this.coverRes = coverRes;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getHouseTypeName() {
        return houseTypeName;
    }

    public void setHouseTypeName(String houseTypeName) {
        this.houseTypeName = houseTypeName;
    }
}
