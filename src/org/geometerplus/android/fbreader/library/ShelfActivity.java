package org.geometerplus.android.fbreader.library;

import java.util.*;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.DisplayMetrics;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.*;

public class ShelfActivity extends Activity implements Library.ChangeListener {
	private int myCoverWidth;
	private int myCoverHeight;

	private BooksDatabase myDatabase;
	private Library myLibrary;
	private ShelfAdapter myAdapter0;
	private ShelfAdapter myAdapter1;
	private ShelfAdapter myAdapter2;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		myDatabase = SQLiteBooksDatabase.Instance();
		if (myDatabase == null) {
			myDatabase = new SQLiteBooksDatabase(this, "LIBRARY");
		}
		if (myLibrary == null) {
			myLibrary = new Library();
			myLibrary.addChangeListener(this);
			myLibrary.startBuild();
		}

		final FBTree tree = myLibrary.getRootTree().subTrees().get(1);
		myAdapter0 = new ShelfAdapter(tree.subTrees());
		myAdapter1 = new ShelfAdapter(tree.subTrees());
		myAdapter2 = new ShelfAdapter(tree.subTrees());

		setContentView(R.layout.shelf);
		((Gallery)findViewById(R.id.shelf0)).setAdapter(myAdapter0);
		((Gallery)findViewById(R.id.shelf0)).setPadding(0, 20, 0, 20);
		((Gallery)findViewById(R.id.shelf0)).setSpacing(20);
		((Gallery)findViewById(R.id.shelf1)).setAdapter(myAdapter1);
		((Gallery)findViewById(R.id.shelf1)).setPadding(0, 20, 0, 20);
		((Gallery)findViewById(R.id.shelf1)).setSpacing(20);
		((Gallery)findViewById(R.id.shelf2)).setAdapter(myAdapter2);
		((Gallery)findViewById(R.id.shelf2)).setPadding(0, 20, 0, 20);
		((Gallery)findViewById(R.id.shelf2)).setSpacing(20);
	}

	public void onLibraryChanged(final Code code) {
		System.err.println("onLibraryChanged " + code);
		if (myAdapter0 != null) {
			runOnUiThread(new Runnable() {
				public void run() {
					final FBTree tree = myLibrary.getRootTree().subTrees().get(1);
					myAdapter0.updateList(tree.subTrees());
					myAdapter1.updateList(tree.subTrees());
					myAdapter2.updateList(tree.subTrees());
				}
			});
		}
	}

	private int getCoverWidth() {
		if (myCoverWidth == 0) {
			initMetrics();
		}
		return myCoverWidth;
	}

	private int getCoverHeight() {
		if (myCoverHeight == 0) {
			initMetrics();
		}
		return myCoverHeight;
	}

	private void initMetrics() {
		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int height = (int)metrics.ydpi;
		int widht = (int)metrics.xdpi * 3 / 4;
		final int maxHeight = metrics.heightPixels * 7 / 10;
		if (height > maxHeight) {
			widht = widht * maxHeight / height;
			height = maxHeight;
		}
		myCoverWidth = widht;
		myCoverHeight = height;
	}

	private class ShelfAdapter extends BaseAdapter {
		private final List<FBTree> myTrees;

		ShelfAdapter(List<FBTree> trees) {
			myTrees = new ArrayList<FBTree>(trees);
		}

		void updateList(List<FBTree> trees) {
			if (!myTrees.equals(trees)) {
				myTrees.clear();
				myTrees.addAll(trees);
				notifyDataSetChanged();
			}
		}

		private Bitmap getCoverBitmap(ZLImage cover) {
			if (cover == null) {
				return null;
			}

			ZLAndroidImageData data = null;
			final ZLAndroidImageManager mgr = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
			if (cover instanceof ZLLoadableImage) {
				final ZLLoadableImage img = (ZLLoadableImage)cover;
				if (img.isSynchronized()) {
					data = mgr.getImageData(img);
				} else {
					img.startSynchronization(myInvalidateViewsRunnable);
				}
			} else {
				data = mgr.getImageData(cover);
			}
			return data != null ? data.getBitmap(2 * getCoverWidth(), 2 * getCoverHeight()) : null;
		}

		private final Runnable myInvalidateViewsRunnable = new Runnable() {
			public void run() {
				notifyDataSetChanged();
			}
		};

		public int getCount() {
			return myTrees.size();
		}

		public FBTree getItem(int position) {
			return myTrees.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		private Bitmap myCoverPlaceHolder;

		public View getView(int position, View convertView, ViewGroup parent) {
			final LibraryTree tree = (LibraryTree)getItem(position);
			//final FrameLayout frame = new FrameLayout(ShelfActivity.this);
			final Bitmap coverBitmap = getCoverBitmap(/*imageView,*/ tree.getCover());
			if (coverBitmap != null) {
				final ImageView coverView = new ImageView(ShelfActivity.this);
				coverView.setScaleType(ImageView.ScaleType.FIT_XY);
				coverView.setLayoutParams(new Gallery.LayoutParams(getCoverWidth(), getCoverHeight()));
				coverView.setMinimumWidth(getCoverWidth());
				coverView.setMinimumHeight(getCoverHeight());
				coverView.setImageBitmap(coverBitmap);
				return coverView;
			} else {
				final TextView titleView = new TextView(ShelfActivity.this);
				if (myCoverPlaceHolder == null) {
					myCoverPlaceHolder = BitmapFactory.decodeResource(
						ShelfActivity.this.getResources(),
						R.drawable.book_cover
					);
				}
				if (myCoverPlaceHolder != null) {
					if (myCoverPlaceHolder.getWidth() != getCoverWidth() ||
						myCoverPlaceHolder.getHeight() != getCoverHeight()) {
						myCoverPlaceHolder = Bitmap.createScaledBitmap(
							myCoverPlaceHolder, getCoverWidth(), getCoverHeight(), false
						);
					}
					titleView.setBackgroundDrawable(new BitmapDrawable(myCoverPlaceHolder));
				}
				titleView.setTypeface(Typeface.DEFAULT_BOLD);
				titleView.setTextColor(Color.BLACK);
				titleView.setText(tree.getName());
				titleView.setPadding(
					getCoverWidth() * 12 / 100,
					getCoverHeight() * 4 / 100,
					getCoverWidth() * 10 / 100,
					getCoverHeight() * 15 / 100
				);
				titleView.setMinWidth(getCoverWidth());
				titleView.setMaxWidth(getCoverWidth());
				titleView.setMinHeight(getCoverHeight());
				titleView.setMaxHeight(getCoverHeight());
				titleView.setGravity(Gravity.CENTER);
				return titleView;
			}
		}
	}
}
