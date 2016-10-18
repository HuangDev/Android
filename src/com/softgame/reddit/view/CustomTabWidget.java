package com.softgame.reddit.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import android.widget.TabHost;

/**
 * must call setHaveDivider(boolean hasChild) before use this TabWidget
 * @author xinyunxixi
 *
 */
public class CustomTabWidget extends LinearLayout implements
		OnFocusChangeListener {
	private OnTabSelectionChanged mSelectionChangedListener;
	private int mSelectedTab = 0;
	Context mContext;
	boolean hasDivider;

	
	public CustomTabWidget(Context context) {
		this(context, null);
	}

	public CustomTabWidget(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomTabWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		mContext = context;
		initTabWidget();
	}

	public int getSelectedTab() {
		return mSelectedTab;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		// Always draw the selected tab last, so that drop shadows are drawn
		// in the correct z-order.
		if (i == childCount - 1) {
			return mSelectedTab;
		} else if (i >= mSelectedTab) {
			return i + 1;
		} else {
			return i;
		}
	}

	private void initTabWidget() {
		setOrientation(LinearLayout.HORIZONTAL);
		// Deal with focus, as we don't want the focus to go by default
		// to a tab other than the current tab
		hasDivider = false;
		setFocusable(true);
		setOnFocusChangeListener(this);
		 //setChildView();
	}

	/**
	 * must call this to make it function
	 * @param h
	 */
	public void setHaveDivider(boolean h) {
		hasDivider = h;
		setChildView();
	}

	/**
	 * Returns the tab indicator view at the given index.
	 * 
	 * @param index
	 *            the zero-based index of the tab indicator view to return
	 * @return the tab indicator view at the given index
	 */
	public View getChildTabViewAt(int index) {
		// If we are using dividers, then instead of tab views at 0, 1, 2, ...
		// we have tab views at 0, 2, 4, ...
		if (hasDivider) {
			index *= 2;
		}
		return getChildAt(index);
	}
	/**
	 * Returns the number of tab indicator views.
	 * 
	 * @return the number of tab indicator views.
	 */
	public int getTabCount() {
		int children = getChildCount();

		if (hasDivider) {
			children = (children + 1) / 2;
		}

		return children;
	}

	@Override
	public void removeViewAt(int index) {
		focusCurrentTab(0);
		super.removeViewAt(index);
	}

	@Override
	public void addView(View view, int index) {
		focusCurrentTab(0);
		super.addView(view, index);
	}

	@Override
	public void childDrawableStateChanged(View child) {
		if (getTabCount() > 0 && child == getChildTabViewAt(mSelectedTab)) {
			// To make sure that the bottom strip is redrawn
			invalidate();
		}
		super.childDrawableStateChanged(child);
	}

	@Override
	public void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
	}

	/**
	 * Sets the current tab. This method is used to bring a tab to the front of
	 * the Widget, and is used to post to the rest of the UI that a different
	 * tab has been brought to the foreground.
	 * 
	 * Note, this is separate from the traditional "focus" that is employed from
	 * the view logic.
	 * 
	 * For instance, if we have a list in a tabbed view, a user may be
	 * navigating up and down the list, moving the UI focus (orange
	 * highlighting) through the list items. The cursor movement does not effect
	 * the "selected" tab though, because what is being scrolled through is all
	 * on the same tab. The selected tab only changes when we navigate between
	 * tabs (moving from the list view to the next tabbed view, in this
	 * example).
	 * 
	 * To move both the focus AND the selected tab at once, please use
	 * {@link #setCurrentTab}. Normally, the view logic takes care of adjusting
	 * the focus, so unless you're circumventing the UI, you'll probably just
	 * focus your interest here.
	 * 
	 * @param index
	 *            The tab that you want to indicate as the selected tab (tab
	 *            brought to the front of the widget)
	 * 
	 * @see #focusCurrentTab
	 */
	public void setCurrentTab(int index) {
		if (index < 0 || index >= getTabCount()) {
			return;
		}
		if (mSelectedTab >= 0 && mSelectedTab < getTabCount()) {
			getChildTabViewAt(mSelectedTab).setSelected(false);
		}
		mSelectedTab = index;
		getChildTabViewAt(mSelectedTab).setSelected(true);
	}

	/**
	 * Sets the current tab and focuses the UI on it. This method makes sure
	 * that the focused tab matches the selected tab, normally at
	 * {@link #setCurrentTab}. Normally this would not be an issue if we go
	 * through the UI, since the UI is responsible for calling
	 * TabWidget.onFocusChanged(), but in the case where we are selecting the
	 * tab programmatically, we'll need to make sure focus keeps up.
	 * 
	 * @param index
	 *            The tab that you want focused (highlighted in orange) and
	 *            selected (tab brought to the front of the widget)
	 * 
	 * @see #setCurrentTab
	 */
	public void focusCurrentTab(int index) {
		final int oldTab = mSelectedTab;

		// set the tab
		setCurrentTab(index);

		// change the focus if applicable.
		if (oldTab != index) {
			getChildTabViewAt(index).requestFocus();
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		int count = getTabCount();

		for (int i = 0; i < count; i++) {
			View child = getChildTabViewAt(i);
			child.setEnabled(enabled);
		}
	}

	// set child

	public void setChildView() {
		for (int i = 0; i < this.getChildCount(); i++) {
			View child = this.getChildAt(i);
			child.setFocusable(true);
			child.setClickable(true);
			child.setOnClickListener(new TabClickListener(i));
			child.setOnFocusChangeListener(this);
		}
	}

	/**
	 * Provides a way for {@link TabHost} to be notified that the user clicked
	 * on a tab indicator.
	 */
	public void setTabSelectionListener(OnTabSelectionChanged listener) {
		mSelectionChangedListener = listener;
	}

	public void onFocusChange(View v, boolean hasFocus) {
		if (v == this && hasFocus && getTabCount() > 0) {
			getChildTabViewAt(mSelectedTab).requestFocus();
			return;
		}

		if (hasFocus) {
			int i = 0;
			int numTabs = getTabCount();
			while (i < numTabs) {
				if (getChildTabViewAt(i) == v) {
					setCurrentTab(i);
					mSelectionChangedListener.onTabSelectionChanged(i, false);
					break;
				}
				i++;
			}
		}
	}

	// registered with each tab indicator so we can notify tab host
	private class TabClickListener implements OnClickListener {

		private final int mTabIndex;

		private TabClickListener(int tabIndex) {
			mTabIndex = tabIndex;
		}

		public void onClick(View v) {
			mSelectionChangedListener.onTabSelectionChanged(mTabIndex, true);
		}
	}

	/**
	 * Let {@link TabHost} know that the user clicked on a tab indicator.
	 */
	public static interface OnTabSelectionChanged {
		/**
		 * Informs the TabHost which tab was selected. It also indicates if the
		 * tab was clicked/pressed or just focused into.
		 * 
		 * @param tabIndex
		 *            index of the tab that was selected
		 * @param clicked
		 *            whether the selection changed due to a touch/click or due
		 *            to focus entering the tab through navigation. Pass true if
		 *            it was due to a press/click and false otherwise.
		 */
		public void onTabSelectionChanged(int tabIndex, boolean clicked);
	}

}
