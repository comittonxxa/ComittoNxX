package src.comitton.config.seekbar;

import src.comitton.common.DEF;

import android.content.Context;
import android.util.AttributeSet;

public class ItemMarginSeekbar extends SeekBarPreference {

	public ItemMarginSeekbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mDefValue = DEF.DEFAULT_ITEMMARGIN;
		mMaxValue = DEF.MAX_ITEMMARGIN;
		super.setKey(DEF.KEY_ITEMMRGN);
	}
}
