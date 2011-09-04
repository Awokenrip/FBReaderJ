package org.geometerplus.android.fbreader.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.*;

public class ShelfActivity extends Activity implements Library.ChangeListener {
	private static final int WIDTH = 150;
	private static final int HEIGHT = 200;

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

		myAdapter0 = new ShelfAdapter();
		myAdapter1 = new ShelfAdapter();
		myAdapter2 = new ShelfAdapter();

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
					myAdapter0.notifyDataSetChanged();
					myAdapter1.notifyDataSetChanged();
					myAdapter2.notifyDataSetChanged();
				}
			});
		}
	}

	private class ShelfAdapter extends BaseAdapter {
		private final FBTree myTree = myLibrary.getRootTree().subTrees().get(1);

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
			return data != null ? data.getBitmap(2 * WIDTH, 2 * HEIGHT) : null;
		}

		private int myCoverWidth = -1;
		private int myCoverHeight = -1;
		private final Runnable myInvalidateViewsRunnable = new Runnable() {
			public void run() {
				notifyDataSetChanged();
			}
		};

		public int getCount() {
			return myTree.subTrees().size();
		}

		public FBTree getItem(int position) {
			return myTree.subTrees().get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final LibraryTree tree = (LibraryTree)getItem(position);
			//final FrameLayout frame = new FrameLayout(ShelfActivity.this);
			final Bitmap coverBitmap = getCoverBitmap(/*imageView,*/ tree.getCover());
			if (coverBitmap != null) {
				/*
				final ImageView coverView = new ImageView(ShelfActivity.this);
				coverView.setScaleType(ImageView.ScaleType.FIT_XY);
				coverView.setLayoutParams(new Gallery.LayoutParams(WIDTH, HEIGHT));
				coverView.setMinimumWidth(WIDTH);
				coverView.setMinimumHeight(HEIGHT);
				coverView.setImageBitmap(coverBitmap);
				//coverView.requestLayout();
				return coverView;
				*/
				final TextView titleView = new TextView(ShelfActivity.this);
				Drawable background = new BitmapDrawable(coverBitmap);
				//background = new ScaleDrawable(background, Gravity.CENTER, WIDTH / 150f, HEIGHT / 200f);
				//background = new ScaleDrawable(background, Gravity.FILL, WIDTH, HEIGHT);
				titleView.setBackgroundDrawable(background);
				titleView.setTypeface(Typeface.DEFAULT_BOLD);
				titleView.setTextColor(Color.BLACK);
				titleView.setText(tree.getName());
				titleView.setPadding(28, 10, 10, 40);
				titleView.setMinWidth(WIDTH);
				titleView.setMaxWidth(WIDTH);
				titleView.setMinHeight(HEIGHT);
				titleView.setMaxHeight(HEIGHT);
				titleView.setGravity(Gravity.CENTER);
				return titleView;
			} else {
				final TextView titleView = new TextView(ShelfActivity.this);
				Drawable background = ShelfActivity.this.getResources().getDrawable(R.drawable.book_cover);
				//background = new ScaleDrawable(background, Gravity.CENTER, WIDTH / 150f, HEIGHT / 200f);
				//background = new ScaleDrawable(background, Gravity.FILL, WIDTH, HEIGHT);
				titleView.setBackgroundDrawable(background);
				titleView.setTypeface(Typeface.DEFAULT_BOLD);
				titleView.setTextColor(Color.BLACK);
				titleView.setText(tree.getName());
				titleView.setPadding(28, 10, 10, 40);
				titleView.setMinWidth(WIDTH);
				titleView.setMaxWidth(WIDTH);
				titleView.setMinHeight(HEIGHT);
				titleView.setMaxHeight(HEIGHT);
				titleView.setGravity(Gravity.CENTER);
				return titleView;
			}
		}
	}
}
