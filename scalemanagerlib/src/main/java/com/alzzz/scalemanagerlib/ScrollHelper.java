package com.alzzz.scalemanagerlib;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class ScrollHelper {
    /* package */ static void smoothScrollToPosition(RecyclerView recyclerView, CardScaleLayoutManager viewPagerLayoutManager, int targetPosition) {
        final int delta = viewPagerLayoutManager.getOffsetToPosition(targetPosition);
        if (viewPagerLayoutManager.getOrientation() == CardScaleLayoutManager.VERTICAL) {
            recyclerView.smoothScrollBy(0, delta);
        } else {
            recyclerView.smoothScrollBy(delta, 0);
        }
    }

    public static void smoothScrollToTargetView(RecyclerView recyclerView, View targetView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof CardScaleLayoutManager)) return;
        final int targetPosition = ((CardScaleLayoutManager) layoutManager).getLayoutPositionOfView(targetView);
        smoothScrollToPosition(recyclerView, (CardScaleLayoutManager) layoutManager, targetPosition);
    }
}
