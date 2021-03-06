package com.alzzz.scalemanagerlib;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;

import java.util.ArrayList;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * An implementation of {@link RecyclerView.LayoutManager} which behaves like view pager.
 * Please make sure your child view have the same size.
 *
 * OrientationHelper.VERTICAL unavailable now
 */

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public abstract class CardScaleLayoutManager extends LinearLayoutManager {

    public static final int DETERMINE_BY_MAX_AND_MIN = -1;

    public static final int HORIZONTAL = OrientationHelper.HORIZONTAL;

    public static final int VERTICAL = OrientationHelper.VERTICAL;

    private static final int DIRECTION_NO_WHERE = -1;

    private static final int DIRECTION_FORWARD = 0;

    private static final int DIRECTION_BACKWARD = 1;

    protected static final int INVALID_SIZE = Integer.MAX_VALUE;

    private SparseArray<View> positionCache = new SparseArray<>();

    protected int mDecoratedMeasurement;

    protected int mDecoratedMeasurementInOther;

    /**
     * Current orientation. Either {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    int mOrientation;

    protected int mSpaceMain;

    protected int mSpaceInOther;

    /**
     * The offset of property which will change while scrolling
     */
    protected float mOffset;

    /**
     * Many calculations are made depending on orientation. To keep it clean, this interface
     * helps {@link LinearLayoutManager} make those decisions.
     * Based on {@link #mOrientation}, an implementation is lazily created in
     * {@link #ensureLayoutState} method.
     */
    protected OrientationHelper mOrientationHelper;

    /**
     * Defines if layout should be calculated from end to start.
     */
    private boolean mReverseLayout = false;

    /**
     * Works the same way as {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}.
     * see {@link android.widget.AbsListView#setSmoothScrollbarEnabled(boolean)}
     */
    private boolean mSmoothScrollbarEnabled = true;

    /**
     * When LayoutManager needs to scroll to a position, it sets this variable and requests a
     * layout which will check this variable and re-layout accordingly.
     */
    private int mPendingScrollPosition = NO_POSITION;

    private SavedState mPendingSavedState = null;

    protected float mInterval; //the mInterval of each item's mOffset

    /* package */ OnPageChangeListener onPageChangeListener;

    private boolean mRecycleChildrenOnDetach;

    private boolean mInfinite = false;

    private boolean mEnableBringCenterToFront;

    private int mLeftItems;

    private int mRightItems;

    /**
     * max visible item count
     */
    private int mMaxVisibleItemCount = DETERMINE_BY_MAX_AND_MIN;

    private Interpolator mSmoothScrollInterpolator;

    private int mDistanceToBottom = INVALID_SIZE;

    /**
     * use for handle focus
     */
    private View currentFocusView;

    /**
     * @return the mInterval of each item's mOffset
     */
    protected abstract float setInterval();

    protected abstract void setItemViewProperty(View itemView, float targetOffset, float targetScale);

    /**
     * Get zoom ratio
     * @param position
     * @return
     */
    protected abstract float getTargetScale(int position);
    /**
     * cause elevation is not support below api 21,
     * so you can set your elevation here for supporting it below api 21
     * or you can just setElevation in {@link #setItemViewProperty(View, float, float)}
     */
    protected float setViewElevation(View itemView, float targetOffset) {
        return 0;
    }

    /**
     * Creates a horizontal CardScaleLayoutManager
     */
    public CardScaleLayoutManager(Context context) {
        this(context, HORIZONTAL, false);
    }

    /**
     * @param orientation   Layout orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}
     * @param reverseLayout When set to true, layouts from end to start
     */
    public CardScaleLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context);
        setOrientation(orientation);
        setReverseLayout(reverseLayout);
        setAutoMeasureEnabled(true);
        setItemPrefetchEnabled(false);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /**
     * Returns whether LayoutManager will recycle its children when it is detached from
     * RecyclerView.
     *
     * @return true if LayoutManager will recycle its children when it is detached from
     * RecyclerView.
     */
    public boolean getRecycleChildrenOnDetach() {
        return mRecycleChildrenOnDetach;
    }

    /**
     * Set whether LayoutManager will recycle its children when it is detached from
     * RecyclerView.
     * <p>
     * If you are using a {@link RecyclerView.RecycledViewPool}, it might be a good idea to set
     * this flag to <code>true</code> so that views will be available to other RecyclerViews
     * immediately.
     * <p>
     * Note that, setting this flag will result in a performance drop if RecyclerView
     * is restored.
     *
     * @param recycleChildrenOnDetach Whether children should be recycled in detach or not.
     */
    public void setRecycleChildrenOnDetach(boolean recycleChildrenOnDetach) {
        mRecycleChildrenOnDetach = recycleChildrenOnDetach;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (mRecycleChildrenOnDetach) {
            removeAndRecycleAllViews(recycler);
            recycler.clear();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (mPendingSavedState != null) {
            return new SavedState(mPendingSavedState);
        }
        SavedState savedState = new SavedState();
        savedState.position = mPendingScrollPosition;
        savedState.offset = mOffset;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            mPendingSavedState = new SavedState((SavedState) state);
            requestLayout();
        }
    }

    /**
     * @return true if {@link #getOrientation()} is {@link #HORIZONTAL}
     */
    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    /**
     * @return true if {@link #getOrientation()} is {@link #VERTICAL}
     */
    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    /**
     * Returns the current orientation of the layout.
     *
     * @return Current orientation,  either {@link #HORIZONTAL} or {@link #VERTICAL}
     * @see #setOrientation(int)
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * Sets the orientation of the layout. {@link CardScaleLayoutManager}
     * will do its best to keep scroll position.
     *
     * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
     */
    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL && orientation != VERTICAL) {
            throw new IllegalArgumentException("invalid orientation:" + orientation);
        }
        assertNotInLayoutOrScroll(null);
        if (orientation == mOrientation) {
            return;
        }
        mOrientation = orientation;
        mOrientationHelper = null;
        mDistanceToBottom = INVALID_SIZE;
        removeAllViews();
    }

    /**
     * Returns the max visible item count, {@link #DETERMINE_BY_MAX_AND_MIN} means it haven't been set now
     * And it will use {@link #maxRemoveOffset()} and {@link #minRemoveOffset()} to handle the range
     *
     * @return Max visible item count
     */
    public int getMaxVisibleItemCount() {
        return mMaxVisibleItemCount;
    }

    /**
     * Set the max visible item count, {@link #DETERMINE_BY_MAX_AND_MIN} means it haven't been set now
     * And it will use {@link #maxRemoveOffset()} and {@link #minRemoveOffset()} to handle the range
     *
     * @param mMaxVisibleItemCount Max visible item count
     */
    public void setMaxVisibleItemCount(int mMaxVisibleItemCount) {
        assertNotInLayoutOrScroll(null);
        if (this.mMaxVisibleItemCount == mMaxVisibleItemCount) return;
        this.mMaxVisibleItemCount = mMaxVisibleItemCount;
        removeAllViews();
    }

    /**
     * Returns if views are laid out from the opposite direction of the layout.
     *
     * @return If layout is reversed or not.
     * @see #setReverseLayout(boolean)
     */
    public boolean getReverseLayout() {
        return mReverseLayout;
    }

    /**
     * Used to reverse item traversal and layout order.
     * This behaves similar to the layout change for RTL views. When set to true, first item is
     * laid out at the end of the UI, second item is laid out before it etc.
     * <p>
     * For horizontal layouts, it depends on the layout direction.
     * When set to true, If {@link android.support.v7.widget.RecyclerView} is LTR, than it will
     * layout from RTL, if {@link android.support.v7.widget.RecyclerView}} is RTL, it will layout
     * from LTR.
     */
    public void setReverseLayout(boolean reverseLayout) {
        assertNotInLayoutOrScroll(null);
        if (reverseLayout == mReverseLayout) {
            return;
        }
        mReverseLayout = reverseLayout;
        removeAllViews();
    }

    public void setSmoothScrollInterpolator(Interpolator smoothScrollInterpolator) {
        this.mSmoothScrollInterpolator = smoothScrollInterpolator;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final int offsetPosition;

        // fix wrong scroll direction when infinite enable
        if (mInfinite) {
            final int currentPosition = getCurrentPosition();
            final int total = getItemCount();
            final int targetPosition;
            if (position < currentPosition) {
                int d1 = currentPosition - position;
                int d2 = total - currentPosition + position;
                targetPosition = d1 < d2 ? (currentPosition - d1) : (currentPosition + d2);
            } else {
                int d1 = position - currentPosition;
                int d2 = currentPosition + total - position;
                targetPosition = d1 < d2 ? (currentPosition + d1) : (currentPosition - d2);
            }

            offsetPosition = getOffsetToPosition(targetPosition);
        } else {
            offsetPosition = getOffsetToPosition(position);
        }

        if (mOrientation == VERTICAL) {
            recyclerView.smoothScrollBy(0, offsetPosition, mSmoothScrollInterpolator);
        } else {
            recyclerView.smoothScrollBy(offsetPosition, 0, mSmoothScrollInterpolator);
        }
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            mOffset = 0;
            return;
        }

        ensureLayoutState();

        //make sure properties are correct while measure more than once
        View scrap = getMeasureView(recycler, state, 0);
        if (scrap == null) {
            removeAndRecycleAllViews(recycler);
            mOffset = 0;
            return;
        }

        measureChildWithMargins(scrap, 0, 0);
        mDecoratedMeasurement = mOrientationHelper.getDecoratedMeasurement(scrap);
        mDecoratedMeasurementInOther = mOrientationHelper.getDecoratedMeasurementInOther(scrap);
        //首个和最后一个距离屏幕的距离
        mSpaceMain = (mOrientationHelper.getTotalSpace() - mDecoratedMeasurement) / 2;
        if (mDistanceToBottom == INVALID_SIZE) {
            mSpaceInOther = (mOrientationHelper.getTotalSpaceInOther() - mDecoratedMeasurementInOther) / 2;
        } else {
            mSpaceInOther = mOrientationHelper.getTotalSpaceInOther() - mDecoratedMeasurementInOther - mDistanceToBottom;
        }
        //每一个View的大小
        mInterval = setInterval();
        setUp();
        if (mInterval == 0) {
            mLeftItems = 1;
            mRightItems = 1;
        } else {
            mLeftItems = (int) Math.abs(minRemoveOffset() / mInterval) + 1;
            mRightItems = (int) Math.abs(maxRemoveOffset() / mInterval) + 1;
        }

        if (mPendingSavedState != null) {
            mPendingScrollPosition = mPendingSavedState.position;
            mOffset = mPendingSavedState.offset;
        }

        if (mPendingScrollPosition != NO_POSITION) {
            mOffset = mPendingScrollPosition * mInterval;
        }

        layoutItems(recycler);
    }

    private View getMeasureView(RecyclerView.Recycler recycler, RecyclerView.State state, int index) {
        if (index >= state.getItemCount() || index < 0) return null;
        try {
            return recycler.getViewForPosition(index);
        } catch (Exception e) {
            return getMeasureView(recycler, state, index + 1);
        }
    }

    @Override
    public void onLayoutCompleted(RecyclerView.State state) {
        super.onLayoutCompleted(state);
        mPendingSavedState = null;
        mPendingScrollPosition = NO_POSITION;
    }

    @Override
    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        final int currentPosition = getCurrentPosition();
        final View currentView = findViewByPosition(currentPosition);
        if (currentView == null) return true;
        if (recyclerView.hasFocus()) {
            final int movement = getMovement(direction);
            if (movement != DIRECTION_NO_WHERE) {
                final int targetPosition = movement == DIRECTION_BACKWARD ?
                        currentPosition - 1 : currentPosition + 1;
                ScrollHelper.smoothScrollToPosition(recyclerView, this, targetPosition);
            }
        } else {
            currentView.addFocusables(views, direction, focusableMode);
        }
        return true;
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return null;
    }

    private int getMovement(int direction) {
        if (mOrientation == VERTICAL) {
            if (direction == View.FOCUS_UP) {
                return DIRECTION_BACKWARD;
            } else if (direction == View.FOCUS_DOWN) {
                return DIRECTION_FORWARD;
            } else {
                return DIRECTION_NO_WHERE;
            }
        } else {
            if (direction == View.FOCUS_LEFT) {
                return DIRECTION_BACKWARD;
            } else if (direction == View.FOCUS_RIGHT) {
                return DIRECTION_FORWARD;
            } else {
                return DIRECTION_NO_WHERE;
            }
        }
    }

    void ensureLayoutState() {
        if (mOrientationHelper == null) {
            mOrientationHelper = OrientationHelper.createOrientationHelper(this, mOrientation);
        }
    }

    /**
     * You can set up your own properties here or change the exist properties like mSpaceMain and mSpaceInOther
     */
    protected void setUp() {


    }

    protected float getProperty(int position) {
        return position * mInterval + mOrientationHelper.getStartAfterPadding() ;
    }

    /**
     * 是否是最尾部
     * @return
     * @param position
     */
    protected boolean isTail(int position) {
        return position == getItemCount()-1;
    }

    protected boolean isHeader(int position) {
        return position == 0;
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        mOffset = 0;
    }

    @Override
    public void scrollToPosition(int position) {
        if (!mInfinite && (position < 0 || position >= getItemCount())) return;
        mPendingScrollPosition = position;
        mOffset = position * mInterval;
        requestLayout();
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset();
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        return computeScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent();
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return computeScrollExtent();
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return computeScrollRange();
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return computeScrollRange();
    }

    private int computeScrollOffset() {
        if (getChildCount() == 0) {
            return 0;
        }

        if (!mSmoothScrollbarEnabled) {
            return getCurrentPosition();
        }

        final float realOffset = getOffsetOfRightAdapterPosition();
        return (int) realOffset;
    }

    private int computeScrollExtent() {
        if (getChildCount() == 0) {
            return 0;
        }

        if (!mSmoothScrollbarEnabled) {
            return 1;
        }

        return (int) mInterval;
    }

    private int computeScrollRange() {
        if (getChildCount() == 0) {
            return 0;
        }

        if (!mSmoothScrollbarEnabled) {
            return getItemCount();
        }

        return (int) (getItemCount() * mInterval);
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == VERTICAL) {
            return 0;
        }
        return scrollBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == HORIZONTAL) {
            return 0;
        }
        return scrollBy(dy, recycler, state);
    }

    private int scrollBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        ensureLayoutState();
        int willScroll = dy;

        float realDx = dy / getDistanceRatio();
        if (Math.abs(realDx) < 0.00000001f) {
            return 0;
        }
        float targetOffset = mOffset + realDx;

        //handle the boundary
        if (!mInfinite && targetOffset < getMinOffset()) {
            willScroll -= (targetOffset - getMinOffset()) * getDistanceRatio();
        } else if (!mInfinite && targetOffset > getMaxOffset()) {
            willScroll = (int) ((getMaxOffset() - mOffset) * getDistanceRatio());
        }

        realDx = willScroll / getDistanceRatio();

        mOffset += realDx;

        //handle recycle
        layoutItems(recycler);

        return willScroll;
    }

    private void layoutItems(RecyclerView.Recycler recycler) {
        detachAndScrapAttachedViews(recycler);
        positionCache.clear();

        final int itemCount = getItemCount();
        if (itemCount == 0) return;

        // make sure that current position start from 0 to 1
        final int currentPos = getCurrentPositionOffset();
        int start = currentPos - mLeftItems;
        int end = currentPos + mRightItems;

        // handle max visible count
        if (useMaxVisibleCount()) {
            boolean isEven = mMaxVisibleItemCount % 2 == 0;
            if (isEven) {
                int offset = mMaxVisibleItemCount / 2;
                start = currentPos - offset + 1;
                end = currentPos + offset + 1;
            } else {
                int offset = (mMaxVisibleItemCount - 1) / 2;
                start = currentPos - offset;
                end = currentPos + offset + 1;
            }
        }

        if (!mInfinite) {
            if (start < 0) {
                start = 0;
                if (useMaxVisibleCount()) end = mMaxVisibleItemCount;
            }
            if (end > itemCount) end = itemCount;
        }

        float lastOrderWeight = Float.MIN_VALUE;

        for (int i = start; i < end; i++) {
            if (useMaxVisibleCount() || !removeCondition(getProperty(i) - mOffset)) {
                // start and end base on current position,
                // so we need to calculate the adapter position
                int adapterPosition = i;
                if (i >= itemCount) {
                    adapterPosition %= itemCount;
                } else if (i < 0) {
                    int delta = (-adapterPosition) % itemCount;
                    if (delta == 0) delta = itemCount;
                    adapterPosition = itemCount - delta;
                }
                final View scrap = recycler.getViewForPosition(adapterPosition);
                measureChildWithMargins(scrap, 0, 0);
                resetViewProperty(scrap);
                // we need i to calculate the real offset of current view
                float targetOffset = getProperty(i) - mOffset;
                float targetScale = getTargetScale(i);
                layoutScrap(scrap, targetOffset, targetScale);
                final float orderWeight = mEnableBringCenterToFront ?
                        setViewElevation(scrap, targetOffset) : adapterPosition;
                if (orderWeight > lastOrderWeight) {
                    addView(scrap);
                } else {
                    addView(scrap, 0);
                }
                if (i == currentPos) currentFocusView = scrap;
                lastOrderWeight = orderWeight;
                positionCache.put(i, scrap);
            }
        }

        currentFocusView.requestFocus();
    }

    private boolean useMaxVisibleCount() {
        return mMaxVisibleItemCount != DETERMINE_BY_MAX_AND_MIN;
    }

    private boolean removeCondition(float targetOffset) {
        return targetOffset > maxRemoveOffset() || targetOffset < minRemoveOffset();
    }

    private void resetViewProperty(View v) {
        v.setRotation(0);
        v.setRotationY(0);
        v.setRotationX(0);
        v.setScaleX(1f);
        v.setScaleY(1f);
        v.setAlpha(1f);
    }

    /* package */ float getMaxOffset() {
        float maxOffset = (getItemCount()) * mInterval - mOrientationHelper.getTotalSpace();
        if (maxOffset < 0){
            maxOffset = 0;
        }
        return maxOffset;
    }

    /* package */ float getMinOffset() {
        return 0;
    }

    private void layoutScrap(View scrap, float targetOffset, float targetScale) {
        final int left = calItemLeft(scrap, targetOffset);
        final int top = calItemTop(scrap, targetOffset);
        if (mOrientation == VERTICAL) {
            layoutDecorated(scrap, mSpaceInOther + left, mSpaceMain + top,
                    mSpaceInOther + left + mDecoratedMeasurementInOther, mSpaceMain + top + mDecoratedMeasurement);
        } else {
            layoutDecorated(scrap, left, mSpaceInOther + top,
                    left + mDecoratedMeasurement, mSpaceInOther + top + mDecoratedMeasurementInOther);
        }
        setItemViewProperty(scrap, targetOffset, targetScale);
    }

    protected int calItemLeft(View itemView, float targetOffset) {
        return mOrientation == VERTICAL ? 0 : (int) targetOffset;
    }

    protected int calItemTop(View itemView, float targetOffset) {
        return mOrientation == VERTICAL ? (int) targetOffset : 0;
    }

    /**
     * when the target offset reach this,
     * the view will be removed and recycled in {@link #layoutItems(RecyclerView.Recycler)}
     */
    protected float maxRemoveOffset() {
        //此时的返回并不准确，但一定多余1个
        return mOrientationHelper.getTotalSpace() + mSpaceMain + mDecoratedMeasurement;
    }

    /**
     * when the target offset reach this,
     * the view will be removed and recycled in {@link #layoutItems(RecyclerView.Recycler)}
     */
    protected float minRemoveOffset() {
        return -mOrientationHelper.getStartAfterPadding()- mDecoratedMeasurement;
    }

    protected float getDistanceRatio() {
        return 1f;
    }

    public int getCurrentPosition() {
        if (getItemCount() == 0) return 0;

        int position = getCurrentPositionOffset();
        if (!mInfinite) return Math.abs(position);

        position =(position >= 0 ?
                        position % getItemCount() :
                        getItemCount() + position % getItemCount());
        return position == getItemCount() ? 0 : position;
    }

    @Override
    public View findViewByPosition(int position) {
        final int itemCount = getItemCount();
        if (itemCount == 0) return null;
        for (int i = 0; i < positionCache.size(); i++) {
            final int key = positionCache.keyAt(i);
            if (key >= 0) {
                if (position == key % itemCount) return positionCache.valueAt(i);
            } else {
                int delta = key % itemCount;
                if (delta == 0) delta = -itemCount;
                if (itemCount + delta == position) return positionCache.valueAt(i);
            }
        }
        return null;
    }

    public int getLayoutPositionOfView(View v) {
        for (int i = 0; i < positionCache.size(); i++) {
            int key = positionCache.keyAt(i);
            View value = positionCache.get(key);
            if (value == v) return key;
        }
        return -1;
    }

    /* package */ int getCurrentPositionOffset() {
        if (mInterval == 0) return 0;
        //只对最后一个View进行特殊的偏移处理
        if (mOffset > (getMaxOffset() - mInterval + mSpaceMain)){
            //如果已经偏移到最后一屏,只要当前View超过中间就进行偏移
            return (int) Math.floor((mOffset + mOrientationHelper.getTotalSpace()/2.0)/mInterval);
        }

        return Math.round(mOffset / mInterval);
    }

    /**
     * Sometimes we need to get the right offset of matching adapter position
     * cause when {@link #mInfinite} is set true, there will be no limitation of {@link #mOffset}
     */
    private float getOffsetOfRightAdapterPosition() {
        return mInfinite ?
                (mOffset >= 0 ?
                        (mOffset % (mInterval * getItemCount())) :
                        (getItemCount() * mInterval + mOffset % (mInterval * getItemCount()))) :
                mOffset;
    }

    public int getOffsetToPosition(int position) {
        if (mInfinite)
            return (int) (((getCurrentPositionOffset() + (position - getCurrentPositionOffset())) *
                    mInterval - mOffset) * getDistanceRatio());
        return (int) ((position * mInterval - mOffset) * getDistanceRatio());
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setInfinite(boolean enable) {
        assertNotInLayoutOrScroll(null);
        if (enable == mInfinite) {
            return;
        }
        mInfinite = enable;
        requestLayout();
    }

    public boolean getInfinite() {
        return mInfinite;
    }

    public int getDistanceToBottom() {
        return mDistanceToBottom == INVALID_SIZE ?
                (mOrientationHelper.getTotalSpaceInOther() - mDecoratedMeasurementInOther) / 2 : mDistanceToBottom;
    }

    public void setDistanceToBottom(int mDistanceToBottom) {
        assertNotInLayoutOrScroll(null);
        if (this.mDistanceToBottom == mDistanceToBottom) return;
        this.mDistanceToBottom = mDistanceToBottom;
        removeAllViews();
    }

    /**
     * When smooth scrollbar is enabled, the position and size of the scrollbar thumb is computed
     * based on the number of visible pixels in the visible items. This however assumes that all
     * list items have similar or equal widths or heights (depending on list orientation).
     * If you use a list in which items have different dimensions, the scrollbar will change
     * appearance as the user scrolls through the list. To avoid this issue,  you need to disable
     * this property.
     * <p>
     * When smooth scrollbar is disabled, the position and size of the scrollbar thumb is based
     * solely on the number of items in the adapter and the position of the visible items inside
     * the adapter. This provides a stable scrollbar as the user navigates through a list of items
     * with varying widths / heights.
     *
     * @param enabled Whether or not to enable smooth scrollbar.
     * @see #setSmoothScrollbarEnabled(boolean)
     */
    public void setSmoothScrollbarEnabled(boolean enabled) {
        mSmoothScrollbarEnabled = enabled;
    }

    public void setEnableBringCenterToFront(boolean bringCenterToTop) {
        assertNotInLayoutOrScroll(null);
        if (mEnableBringCenterToFront == bringCenterToTop) {
            return;
        }
        this.mEnableBringCenterToFront = bringCenterToTop;
        requestLayout();
    }

    public boolean getEnableBringCenterToFront() {
        return mEnableBringCenterToFront;
    }

    /**
     * Returns the current state of the smooth scrollbar feature. It is enabled by default.
     *
     * @return True if smooth scrollbar is enabled, false otherwise.
     * @see #setSmoothScrollbarEnabled(boolean)
     */
    public boolean getSmoothScrollbarEnabled() {
        return mSmoothScrollbarEnabled;
    }

    private static class SavedState implements Parcelable {
        int position;
        float offset;

        SavedState() {

        }

        SavedState(Parcel in) {
            position = in.readInt();
            offset = in.readFloat();
        }

        public SavedState(SavedState other) {
            position = other.position;
            offset = other.offset;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(position);
            dest.writeFloat(offset);
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public interface OnPageChangeListener {
        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }
}
