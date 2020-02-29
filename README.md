# ScaleBanner
基于RecycleView编写的左右滑动ViewPager，与其他ViewPager不同的是，第一个和最后一个item靠边放大，其他的中间放大。

## 样式展示
#### 最前面的的样式
![image](https://github.com/Alzzzz/ScaleBanner/blob/master/images/scale_image_2.jpg)
#### 中间的样式
![image](https://github.com/Alzzzz/ScaleBanner/blob/master/images/scale_image_1.jpg)
#### 最后面的样式
![image](https://github.com/Alzzzz/ScaleBanner/blob/master/images/scale_image_3.jpg)

## 使用方式
1、RecyclerView使用HouseScaleLayoutManager
``` java
    HouseScaleLayoutManager layoutManager = new HouseScaleLayoutManager
            .Builder(this, 0)
            .build();
    layoutManager.setOrientation(CardScaleLayoutManager.HORIZONTAL);
    layoutManager.setInfinite(false);
    recyclerView.setLayoutManager(layoutManager);
```
2、滑动绑定，使用HouseBannerSnapHelper绑定RecyclerView
``` java
    HouseBannerSnapHelper pagerSnapHelper = new HouseBannerSnapHelper();
    pagerSnapHelper.attachToRecyclerView(recyclerView);
```

3、绑定Adapter，自定义View的样式
``` java
    HouseBannerSnapHelper pagerSnapHelper = new HouseBannerSnapHelper();
    pagerSnapHelper.attachToRecyclerView(recyclerView);
```
## 引用方式
