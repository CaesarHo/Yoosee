package com.home.yoosee.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;


import com.home.yoosee.R;
import com.home.yoosee.global.NpcCommon;
import com.home.yoosee.utils.ImageUtils;

import java.io.File;

public class HeaderView extends AppCompatImageView {
	Bitmap tempBitmap;
	private Context mContext;

	public HeaderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TypedArray array = context.obtainStyledAttributes(attrs,R.styleable.);
		this.mContext = context;
	}

	public void updateImage(String threeNum, boolean isGray) {
		updateImage(threeNum, isGray, -1);
	}

	public void updateImage(String threeNum, boolean isGray, int oritation) {
		try {
			tempBitmap = ImageUtils.getBitmap(new File("/sdcard/screenshot/tempHead/" + NpcCommon.mThreeNum + "/" + threeNum + ".jpg"), 200, 200);
			if (oritation != -1) {
				tempBitmap = ImageUtils.roundHalfCorners(mContext, tempBitmap, 5, oritation);
			} else {
				tempBitmap = ImageUtils.roundCorners(tempBitmap, ImageUtils.getScaleRounded(tempBitmap.getWidth()));
			}
			this.setImageBitmap(tempBitmap);
		} catch (Exception e) {
			tempBitmap = BitmapFactory.decodeResource(this.getResources(), R.mipmap.header_icon);
			if (oritation != -1) {
				tempBitmap = ImageUtils.roundHalfCorners(mContext, tempBitmap, 5, oritation);
			} else {
				tempBitmap = ImageUtils.roundCorners(tempBitmap, ImageUtils.getScaleRounded(tempBitmap.getWidth()));
			}
			this.setImageBitmap(tempBitmap);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

}
