package com.softgame.reddit.fragment;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;

import org.json.custom.JSONArray;
import org.json.custom.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.softgame.reddit.CommentFragmentActivity;
import com.softgame.reddit.OverviewCommentActivity;
import com.softgame.reddit.OverviewFragmentActivity;
import com.softgame.reddit.R;
import com.softgame.reddit.dialog.CustomDialogHandler;
import com.softgame.reddit.dialog.PostCommentDialog;
import com.softgame.reddit.impl.OnPostCommentListener;
import com.softgame.reddit.model.Comment;
import com.softgame.reddit.model.CommentIndex;
import com.softgame.reddit.model.CommentIndexModel;
import com.softgame.reddit.model.CommentModel;
import com.softgame.reddit.model.SubRedditItem;
import com.softgame.reddit.utils.Common;
import com.softgame.reddit.utils.CommonUtil;
import com.softgame.reddit.utils.RedditCommentManager;
import com.softgame.reddit.utils.RedditManager;
import com.softgame.reddit.utils.SubRedditManager;
import com.softgame.reddit.view.ExpendAnimation;

public class OverviewCommentFragment extends SherlockListFragment implements
		OnItemLongClickListener, OnPostCommentListener, OnItemSelectedListener,
		OnItemClickListener, CustomDialogHandler, OnClickListener {

	public static final String TAG = "OverviewCommentFragment";

	public static final int TYPE_COUNT = 10;

	public static final int TYPE_SUBREDDIT = 0;
	public static final int TYPE_COMMENT = 1;
	public static final int TYPE_HIDE = 2;
	public static final int TYPE_LOADING = 3;
	public static final int TYPE_NO_COMMENT = 4;
	public static final int TYPE_COMMENT_SORT = 5;
	public static final int TYPE_LOAD_FAIL = 6;
	public static final int TYPE_SHOW_ALL_COMMENT = 7;
	// yellow background
	public static final int TYPE_CONTEXT_COMMENT = 8;
	public static final int TYPE_EMPTY = 9;

	LayoutInflater mLayoutInflater;

	public CommentAdapter mCommentAdapter;

	// original list
	public CommentModel mCommentModel;

	public SubRedditItem mSubRedditItem;
	// new order list
	public CommentIndexModel mCommentIndexModel;
	WeakReference<CommentTask> mCommentTaskWF;

	int mSortIndex;
	int mCommentCount = 0;

	String mCommentUrl;

	int mIndicatorColor;

	String mContextCommentId;

	String mIndicator = "9999999999999999999999999999999999999999999999999999999999999999";

	// boolean loadFromRestore = false;

	String[] mCommentCountString;

	String mLoginUserName;

	boolean mSafeForWork;
	int mLoadType;

	PostCommentDialog mPostCommentDialog;

	public boolean mIsPostingComment = false;
	public int mPostingPosition = 0;
	public String mInputPosting = "";
	public boolean mIsEditComment = false;
	public int mDropDownHeight;
	public boolean mHasUserLoagin;

	public OverviewCommentFragment() {
	}

	public static OverviewCommentFragment findOrCreateOveriviewCommentFragment(
			FragmentManager manager, String commentUrl, int loadType, long id) {
		OverviewCommentFragment fragment = (OverviewCommentFragment) manager
				.findFragmentByTag(OverviewCommentFragment.TAG + id);
		if (fragment == null) {
			fragment = new OverviewCommentFragment();
			Bundle b = new Bundle();
			b.putString("common_url", commentUrl);
			b.putInt("load_type", loadType);
			fragment.setArguments(b);
			final FragmentTransaction ft = manager.beginTransaction();
			ft.add(android.R.id.content, fragment, OverviewCommentFragment.TAG
					+ id);
			ft.commit();
		}
		return fragment;
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		if (mSubRedditItem != null) {
			// Used to put dark icons on light action bar
			// boolean isLight = SampleList.THEME ==
			// R.style.Theme_Sherlock_Light;
			inflater.inflate(R.menu.comment_activity_menu, menu);
			// Set file with share history to the provider and set the share
			// intent.
			MenuItem actionItem = menu
					.findItem(R.id.menu_item_share_action_provider_action_bar);
			ShareActionProvider actionProvider = (ShareActionProvider) actionItem
					.getActionProvider();
			actionProvider.setShareHistoryFileName(null);
			// Note that you can set/change the intent any time,
			// say when the user has selected an image.
			actionProvider.setShareIntent(createShareIntent());
		}

		// Note that you can set/change the intent any time,
		// say when the user has selected an image.
		// actionProvider.setShareIntent(createShareIntent());
	}

	public void showCommentHiddenMenu(View menus) {
		View v = menus.findViewById(R.id.hidden_menu);
		ExpendAnimation animation = null;
		if (v.getVisibility() != View.GONE) {
			animation = new ExpendAnimation(v, 555, 1);
		} else {
			animation = new ExpendAnimation(v, 333, 0);
		}
		menus.startAnimation(animation);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_action_mode:
			((OverviewCommentActivity) this.getActivity()).showActionMode();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public Intent createShareIntent() {
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
		sharingIntent.setType("text/plain");
		String shareBody = "http://www.reddit.com" + mSubRedditItem.permalink;
		String subject = "Reddit this! " + mSubRedditItem.title;
		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
		return sharingIntent;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);
		mCommentUrl = this.getArguments() == null ? "" : this.getArguments()
				.getString("common_url");
		if (mCommentUrl == null || "".equals(mCommentUrl)) {
			Toast.makeText(getActivity(), "Comments missing!",
					Toast.LENGTH_SHORT).show();
			this.getActivity().finish();
		}

		mLoadType = this.getArguments() == null ? OverviewCommentActivity.LOAD_TYPE_LINK_ID
				: this.getArguments().getInt("load_type");

		if (mLoadType == OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT_CONTEXT) {
			mContextCommentId = getContextCommentId(mCommentUrl);
		}
		mIndicatorColor = OverviewCommentFragment.this
				.getActivity()
				.getResources()
				.getColor(
						CommonUtil.getIndicateColor(
								OverviewCommentFragment.this.getActivity(),
								R.string.pref_theme_key));
		mLoginUserName = RedditManager.getUserName(this.getActivity());
		mSafeForWork = CommonUtil.safeForWork(this.getActivity());
		mCommentModel = new CommentModel();
		mCommentIndexModel = new CommentIndexModel();
		mCommentAdapter = new CommentAdapter();
		updateDataState();

		mDropDownHeight = this.getActivity().getResources()
				.getDimensionPixelSize(R.dimen.drop_item_height);
		mHasUserLoagin = RedditManager.isUserAuth(this.getActivity());
	}

	public SubRedditItem getSubRedditItem() {
		return mSubRedditItem;
	}

	public String getContextCommentId(String link) {
		try {
			URL linkURL = new URL(link);
			String path = linkURL.getPath();
			if (path.endsWith("/")) {
				path = path.substring(0, path.length() - 1);
			}
			int start = path.lastIndexOf("/");
			if (start != -1) {
				return path.substring(start + 1, path.length());
			} else {
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		return inflater.inflate(R.layout.subreddit_fragment_listview,
				container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.getListView().setAdapter(mCommentAdapter);
		this.getListView().setOnItemClickListener(this);
		this.getListView().setOnItemLongClickListener(this);
	}

	public void updateDataState() {
		if (mCommentTaskWF != null && mCommentTaskWF.get() != null) {
			mCommentTaskWF.get().cancel(true);
		}
		CommentTask d = new CommentTask(this, mCommentUrl);
		mCommentTaskWF = new WeakReference<CommentTask>(d);
		d.execute();
	}

	public void updateIndexList() {
		mCommentIndexModel.convertToCommentIndex(mCommentModel);
		mCommentAdapter.notifyDataSetChanged();
	}

	private void initCommentCount(long num) {
		int count;
		int i = (int) ((num - 1) / 100);
		count = i + 1;
		if (count > 5) {
			count = 5;
		}

		mCommentCountString = new String[count];

		if (num <= 100) {
			mCommentCountString[0] = "show all " + num;
		} else {
			mCommentCountString[0] = "top 100 comments";

			if (num <= 200) {
				mCommentCountString[1] = "show all " + num;
			} else {
				mCommentCountString[1] = "top 200 comments";

				if (num <= 300) {
					mCommentCountString[2] = "show all " + num;
				} else {
					mCommentCountString[2] = "top 300 comments";

					if (num <= 400) {
						mCommentCountString[3] = "show all " + num;
					} else {
						mCommentCountString[3] = "top 400 comments";

						if (num <= 500) {
							mCommentCountString[4] = "show all " + num;
						} else {
							mCommentCountString[4] = "top 500 comments";
						}
					}
				}
			}
		}
	}

	public class CommentAdapter extends BaseAdapter {
		boolean mLoadFail = false;
		boolean mIsLoading = false;

		@Override
		public int getCount() {

			if (mIsLoading) {
				// subreddit and loading
				if (mSubRedditItem == null) {
					return 1; // loading
				} else {
					return 3; // subreddit show all/empty loading
				}
			}
			if (mLoadFail) {
				// subreddit and loading
				if (mSubRedditItem == null) {
					return 1; // loading
				} else {
					return 3; // subreddit show all/empty load fail
				}

			} else {
				if (mSubRedditItem != null && mSubRedditItem.num_comments == 0) {
					// subreddit and no comment
					return 2;
				} else {
					if (mSubRedditItem == null) {
						return 1;
					}
					// show all/ empty and comment sort and size of comment list
					return mCommentIndexModel.getSize() + 2;
				}
			}

		}

		@Override
		public int getViewTypeCount() {
			return TYPE_COUNT;
		}

		@Override
		public Object getItem(int position) {
			return mCommentIndexModel.getOverviewItem(position);
		}

		@Override
		public int getItemViewType(int position) {
			if (position == 0 && mSubRedditItem != null) {
				return TYPE_SUBREDDIT;
			}

			if (position == 1
					&& mSubRedditItem != null
					&& mLoadType == OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT_CONTEXT) {
				return TYPE_SHOW_ALL_COMMENT;
			}

			if (position == 1
					&& mSubRedditItem != null
					&& mLoadType != OverviewCommentActivity.LOAD_TYPE_SHARE_COMMENT_CONTEXT) {
				return TYPE_EMPTY;
			}

			if (mIsLoading) {
				return TYPE_LOADING;
			} else if (mLoadFail) {
				return TYPE_LOAD_FAIL;
			} else if (mSubRedditItem == null
					|| mSubRedditItem.num_comments == 0) {
				return TYPE_NO_COMMENT;
			}
			if (position == 2) {
				return TYPE_COMMENT_SORT;
			}

			Object item = this.getItem(position);
			if (item != null && item instanceof CommentIndex) {
				Comment rc = ((CommentIndex) item).redditComment;
				if (!rc.show) {
					return TYPE_HIDE;
				}
				if ("t1".equals(rc.kind)
						&& rc.id.equalsIgnoreCase(mContextCommentId)) {
					return TYPE_CONTEXT_COMMENT;
				}

				if ("t1".equals(rc.kind)
						&& !rc.id.equalsIgnoreCase(mContextCommentId)) {
					return TYPE_COMMENT;
				}
			}
			return TYPE_HIDE;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			switch (this.getItemViewType(position)) {
			case TYPE_NO_COMMENT:
			case TYPE_COMMENT_SORT:
			case TYPE_LOADING:
			case TYPE_EMPTY:
			case TYPE_SUBREDDIT:
				return false;
			}
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int type = this.getItemViewType(position);
			Object item = this.getItem(position);
			switch (type) {

			case TYPE_EMPTY:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(R.layout.item_empty,
							null);
				}
				break;
			case TYPE_SHOW_ALL_COMMENT:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_show_all_comment, null);
				}
				break;

			case TYPE_CONTEXT_COMMENT:

				CommentIndex ic_item = (CommentIndex) item;
				CommentViewHolder cCommentViewHolder;
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_comment_context, null);
					cCommentViewHolder = new CommentViewHolder();
					cCommentViewHolder.comment = (TextView) convertView
							.findViewById(R.id.comment);
					cCommentViewHolder.comment_author = (TextView) convertView
							.findViewById(R.id.comment_author);
					cCommentViewHolder.comment_score = (TextView) convertView
							.findViewById(R.id.comment_score);
					cCommentViewHolder.comment_indicator = (TextView) convertView
							.findViewById(R.id.comment_indicator);
					cCommentViewHolder.comment_space = (TextView) convertView
							.findViewById(R.id.comment_space);
					cCommentViewHolder.comment_time = (TextView) convertView
							.findViewById(R.id.comment_time);
					cCommentViewHolder.comment_vote = (ImageView) convertView
							.findViewById(R.id.comment_vote);

					cCommentViewHolder.hiddenMenu = (LinearLayout) convertView
							.findViewById(R.id.hidden_menu);

					cCommentViewHolder.mComment = (LinearLayout) convertView
							.findViewById(R.id.comment_post_comment);
					cCommentViewHolder.mVoteUp = (LinearLayout) convertView
							.findViewById(R.id.comment_vote_up);
					cCommentViewHolder.mVoteDown = (LinearLayout) convertView
							.findViewById(R.id.comment_vote_down);
					cCommentViewHolder.mVoteUpIcon = (ImageView) convertView
							.findViewById(R.id.comment_vote_up_icon);
					cCommentViewHolder.mVoteDownIcon = (ImageView) convertView
							.findViewById(R.id.comment_vote_down_icon);
					cCommentViewHolder.mProfile = (LinearLayout) convertView
							.findViewById(R.id.comment_profile);

					cCommentViewHolder.mDeleteComment = (LinearLayout) convertView
							.findViewById(R.id.comment_delete);
					cCommentViewHolder.mEditComment = (LinearLayout) convertView
							.findViewById(R.id.comment_edit);

					convertView.setTag(cCommentViewHolder);

				} else {
					cCommentViewHolder = (CommentViewHolder) convertView
							.getTag();
				}

				cCommentViewHolder.comment_space.setText(mIndicator.substring(
						0, ic_item.deep < 20 ? ic_item.deep : 20));

				int capha = 255 - 20 * ic_item.deep;
				if (capha > 255) {
					capha = 255;
				}
				if (capha <= 60) {
					capha = 60;
				}
				ColorDrawable colordw = new ColorDrawable(mIndicatorColor);

				colordw.setAlpha(capha);
				cCommentViewHolder.comment_indicator
						.setBackgroundDrawable(colordw);

				cCommentViewHolder.comment_author
						.setText(ic_item.redditComment.author);

				if (ic_item.redditComment.author != null
						&& ic_item.redditComment.author
								.equals(mSubRedditItem.author)) {
					cCommentViewHolder.comment_author
							.setBackgroundResource(R.drawable.bg_author_name);
				} else if (ic_item.redditComment.author != null
						&& mLoginUserName != null
						&& ic_item.redditComment.author
								.equalsIgnoreCase(mLoginUserName)) {
					cCommentViewHolder.comment_author
							.setBackgroundResource(R.drawable.bg_me_name);
				} else {
					cCommentViewHolder.comment_author
							.setBackgroundResource(R.drawable.bg_name);
				}

				if (ic_item.redditComment.likes == null) {
					cCommentViewHolder.comment_vote.setVisibility(View.GONE);
				} else if (ic_item.redditComment.likes) {
					cCommentViewHolder.comment_vote.setVisibility(View.VISIBLE);
					cCommentViewHolder.comment_vote
							.setImageResource(R.drawable.vote_up_selected);
				} else {
					cCommentViewHolder.comment_vote.setVisibility(View.VISIBLE);
					cCommentViewHolder.comment_vote
							.setImageResource(R.drawable.vote_down_selected);
				}

				cCommentViewHolder.comment_time.setText(CommonUtil
						.getRelateTimeString(
								ic_item.redditComment.created_utc * 1000,
								OverviewCommentFragment.this.getActivity()));
				long stt = ic_item.redditComment.ups
						- ic_item.redditComment.downs;
				if (stt > 0)
					cCommentViewHolder.comment_score.setText("+" + stt);
				else {
					cCommentViewHolder.comment_score.setText("" + stt);
				}

				cCommentViewHolder.comment.setMovementMethod(LinkMovementMethod
						.getInstance());
				cCommentViewHolder.comment
						.setText(ic_item.redditComment.bodyMarkProcess);
				// Set the convetView to the comment text
				cCommentViewHolder.comment.setTag(convertView);

				cCommentViewHolder.comment
						.setOnClickListener(OverviewCommentFragment.this);

				if (cCommentViewHolder.hiddenMenu.getVisibility() != View.GONE) {
					try {
						int pp = (Integer) cCommentViewHolder.hiddenMenu
								.getTag();
						if (position == pp) {
							showCommentHiddenMenu(convertView);
						} else {
							cCommentViewHolder.hiddenMenu
									.setVisibility(View.GONE);
						}
					} catch (Exception e) {
						cCommentViewHolder.hiddenMenu.setVisibility(View.GONE);
					}

				}
				cCommentViewHolder.hiddenMenu.getLayoutParams().height = mDropDownHeight;
				setDropItem(cCommentViewHolder, position);
				cCommentViewHolder.hiddenMenu.setTag(position);
				break;

			case TYPE_LOAD_FAIL:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_load_fail, null);
				}
				break;

			case TYPE_COMMENT_SORT:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_comment_sort, null);

					Spinner sortSpinner = (Spinner) convertView
							.findViewById(R.id.comments_sort_spinner);
					ArrayAdapter<CharSequence> adapter = ArrayAdapter
							.createFromResource(
									OverviewCommentFragment.this.getActivity(),
									R.array.comments_sort_array,
									R.layout.simple_spinner_text_item);
					adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
					sortSpinner.setAdapter(adapter);
					sortSpinner.setSelection(mSortIndex);
					sortSpinner
							.setOnItemSelectedListener(OverviewCommentFragment.this);

					// add spinner based on the number count
					Spinner commentSpinner = (Spinner) convertView
							.findViewById(R.id.comments_count_spinner);

					ArrayAdapter<CharSequence> commentAdapter = new ArrayAdapter<CharSequence>(
							OverviewCommentFragment.this.getActivity(),
							R.layout.simple_spinner_text_item,
							mCommentCountString);
					commentAdapter
							.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
					commentSpinner.setAdapter(commentAdapter);
					commentSpinner.setSelection(mCommentCount);
					commentSpinner
							.setOnItemSelectedListener(OverviewCommentFragment.this);
				}
				break;
			case TYPE_NO_COMMENT:
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_no_comment, null);
				}
				break;
			case TYPE_LOADING:

				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_loading_comment, null);
				}
				break;
			case TYPE_SUBREDDIT:

				SubRedditItem s_item = (SubRedditItem) item;

				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_comment_post, null);

					TextView content_text = (TextView) convertView
							.findViewById(R.id.subreddit_content_text);

					if (s_item.is_self) {
						if (s_item.getSelftext() != null
								&& s_item.getSelftext().length() != 0) {
							content_text.setVisibility(View.VISIBLE);
							content_text.setText(s_item.getSelftext());
							// ADD LINKS
							Linkify.addLinks(content_text, Linkify.WEB_URLS);
							content_text.setMovementMethod(LinkMovementMethod
									.getInstance());
						}
					}
				}

				LinearLayout subreddit_link_wraper = (LinearLayout) convertView
						.findViewById(R.id.subreddit_link_linear);
				ImageView r_image = (ImageView) convertView
						.findViewById(R.id.subreddit_image);
				LinearLayout r_image_wrapper = (LinearLayout) convertView
						.findViewById(R.id.subreddit_image_wrapper);

				LinearLayout subreddit_link = (LinearLayout) convertView
						.findViewById(R.id.subreddit_link);
				TextView content_text = (TextView) convertView
						.findViewById(R.id.subreddit_content_text);

				r_image.setOnClickListener((OnClickListener) OverviewCommentFragment.this
						.getActivity());
				subreddit_link
						.setOnClickListener((OnClickListener) OverviewCommentFragment.this
								.getActivity());

				if (s_item.is_self) {
					if (s_item.getSelftext() != null
							&& s_item.getSelftext().length() != 0) {
						content_text.setVisibility(View.VISIBLE);
					} else {
						content_text.setVisibility(View.GONE);
					}

					r_image_wrapper.setVisibility(View.GONE);
					subreddit_link_wraper.setVisibility(View.GONE);
				} else {

					content_text.setVisibility(View.GONE);

					if (CommonUtil.isPictureUrl(s_item.url, s_item.domain,
							s_item.subreddit)) {
						subreddit_link_wraper.setVisibility(View.GONE);
						r_image_wrapper.setVisibility(View.VISIBLE);
						((OverviewCommentActivity) OverviewCommentFragment.this
								.getActivity()).getImageWorker().loadImage(
								CommonUtil.appendJPG(s_item.url), r_image);
					} else {
						subreddit_link_wraper.setVisibility(View.GONE);
						subreddit_link_wraper.setVisibility(View.VISIBLE);
						r_image_wrapper.setVisibility(View.GONE);
						ImageView thumb = (ImageView) subreddit_link_wraper
								.findViewById(R.id.subreddit_thumb);

						if (s_item.thumbnail == null
								|| s_item.thumbnail.equals("")
								|| s_item.thumbnail.equals("default")
								|| s_item.thumbnail.equalsIgnoreCase("nsfw")) {
							thumb.setImageResource(R.drawable.icon_reddit_link);
						} else {
							((OverviewCommentActivity) OverviewCommentFragment.this
									.getActivity()).getImageWorker().loadImage(
									s_item.thumbnail, thumb);
						}

						TextView link = (TextView) subreddit_link_wraper
								.findViewById(R.id.subreddit_link_text);

						link.setText(s_item.domain);
					}
				}
				setUpContentView(convertView, s_item, position);
				TextView postTitle = (TextView) convertView
						.findViewById(R.id.post_title);
				postTitle.setText(s_item.title);

				break;
			case TYPE_COMMENT:
				CommentIndex i_item = (CommentIndex) item;
				CommentViewHolder commentViewHolder;
				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_comment, null);
					commentViewHolder = new CommentViewHolder();
					commentViewHolder.comment = (TextView) convertView
							.findViewById(R.id.comment);
					commentViewHolder.comment_author = (TextView) convertView
							.findViewById(R.id.comment_author);
					commentViewHolder.comment_score = (TextView) convertView
							.findViewById(R.id.comment_score);
					commentViewHolder.comment_indicator = (TextView) convertView
							.findViewById(R.id.comment_indicator);
					commentViewHolder.comment_space = (TextView) convertView
							.findViewById(R.id.comment_space);
					commentViewHolder.comment_time = (TextView) convertView
							.findViewById(R.id.comment_time);
					commentViewHolder.comment_vote = (ImageView) convertView
							.findViewById(R.id.comment_vote);

					commentViewHolder.hiddenMenu = (LinearLayout) convertView
							.findViewById(R.id.hidden_menu);

					commentViewHolder.mComment = (LinearLayout) convertView
							.findViewById(R.id.comment_post_comment);
					commentViewHolder.mVoteUp = (LinearLayout) convertView
							.findViewById(R.id.comment_vote_up);
					commentViewHolder.mVoteDown = (LinearLayout) convertView
							.findViewById(R.id.comment_vote_down);
					commentViewHolder.mVoteUpIcon = (ImageView) convertView
							.findViewById(R.id.comment_vote_up_icon);
					commentViewHolder.mVoteDownIcon = (ImageView) convertView
							.findViewById(R.id.comment_vote_down_icon);
					commentViewHolder.mProfile = (LinearLayout) convertView
							.findViewById(R.id.comment_profile);

					commentViewHolder.mDeleteComment = (LinearLayout) convertView
							.findViewById(R.id.comment_delete);
					commentViewHolder.mEditComment = (LinearLayout) convertView
							.findViewById(R.id.comment_edit);

					convertView.setTag(commentViewHolder);

				} else {
					commentViewHolder = (CommentViewHolder) convertView
							.getTag();
				}

				commentViewHolder.comment_space.setText(mIndicator.substring(0,
						i_item.deep < 20 ? i_item.deep : 20));

				int apha = 255 - 20 * i_item.deep;
				if (apha > 255) {
					apha = 255;
				}
				if (apha <= 60) {
					apha = 60;
				}
				ColorDrawable colordws = new ColorDrawable(mIndicatorColor);

				colordws.setAlpha(apha);
				commentViewHolder.comment_indicator
						.setBackgroundDrawable(colordws);

				commentViewHolder.comment_author
						.setText(i_item.redditComment.author);

				if (i_item.redditComment.author != null
						&& i_item.redditComment.author
								.equals(mSubRedditItem.author)) {
					commentViewHolder.comment_author
							.setBackgroundResource(R.drawable.bg_author_name);
				} else if (i_item.redditComment.author != null
						&& mLoginUserName != null
						&& i_item.redditComment.author
								.equalsIgnoreCase(mLoginUserName)) {
					commentViewHolder.comment_author
							.setBackgroundResource(R.drawable.bg_me_name);
				} else {
					commentViewHolder.comment_author
							.setBackgroundResource(R.drawable.bg_name);
				}

				if (i_item.redditComment.likes == null) {
					commentViewHolder.comment_vote.setVisibility(View.GONE);
				} else if (i_item.redditComment.likes) {
					commentViewHolder.comment_vote.setVisibility(View.VISIBLE);
					commentViewHolder.comment_vote
							.setImageResource(R.drawable.vote_up_selected);
				} else {
					commentViewHolder.comment_vote.setVisibility(View.VISIBLE);
					commentViewHolder.comment_vote
							.setImageResource(R.drawable.vote_down_selected);
				}

				commentViewHolder.comment_time.setText(CommonUtil
						.getRelateTimeString(
								i_item.redditComment.created_utc * 1000,
								OverviewCommentFragment.this.getActivity()));
				long sttd = i_item.redditComment.ups
						- i_item.redditComment.downs;
				if (sttd > 0)
					commentViewHolder.comment_score.setText("+" + sttd);
				else {
					commentViewHolder.comment_score.setText("" + sttd);
				}

				commentViewHolder.comment.setMovementMethod(LinkMovementMethod
						.getInstance());
				commentViewHolder.comment
						.setText(i_item.redditComment.bodyMarkProcess);
				// Set the convetView to the comment text
				commentViewHolder.comment.setTag(convertView);

				commentViewHolder.comment
						.setOnClickListener(OverviewCommentFragment.this);

				if (commentViewHolder.hiddenMenu.getVisibility() != View.GONE) {
					try {
						int pp = (Integer) commentViewHolder.hiddenMenu
								.getTag();
						if (position == pp) {
							showCommentHiddenMenu(convertView);
						} else {
							commentViewHolder.hiddenMenu
									.setVisibility(View.GONE);
						}
					} catch (Exception e) {
						commentViewHolder.hiddenMenu.setVisibility(View.GONE);
					}

				}
				commentViewHolder.hiddenMenu.getLayoutParams().height = mDropDownHeight;
				setDropItem(commentViewHolder, position);
				commentViewHolder.hiddenMenu.setTag(position);
				break;
			case TYPE_HIDE:
				CommentIndex h_item = (CommentIndex) item;

				if (convertView == null) {
					convertView = mLayoutInflater.inflate(
							R.layout.item_comment_hide, null);
				}

				// set the space
				TextView hcommentSpace = (TextView) convertView
						.findViewById(R.id.comment_space);
				hcommentSpace.setText(mIndicator.substring(0,
						h_item.deep < 20 ? h_item.deep : 20));

				// set indicator color
				TextView hindicator = (TextView) convertView
						.findViewById(R.id.comment_indicator);
				int hapha = 255 - 20 * h_item.deep;
				if (hapha > 255) {
					hapha = 255;
				}
				if (hapha <= 60) {
					apha = 60;
				}

				ColorDrawable hcolordw = new ColorDrawable(mIndicatorColor);
				hcolordw.setAlpha(hapha);
				hindicator.setBackgroundDrawable(hcolordw);

				TextView hauthor = (TextView) convertView
						.findViewById(R.id.comment_author);
				hauthor.setText(h_item.redditComment.author);
				if (h_item.redditComment.author != null
						&& h_item.redditComment.author
								.equals(mSubRedditItem.author)) {
					hauthor.setBackgroundDrawable(OverviewCommentFragment.this
							.getActivity().getResources()
							.getDrawable(R.drawable.bg_author_name));
				} else if (h_item.redditComment.author != null
						&& mLoginUserName != null
						&& h_item.redditComment.author.equals(mLoginUserName)) {
					hauthor.setBackgroundDrawable(OverviewCommentFragment.this
							.getActivity().getResources()
							.getDrawable(R.drawable.bg_me_name));

				} else {
					hauthor.setBackgroundDrawable(OverviewCommentFragment.this
							.getActivity().getResources()
							.getDrawable(R.drawable.bg_name));
				}

				TextView child = (TextView) convertView
						.findViewById(R.id.comment_children);
				child.setText("(" + h_item.redditComment.getChildSize()
						+ " children)");
				break;
			}
			return convertView;
		}
	}

	public void setDropItem(CommentViewHolder viewHolder, int p) {
		CommentIndex commentItemIndex = (CommentIndex) mCommentAdapter
				.getItem(p);
		viewHolder.mComment.setOnClickListener(this);
		viewHolder.mVoteUp.setOnClickListener(this);
		viewHolder.mVoteDown.setOnClickListener(this);
		viewHolder.mDeleteComment.setOnClickListener(this);
		viewHolder.mEditComment.setOnClickListener(this);
		viewHolder.mProfile.setOnClickListener(this);

		viewHolder.mComment.setTag(p);
		viewHolder.mVoteUp.setTag(p);
		viewHolder.mVoteDown.setTag(p);
		viewHolder.mDeleteComment.setTag(p);
		viewHolder.mEditComment.setTag(p);
		viewHolder.mProfile.setTag(p);

		if (mHasUserLoagin) {
			viewHolder.mComment.setVisibility(View.VISIBLE);
			viewHolder.mVoteUp.setVisibility(View.VISIBLE);
			viewHolder.mVoteDown.setVisibility(View.VISIBLE);
			viewHolder.mDeleteComment.setVisibility(View.GONE);
			viewHolder.mEditComment.setVisibility(View.GONE);

			if (mLoginUserName != null && !"".equals(mLoginUserName)) {

				viewHolder.mDeleteComment
						.setVisibility(commentItemIndex.redditComment.author
								.equalsIgnoreCase(mLoginUserName) ? View.VISIBLE
								: View.GONE);

				viewHolder.mEditComment
						.setVisibility(commentItemIndex.redditComment.author
								.equalsIgnoreCase(mLoginUserName) ? View.VISIBLE
								: View.GONE);

			}
			if (commentItemIndex.redditComment.likes == null) {
				viewHolder.mVoteUpIcon
						.setImageResource(R.drawable.vote_up_grey);
				viewHolder.mVoteDownIcon
						.setImageResource(R.drawable.vote_down_grey);
			} else {
				viewHolder.mVoteUpIcon
						.setImageResource(commentItemIndex.redditComment.likes ? R.drawable.vote_up_selected
								: R.drawable.vote_up_grey);
				viewHolder.mVoteDownIcon
						.setImageResource(commentItemIndex.redditComment.likes ? R.drawable.vote_down_grey
								: R.drawable.vote_down_selected);
			}

		} else {
			viewHolder.mDeleteComment.setVisibility(View.GONE);
			viewHolder.mEditComment.setVisibility(View.GONE);
			viewHolder.mComment.setVisibility(View.GONE);
			viewHolder.mVoteUp.setVisibility(View.GONE);
			viewHolder.mVoteDown.setVisibility(View.GONE);
		}

	}

	public static class CommentViewHolder {

		TextView comment;
		// set the space
		TextView comment_space;
		// set indicator color
		TextView comment_indicator;

		TextView comment_author;

		TextView comment_time;

		ImageView comment_vote;

		TextView comment_score;

		// hidden
		LinearLayout hiddenMenu;
		LinearLayout mComment;
		LinearLayout mVoteUp;
		LinearLayout mVoteDown;
		ImageView mVoteUpIcon;
		ImageView mVoteDownIcon;
		LinearLayout mProfile;
		LinearLayout mDeleteComment;
		LinearLayout mEditComment;
	}

	private void setUpContentView(View convertView, SubRedditItem item,
			int position) {

		TextView subreddit_info_time = (TextView) convertView
				.findViewById(R.id.comment_info_date);

		subreddit_info_time.setText(CommonUtil.getRelateTimeString(
				item.created_utc * 1000,
				OverviewCommentFragment.this.getActivity()));

		TextView cis = (TextView) convertView
				.findViewById(R.id.comment_info_subreddit);
		cis.setText(item.subreddit);

		// info author

		TextView subreddit_info_author = (TextView) convertView
				.findViewById(R.id.comment_info_author);
		subreddit_info_author.setText("by " + item.author);

		TextView subreddit_info_nsfw = (TextView) convertView
				.findViewById(R.id.comment_info_nsfw);
		if (item.over_18) {
			subreddit_info_nsfw.setVisibility(View.VISIBLE);
		} else {
			subreddit_info_nsfw.setVisibility(View.GONE);
		}

		// info saved
		TextView savedTxt = (TextView) convertView
				.findViewById(R.id.comment_info_saved);
		if (item.saved) {
			savedTxt.setVisibility(View.VISIBLE);
			savedTxt.setText("saved");
		} else {
			savedTxt.setVisibility(View.GONE);
		}

		// info saved
		TextView hideTxt = (TextView) convertView
				.findViewById(R.id.comment_info_hide);
		if (item.hidden) {
			hideTxt.setVisibility(View.VISIBLE);
			hideTxt.setText("hidden");
		} else {
			hideTxt.setVisibility(View.GONE);
		}

		// comments counts

		TextView info_count = (TextView) convertView
				.findViewById(R.id.comment_info_comments_count);
		info_count.setText(item.num_comments + " "
				+ this.getString(R.string.label_comments));

		// vote
		LinearLayout voteUpWraper = (LinearLayout) convertView
				.findViewById(R.id.vote_up_wraper);
		LinearLayout voteDownWraper = (LinearLayout) convertView
				.findViewById(R.id.vote_down_wraper);

		ImageView voteUp = (ImageView) convertView
				.findViewById(R.id.vote_up_image);

		if (voteUp.getTag() == null) {
			voteUp.setTag(voteUp.getDrawable());
		}

		ImageView voteDown = (ImageView) convertView
				.findViewById(R.id.vote_down_image);

		if (voteDown.getTag() == null) {
			voteDown.setTag(voteDown.getDrawable());
		}

		TextView score = (TextView) convertView.findViewById(R.id.vote_score);
		if (score.getTag() == null) {
			score.setTag(score.getCurrentTextColor());
		}
		updateVoteImageAndText(voteUp, voteDown, score, item);

		voteUpWraper.setTag(position);
		voteDownWraper.setTag(position);

		voteUpWraper
				.setOnClickListener((OnClickListener) OverviewCommentFragment.this
						.getActivity());
		voteDownWraper
				.setOnClickListener((OnClickListener) OverviewCommentFragment.this
						.getActivity());
	}

	private void updateVoteImageAndText(ImageView voteUp, ImageView voteDown,
			TextView score, SubRedditItem item) {
		if (item.likes == null) {

			voteUp.setImageDrawable((Drawable) voteUp.getTag());
			// voteUp.setImageResource(R.drawable.vote_up_grey);
			voteDown.setImageDrawable((Drawable) voteDown.getTag());
			// voteDown.setImageResource(R.drawable.vote_down_grey);
			// score.setTextColor(getResources().getColor(
			// R.color.vote_no_color));
			score.setTextColor((Integer) score.getTag());
		} else if (item.likes) {
			voteUp.setImageResource(R.drawable.vote_up_selected);
			// voteDown.setImageResource(R.drawable.vote_down_grey);
			voteDown.setImageDrawable((Drawable) voteDown.getTag());
			score.setTextColor(getResources().getColor(R.color.vote_up_color));
		}

		else if (!item.likes) {
			// voteUp.setImageResource(R.drawable.vote_up_grey);
			voteUp.setImageDrawable((Drawable) voteUp.getTag());
			voteDown.setImageResource(R.drawable.vote_down_selected);
			score.setTextColor(getResources().getColor(R.color.vote_down_color));
		}
		score.setText(item.score + "");
	}

	public static class CommentTask extends AsyncTask<Void, Void, String> {
		JSONArray infoArray;
		WeakReference<OverviewCommentFragment> fragmentWF;
		String commentUrl;

		public CommentTask(OverviewCommentFragment fragment, String cl) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			commentUrl = cl;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF.get() != null) {
				infoArray = new JSONArray();
				fragmentWF.get().mCommentModel.clearList();
				fragmentWF.get().mCommentAdapter.mIsLoading = true;
				fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			} else {
				this.cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return Common.RESULT_TASK_CANCLE;
			}

			try {

				String result = RedditCommentManager
						.getRedditComment(
								fragmentWF.get().getActivity(),
								commentUrl,
								fragmentWF.get().mLoadType,
								Common.COMMENT_SORT_VALUE_ARRAY[fragmentWF
										.get().mSortIndex],
								(fragmentWF.get().mCommentCount + 1) * 100 + "",
								infoArray);

				if (isCancelled()) {
					// cancel, so no need to waste time caculate
					return Common.RESULT_TASK_CANCLE;
				}
				if (Common.RESULT_SUCCESS.equals(result)) {
					fragmentWF.get().mCommentModel.setCommentList(infoArray);
					fragmentWF.get().mCommentIndexModel.mSubRedditItem = fragmentWF
							.get().mCommentModel.mSubRedditItem;
					fragmentWF.get().mCommentIndexModel
							.convertToCommentIndex(fragmentWF.get().mCommentModel);
					fragmentWF.get().mSubRedditItem = fragmentWF.get().mCommentModel.mSubRedditItem;
					fragmentWF.get().initCommentCount(
							fragmentWF.get().mSubRedditItem.num_comments);
				}
				return result;
			} catch (Exception e) {
				return Common.RESULT_FETCHING_FAIL;
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (this.isCancelled() || fragmentWF == null
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return;
			}

			if (fragmentWF.get().mCommentTaskWF != null
					&& this != fragmentWF.get().mCommentTaskWF.get()) {
				// not the newest
				return;
			}

			fragmentWF.get().mCommentAdapter.mIsLoading = false;

			if (Common.RESULT_FETCHING_FAIL.equals(result)
					|| Common.RESULT_UNKNOW.equals(result)
					|| Common.RESULT_PAGE_NOTFOUND.equals(result)) {
				fragmentWF.get().mCommentAdapter.mLoadFail = true;
				Toast.makeText(fragmentWF.get().getActivity(),
						"Load fail,try again latter!", Toast.LENGTH_LONG)
						.show();
			}

			fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			// update option items
			if (fragmentWF.get().mSubRedditItem != null) {
				((SherlockFragmentActivity) (fragmentWF.get().getActivity()))
						.invalidateOptionsMenu();
			}

		}

	}

	// ---------------------- SAVE --------------------
	public static class SaveTask extends AsyncTask<Void, Void, String> {
		String id;
		boolean save;
		WeakReference<OverviewCommentFragment> fragmentWF;

		public SaveTask(OverviewCommentFragment fragment, String i, boolean s) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			id = i;
			save = s;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF.get() != null) {
				// show the saved
				fragmentWF.get().mCommentModel.mSubRedditItem.saved = save;
				fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			} else {
				this.cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return Common.RESULT_TASK_CANCLE;
			}
			return SubRedditManager.saveSubRedditPost(id, save, fragmentWF
					.get().getActivity());
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null
					|| result.equals(Common.RESULT_TASK_CANCLE)) {
				return;
			}
			if (Common.RESULT_SUCCESS.equals(result)) {
				Toast.makeText(fragmentWF.get().getActivity(),
						save ? "Save succeeded!" : "Unsave succeeded!",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(fragmentWF.get().getActivity(),
						"Save failed, try again latter!", Toast.LENGTH_SHORT)
						.show();

				fragmentWF.get().mSubRedditItem.saved = !save;
				fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			}
		}

	}

	// ------------ hide --------------
	public static class HideTask extends AsyncTask<Void, Void, String> {
		String name;
		boolean hide;
		WeakReference<OverviewCommentFragment> fragmentWF;

		public HideTask(OverviewCommentFragment fragment, String n, boolean h) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			name = n;
			hide = h;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (fragmentWF != null && fragmentWF.get() != null) {
				fragmentWF.get().mCommentModel.mSubRedditItem.hidden = hide;
				fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			} else {
				this.cancel(true);
			}
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return Common.RESULT_TASK_CANCLE;
			}

			return SubRedditManager.hideSubReddit(name, hide, fragmentWF.get()
					.getActivity());
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (result.equals(Common.RESULT_TASK_CANCLE) || fragmentWF == null
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(fragmentWF.get().getActivity(),
						hide ? "Hide succeeded!" : "Unhiden succeeded!",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(fragmentWF.get().getActivity(), "Hide failed!",
						Toast.LENGTH_LONG).show();
				fragmentWF.get().mCommentModel.mSubRedditItem.hidden = !hide;
				fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			}
		}
	}

	public static class PostCommentTask extends AsyncTask<Void, Void, String> {
		String thing_id;
		String text;
		JSONObject infoJSON;
		int position;
		WeakReference<Comment> postCommentWF;
		WeakReference<Comment> parentCommentWF;
		WeakReference<OverviewCommentFragment> fragmentWF;

		int addPositon;

		public PostCommentTask(OverviewCommentFragment fragment, String td,
				String tt, int p) {

			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			thing_id = td;
			text = tt;
			position = p;
			// create a comment item
			Comment postComment = new Comment();
			postComment.kind = "t1";
			postComment.author = RedditManager.getUserName(fragment
					.getActivity());
			postComment.likes = true;
			postComment.ups = 1;
			postComment.downs = 0;
			postComment.created_utc = System.currentTimeMillis() / 1000;
			postComment.body = text;
			postComment.repliesList = null;
			postComment.bodyMarkProcess = Comment.processMark(Html
					.fromHtml(CommonUtil.getRedditETMarkDown().markdownToHtml(
							text)));
			if (postComment.bodyMarkProcess == null) {
				postComment.bodyMarkProcess = Html.fromHtml(
						CommonUtil.getRedditETMarkDown().markdownToHtml(text))
						.toString();
			}

			postCommentWF = new WeakReference<Comment>(postComment);
			// add to the list
			if (position == 0) {
				fragment.mCommentModel.addComment(0, postComment);
				// new convert and update the view;
				fragment.mCommentIndexModel
						.convertToCommentIndex(fragment.mCommentModel);
				fragment.mCommentAdapter.notifyDataSetChanged();
				fragment.getListView().smoothScrollToPosition(0);
			} else {
				Comment pIndexComment = ((CommentIndex) fragment.mCommentIndexModel
						.getItem(position)).redditComment;
				if (pIndexComment.repliesList == null)
					pIndexComment.repliesList = new ArrayList<Comment>();
				pIndexComment.repliesList.add(postComment);
				addPositon = position + pIndexComment.getChildSize();
				parentCommentWF = new WeakReference<Comment>(pIndexComment);
			}
			// new convert and update the view;
			fragment.mCommentIndexModel
					.convertToCommentIndex(fragment.mCommentModel);
			fragment.mCommentAdapter.notifyDataSetChanged();
			fragment.getListView().smoothScrollToPosition(addPositon);
			// mListView.setSelection(pp);
			// wait to get id;
			infoJSON = new JSONObject();
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF == null
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return Common.RESULT_TASK_CANCLE;
			}
			return RedditCommentManager.postComment(fragmentWF.get()
					.getActivity(), text, thing_id, infoJSON);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result.equals(Common.RESULT_TASK_CANCLE)
					|| this.isCancelled()
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null
					|| (postCommentWF != null && postCommentWF.get() == null)
					|| (parentCommentWF != null && parentCommentWF.get() == null)) {
				return;
			}
			if (result == Common.RESULT_TOO_LATE) {
				Toast.makeText(fragmentWF.get().getActivity(),
						"Too late to reply", Toast.LENGTH_LONG).show();

			} else if (result == Common.RESULT_TRY_TOO_MUCH) {
				Toast.makeText(fragmentWF.get().getActivity(),
						"Try too much, try again latter!", Toast.LENGTH_LONG)
						.show();
			} else if (result == Common.RESULT_SUCCESS) {
				// decode the result;
				JSONArray resultArray = infoJSON.optJSONArray("jquery");

				if (resultArray != null) {
					for (int i = 0; i < resultArray.length(); i++) {
						try {
							JSONObject dataObject = resultArray.getJSONArray(i)
									.getJSONArray(3).getJSONArray(0)
									.getJSONObject(0);
							JSONObject data = dataObject.getJSONObject("data");
							postCommentWF.get().name = data.optString("id");
							Toast.makeText(fragmentWF.get().getActivity(),
									"Comment post succeeded!",
									Toast.LENGTH_LONG).show();
							return;
						} catch (Exception e) {
						}
					}

				}
				Toast.makeText(fragmentWF.get().getActivity(),
						"Comment post failed!", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(fragmentWF.get().getActivity(),
						"Comment post failed!", Toast.LENGTH_LONG).show();

				// update the date if fail
				if (position == 0) {
					fragmentWF.get().mCommentModel.removeComment(0);
				} else {
					parentCommentWF.get().repliesList.remove(postCommentWF
							.get());
				}
				fragmentWF.get().mCommentIndexModel
						.convertToCommentIndex(fragmentWF.get().mCommentModel);
				fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			}
		}

	}

	// vote
	public static class VoteTask extends AsyncTask<Void, Void, String> {
		Comment comment;
		SubRedditItem subRedditItem;
		String dir;
		String name;
		WeakReference<OverviewCommentFragment> fragmentWF;
		WeakReference<Comment> commentWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public VoteTask(OverviewCommentFragment fragment, Comment c, String d) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			dir = d;
			comment = c;
			name = comment.name;
		}

		public VoteTask(OverviewCommentFragment fragment, SubRedditItem s,
				String d) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			dir = d;
			subRedditItem = s;
			name = subRedditItem.name;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return Common.RESULT_TASK_CANCLE;
			}
			return SubRedditManager.voteSubRedditPost(name, dir, fragmentWF
					.get().getActivity());
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (Common.RESULT_TASK_CANCLE.equals(result)
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(fragmentWF.get().getActivity(), "Vote succeed!",
						Toast.LENGTH_SHORT).show();
			}// not successful
			else {
				Toast.makeText(fragmentWF.get().getActivity(), "Vote failed!",
						Toast.LENGTH_SHORT).show();
				if (commentWF != null && commentWF.get() != null) {
					commentWF.get().likes = commentWF.get().old_likes;
				}
				if (subRedditItemWF != null && subRedditItemWF.get() != null) {
					subRedditItemWF.get().likes = subRedditItemWF.get().old_like;
				}
				fragmentWF.get().mCommentAdapter.notifyDataSetChanged();
			}
		}
	}

	public void updateCommentVote(Comment item, boolean isUp) {
		if (isUp) {
			if (item.likes == null) {
				item.old_likes = Boolean.TRUE;
				item.likes = true;
				item.ups += 1;
			} else if (item.likes) {
				// cancel the like
				item.likes = null;
				item.ups -= 1;
			} else if (!item.likes) {
				// change to like
				item.likes = true;
				item.ups += 1;
				item.downs -= 1;
			}
		}

		if (!isUp) {
			if (item.likes == null) {
				item.likes = false;
				item.downs += 1;
			} else if (item.likes) {
				item.likes = false;
				item.ups -= 1;
				item.downs += 1;
			} else if (!item.likes) {
				item.likes = null;
				item.downs -= 1;
			}

		}

		String dir = "0";
		if (item.likes == null) {
			dir = "0";
		} else if (item.likes) {
			dir = "1";
		} else if (!item.likes) {
			dir = "-1";
		}
		mCommentAdapter.notifyDataSetChanged();
		new VoteTask(this, item, dir).execute();
	}

	public void updateSubRedditVote(SubRedditItem item, boolean isUp) {
		item.old_like = item.likes;
		int s = 0;
		if (isUp) {
			if (item.likes == null) {
				item.likes = true;
				s = 1;
			} else if (item.likes) {
				// cancel the like
				item.likes = null;
				s = -1;
			} else if (!item.likes) {
				// change to like
				item.likes = true;
				s = 2;
			}
		}

		if (!isUp) {
			if (item.likes == null) {
				item.likes = false;
				s = -1;
			} else if (item.likes) {
				item.likes = false;
				s = -2;
			} else if (!item.likes) {
				item.likes = null;
				s = 1;
			}

		}

		String dir = "0";
		if (item.likes == null) {
			dir = "0";
		} else if (item.likes) {
			dir = "1";
		} else if (!item.likes) {
			dir = "-1";
		}

		item.score = item.score + s;
		// parent.
		mCommentAdapter.notifyDataSetChanged();
		new VoteTask(this, item, dir).execute();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			final int position, long id) {
		switch (mCommentAdapter.getItemViewType(position)) {
		case TYPE_COMMENT:
			CommentIndex r = (CommentIndex) mCommentAdapter.getItem(position);
			r.redditComment.show = false;
			updateIndexList();
			break;
		case TYPE_HIDE:
			CommentIndex rc = (CommentIndex) mCommentAdapter.getItem(position);
			rc.redditComment.show = true;
			updateIndexList();
			break;
		}
		return true;
	}

	public void deleteComment(CommentIndex commentItemIndex, int position) {

		// deleteComment
		if (commentItemIndex.redditComment.repliesList != null
				&& !commentItemIndex.redditComment.repliesList.isEmpty()) {
			// perform delete by change comment to delete
			commentItemIndex.redditComment.old_body = commentItemIndex.redditComment.body;
			commentItemIndex.redditComment.old_author = commentItemIndex.redditComment.author;
			commentItemIndex.redditComment.body = Common.LABEL_DELETE;
			commentItemIndex.redditComment.author = Common.LABEL_DELETE;
			commentItemIndex.redditComment.bodyMarkProcess = Common.LABEL_DELETE;
		} else {
			mCommentIndexModel.deleteOverviewItem(position);
			if (mSubRedditItem.num_comments < 0) {
				mSubRedditItem.num_comments = 0;
			}
		}
		mSubRedditItem.num_comments--;
		mCommentAdapter.notifyDataSetChanged();
		new DeleteCommentTask(this, commentItemIndex,
				commentItemIndex.redditComment.name, position).execute();
	}

	public void unDeleteComment(CommentIndex commentItemIndex, int position) {
		if (commentItemIndex.redditComment.repliesList != null
				&& !commentItemIndex.redditComment.repliesList.isEmpty()) {
			commentItemIndex.redditComment.body = commentItemIndex.redditComment.old_body;
			commentItemIndex.redditComment.author = commentItemIndex.redditComment.old_author;
			commentItemIndex.redditComment.bodyMarkProcess = Comment
					.processMark(Html
							.fromHtml(CommonUtil
									.getRedditETMarkDown()
									.markdownToHtml(
											commentItemIndex.redditComment.body)));
			if (commentItemIndex.redditComment.bodyMarkProcess == null) {
				commentItemIndex.redditComment.bodyMarkProcess = (Html
						.fromHtml(CommonUtil.getRedditETMarkDown()
								.markdownToHtml(
										commentItemIndex.redditComment.body)))
						.toString();
			}
		} else {
			mCommentIndexModel.unDeleteOverviewItem(commentItemIndex, position);

		}
		mSubRedditItem.num_comments++;

		mCommentAdapter.notifyDataSetChanged();
	}

	public void editComment(int position, String text) {

		if (mCommentAdapter.getItemViewType(position) == TYPE_COMMENT) {
			CommentIndex commentIndex = (CommentIndex) mCommentAdapter
					.getItem(position);
			commentIndex.redditComment.old_body = commentIndex.redditComment.body;
			commentIndex.redditComment.body = text;
			commentIndex.redditComment.bodyMarkProcess = Comment
					.processMark(Html.fromHtml(CommonUtil.getRedditETMarkDown()
							.markdownToHtml(commentIndex.redditComment.body)));
			if (commentIndex.redditComment.bodyMarkProcess == null) {
				commentIndex.redditComment.bodyMarkProcess = (Html
						.fromHtml(CommonUtil
								.getRedditETMarkDown()
								.markdownToHtml(commentIndex.redditComment.body)))
						.toString();
			}

			mCommentAdapter.notifyDataSetChanged();
			// EditCommentTask
			new EditCommentTask(this, commentIndex, text,
					commentIndex.redditComment.name).execute();
		} else {
			// do nothing
			return;
		}
	}

	public void unEditComment(CommentIndex commentItemIndex) {
		commentItemIndex.redditComment.body = commentItemIndex.redditComment.old_body;
		commentItemIndex.redditComment.bodyMarkProcess = commentItemIndex.redditComment.bodyMarkProcess = Comment
				.processMark(Html.fromHtml(CommonUtil.getRedditETMarkDown()
						.markdownToHtml(commentItemIndex.redditComment.body)));
		if (commentItemIndex.redditComment.bodyMarkProcess == null) {
			commentItemIndex.redditComment.bodyMarkProcess = (Html
					.fromHtml(CommonUtil.getRedditETMarkDown().markdownToHtml(
							commentItemIndex.redditComment.body))).toString();
		}

		mCommentAdapter.notifyDataSetChanged();
	}

	// Delete Comment
	public static class EditCommentTask extends AsyncTask<Void, Void, String> {
		String text;
		String thing_id;
		WeakReference<OverviewCommentFragment> fragmentWF;
		WeakReference<CommentIndex> commentIndexWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public EditCommentTask(OverviewCommentFragment fragment,
				CommentIndex c, String textS, String thingId) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			commentIndexWF = new WeakReference<CommentIndex>(c);
			text = textS;
			thing_id = thingId;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return Common.RESULT_TASK_CANCLE;
			}
			return RedditCommentManager.editRedditComment(fragmentWF.get()
					.getActivity(), thing_id, text);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (Common.RESULT_TASK_CANCLE.equals(result)
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(fragmentWF.get().getActivity(), "Edit succeed!",
						Toast.LENGTH_SHORT).show();
			}// not successful
			else {
				Toast.makeText(fragmentWF.get().getActivity(), "Edit failed!",
						Toast.LENGTH_SHORT).show();
				if (commentIndexWF != null && commentIndexWF.get() != null) {
					fragmentWF.get().unEditComment(commentIndexWF.get());
				}
			}
		}
	}

	// Delete Comment
	public static class DeleteCommentTask extends AsyncTask<Void, Void, String> {
		String thing_id;
		int position;
		WeakReference<OverviewCommentFragment> fragmentWF;
		WeakReference<CommentIndex> commentIndexWF;
		WeakReference<SubRedditItem> subRedditItemWF;

		public DeleteCommentTask(OverviewCommentFragment fragment,
				CommentIndex c, String id, int p) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			commentIndexWF = new WeakReference<CommentIndex>(c);
			thing_id = id;
			position = p;
		}

		public DeleteCommentTask(OverviewCommentFragment fragment,
				SubRedditItem s, String id, int p) {
			fragmentWF = new WeakReference<OverviewCommentFragment>(fragment);
			subRedditItemWF = new WeakReference<SubRedditItem>(s);
			thing_id = id;
			position = p;
		}

		@Override
		protected String doInBackground(Void... params) {
			if (this.isCancelled() || fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return Common.RESULT_TASK_CANCLE;
			}
			return RedditCommentManager.deleteRedditComment(fragmentWF.get()
					.getActivity(), thing_id, "");
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (Common.RESULT_TASK_CANCLE.equals(result)
					|| fragmentWF.get() == null
					|| fragmentWF.get().getActivity() == null) {
				return;
			}

			if (result == Common.RESULT_SUCCESS) {
				Toast.makeText(fragmentWF.get().getActivity(),
						"Delete succeed!", Toast.LENGTH_SHORT).show();
			}// not successful
			else {
				Toast.makeText(fragmentWF.get().getActivity(),
						"Delete failed!", Toast.LENGTH_SHORT).show();
				if (commentIndexWF != null && commentIndexWF.get() != null) {
					fragmentWF.get().unDeleteComment(commentIndexWF.get(),
							position);
				}
			}
		}
	}

	@Override
	public void onPostCommentListener(PostCommentDialog dialog, View v,
			int index) {
		switch (v.getId()) {
		case R.id.comment_send:
			String text = dialog.mContentEtx.getEditableText().toString();
			String things_id = dialog.mThing_id;
			new PostCommentTask(this, things_id, text, index).execute();
			dialog.dismiss();
			break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		int adapterId = parent.getId();
		switch (adapterId) {
		case R.id.comments_sort_spinner:
			if (mSortIndex == position) {
				return;
			} else {
				mSortIndex = position;
				updateDataState();
			}
			// update
			break;
		case R.id.comments_count_spinner:
			if (mCommentCount == position) {
				return;
			} else {
				mCommentCount = position;
				updateDataState();
			}
			break;
		}

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// do nothing
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		switch (mCommentAdapter.getItemViewType(position)) {
		case TYPE_LOAD_FAIL:
			// reload
			mCommentAdapter.mLoadFail = false;
			updateDataState();
			break;

		case TYPE_COMMENT:
		case TYPE_CONTEXT_COMMENT:

			CommentIndex cIndex = (CommentIndex) mCommentAdapter
					.getItem(position);

			if (Common.LABEL_DELETE
					.equalsIgnoreCase(cIndex.redditComment.author)
					|| Common.LABEL_DELETE
							.equalsIgnoreCase(cIndex.redditComment.body)) {
				return;
			}

			showCommentHiddenMenu(view);
			break;

		case TYPE_SHOW_ALL_COMMENT:
			if (mSubRedditItem != null) {
				Intent t = new Intent(this.getActivity(),
						CommentFragmentActivity.class);
				t.putExtra(Common.INTENT_EXTRA_SUBREDDIT, mSubRedditItem);
				this.startActivity(t);
				this.getActivity().finish();
			}
			break;
		}

	}

	// POST COMMENT
	@Override
	public void onViewClick(Dialog customDialog, View v, int position) {
		switch (v.getId()) {

		case R.id.comment_send:
			customDialog.dismiss();
			PostCommentDialog cd = (PostCommentDialog) customDialog;
			String text = cd.mContentEtx.getEditableText().toString();
			String things_id = cd.mThing_id;
			new PostCommentTask(this, things_id, text, position).execute();
			break;

		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		if (mPostCommentDialog != null && mPostCommentDialog.isShowing()) {
			mIsPostingComment = true;
			mIsEditComment = mPostCommentDialog.mIsEdit;
			mPostingPosition = mPostCommentDialog.mPosition;
			mInputPosting = mPostCommentDialog.getInputText();
		}
		if (mPostCommentDialog != null) {
			mPostCommentDialog.dismiss();
		}

	}

	@Override
	public void onClick(View v) {
		int position = 0;
		CommentIndex commentItemIndex = null;
		SubRedditItem subRedditItem = null;

		switch (v.getId()) {
		case R.id.comment_post_comment:
		case R.id.comment_delete:
		case R.id.comment_edit:
		case R.id.comment_vote_up:
		case R.id.comment_vote_down:
		case R.id.comment_save:
		case R.id.comment_hide:
		case R.id.comment_profile:
			position = (Integer) v.getTag();
			commentItemIndex = null;
			subRedditItem = null;
			if (position == 0) {
				subRedditItem = (SubRedditItem) mCommentAdapter
						.getItem(position);

			} else {
				commentItemIndex = (CommentIndex) mCommentAdapter
						.getItem(position);

			}

		}

		switch (v.getId()) {
		case R.id.comment_post_comment:
			// subreddit
			if (position == 0 && subRedditItem != null) {
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(this.getActivity());
				mPostCommentDialog.setCustomDialogHandler(
						(CustomDialogHandler) this.getActivity(),
						subRedditItem.name, position);
				mPostCommentDialog.setItem(subRedditItem);
				mPostCommentDialog.show();
			}
			if (position != 0 && commentItemIndex != null) {
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(this.getActivity());
				mPostCommentDialog.setIsEditComment(false);
				mPostCommentDialog.setCustomDialogHandler(
						(CustomDialogHandler) this.getActivity(),
						commentItemIndex.redditComment.name, position);
				mPostCommentDialog.setItem(commentItemIndex.redditComment);
				mPostCommentDialog.show();
			}
			mCommentAdapter.notifyDataSetChanged();
			break;
		case R.id.comment_delete:
			if (position == 0 && subRedditItem != null) {

			}
			if (position != 0 && commentItemIndex != null) {
				this.deleteComment(commentItemIndex, position);
			}
			break;
		case R.id.comment_edit:

			if (position == 0 && subRedditItem != null) {

			}
			if (position != 0 && commentItemIndex != null) {
				if (mPostCommentDialog != null) {
					mPostCommentDialog.dismiss();
				}
				mPostCommentDialog = new PostCommentDialog(this.getActivity());
				mPostCommentDialog.setCustomDialogHandler(
						(CustomDialogHandler) this.getActivity(),
						commentItemIndex.redditComment.name, position);
				mPostCommentDialog.setIsEditComment(true);
				mPostCommentDialog.setItem(commentItemIndex.redditComment);
				mPostCommentDialog.show();
			}

			break;
		case R.id.comment_vote_up:
			if (position == 0 && subRedditItem != null) {
				this.updateSubRedditVote(subRedditItem, true);
			}
			if (position != 0 && commentItemIndex != null) {
				this.updateCommentVote(commentItemIndex.redditComment, true);
			}
			break;
		case R.id.comment_vote_down:
			if (position == 0 && subRedditItem != null) {
				this.updateSubRedditVote(subRedditItem, false);
			}
			if (position != 0 && commentItemIndex != null) {
				this.updateCommentVote(commentItemIndex.redditComment, false);
			}

			break;
		case R.id.comment_save:
			if (position == 0 && subRedditItem != null) {
				new SaveTask(this, subRedditItem.name, !subRedditItem.saved)
						.execute();
			}

			break;
		case R.id.comment_hide:
			if (position == 0 && subRedditItem != null) {
				new HideTask(this, subRedditItem.name, !subRedditItem.hidden)
						.execute();
			}

			break;
		case R.id.comment_profile:
			mCommentAdapter.notifyDataSetChanged();
			String profileName = "redditet";
			if (position == 0 && subRedditItem != null) {
				profileName = subRedditItem.author;
			}
			if (position != 0 && commentItemIndex != null) {
				profileName = commentItemIndex.redditComment.author;
			}

			Intent profileIntent = new Intent(this.getActivity(),
					OverviewFragmentActivity.class);
			profileIntent.putExtra(Common.EXTRA_USERNAME, profileName);
			this.startActivity(profileIntent);
			break;
		case R.id.comment:
			showCommentHiddenMenu((View) v.getTag());
			return;

			/**
			 * CommentIndex cIndex = (CommentIndex) mCommentAdapter
			 * .getItem(position); if (Common.LABEL_DELETE
			 * .equalsIgnoreCase(cIndex.redditComment.author) ||
			 * Common.LABEL_DELETE .equalsIgnoreCase(cIndex.redditComment.body))
			 * { return; }
			 * 
			 * if (mCommentPopDialog != null) { mCommentPopDialog.dismiss(); }
			 * mCommentPopDialog = new CommentPopDialog(this.getActivity()); if
			 * (mLoginUserName != null && !"".equalsIgnoreCase(mLoginUserName))
			 * { mCommentPopDialog.setIsFromUser(mLoginUserName
			 * .equalsIgnoreCase(cIndex.redditComment.author)); } else {
			 * mCommentPopDialog.setIsFromUser(false); }
			 * mCommentPopDialog.setCommentItem(cIndex, position);
			 * mCommentPopDialog.show();
			 * 
			 * break;
			 */
		}

	}

}
