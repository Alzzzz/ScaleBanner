package com.alzzz.detail;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @Description RoomTypeItemDecoration
 * @Date 2019-07-12
 * @Author sz
 */
public class RoomTypeItemDecoration extends RecyclerView.ItemDecoration{
    private int dividerWidth;

    public RoomTypeItemDecoration(int dividerWidth) {
        this.dividerWidth = dividerWidth;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        outRect.left = dividerWidth;
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (adapter == null){
            return;
        }
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount()-1){
            outRect.right = dividerWidth;
        }
    }
}
