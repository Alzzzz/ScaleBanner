package com.alzzz.detail;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ScrollHelper {
    /* package */ static void smoothScrollToPosition(RecyclerView recyclerView, HouseBannerLayoutManager viewPagerLayoutManager, int targetPosition) {
        final int delta = viewPagerLayoutManager.getOffsetToPosition(targetPosition);
        if (viewPagerLayoutManager.getOrientation() == HouseBannerLayoutManager.VERTICAL) {
            recyclerView.smoothScrollBy(0, delta);
        } else {
            recyclerView.smoothScrollBy(delta, 0);
        }
    }

    public static void smoothScrollToTargetView(RecyclerView recyclerView, View targetView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof HouseBannerLayoutManager)) return;
        final int targetPosition = ((HouseBannerLayoutManager) layoutManager).getLayoutPositionOfView(targetView);
        smoothScrollToPosition(recyclerView, (HouseBannerLayoutManager) layoutManager, targetPosition);
    }
}
