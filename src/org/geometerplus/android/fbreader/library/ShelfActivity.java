package org.geometerplus.android.fbreader.library;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.graphics.Bitmap;

import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.fbreader.library.*;

public class ShelfActivity extends Activity implements Library.ChangeListener {
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
		((Gallery)findViewById(R.id.shelf1)).setAdapter(myAdapter1);
		((Gallery)findViewById(R.id.shelf2)).setAdapter(myAdapter2);
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
			return data != null ? data.getBitmap(2 * myCoverWidth, 2 * myCoverHeight) : null;
		}

		private int myCoverWidth = -1;
		private int myCoverHeight = -1;
		private final Runnable myInvalidateViewsRunnable = new Runnable() {
			public void run() {
				notifyDataSetChanged();
				//getListView().invalidateViews();
			}
		};

		private ImageView getCoverView(View parent) {
			if (myCoverWidth == -1) {
				//parent.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				//myCoverHeight = parent.getMeasuredHeight();
				myCoverHeight = 100;
				myCoverWidth = 80;
				//myCoverWidth = myCoverHeight * 15 / 32;
				//parent.requestLayout();
			}

			final ImageView coverView = new ImageView(ShelfActivity.this);
			//coverView.getLayoutParams().width = myCoverWidth;
			//coverView.getLayoutParams().height = myCoverHeight;
			//coverView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			coverView.setScaleType(ImageView.ScaleType.FIT_XY);
			coverView.setLayoutParams(new Gallery.LayoutParams(110, 140));
			coverView.requestLayout();
			return coverView;
		}

		public int getCount() {
			System.err.println("COUNT = " + myTree.subTrees().size());
			return myTree.subTrees().size();
		}

		public FBTree getItem(int position) {
			System.err.println("requested item at position " + position);
			// TODO: implement
			return myTree.subTrees().get(position);
			//return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			System.err.println("requested view at position " + position);
			// TODO: implement
			final LibraryTree tree = (LibraryTree)getItem(position);
			final ImageView view = getCoverView(parent);
			final Bitmap coverBitmap = getCoverBitmap(tree.getCover());
			if (coverBitmap != null) {
				view.setImageBitmap(coverBitmap);
			} else {
				view.setImageResource(R.drawable.ic_list_library_book);
			}
			/*
			final ImageView view = new ImageView(ShelfActivity.this);
			view.setImageResource(R.drawable.fbreader);
			view.setScaleType(ImageView.ScaleType.FIT_XY);
			view.setLayoutParams(new Gallery.LayoutParams(88, 88));
			*/

			// The preferred Gallery item background
			//view.setBackgroundResource(mGalleryItemBackground);

			return view;
		}
	}
}
