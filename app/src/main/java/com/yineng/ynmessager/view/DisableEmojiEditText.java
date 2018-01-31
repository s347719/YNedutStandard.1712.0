package com.yineng.ynmessager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.Toast;
import com.yineng.ynmessager.R;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by 贺毅柳 on 2016/12/29 14:42.
 */

public class DisableEmojiEditText extends EditText {
    private String mToastText = StringUtils.EMPTY;
    private Toast mToast;

    public DisableEmojiEditText(Context context) {
        super(context);
        init();
    }

    public DisableEmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DisableEmojiEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DisableEmojiEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mToast = Toast.makeText(getContext(), R.string.p2pChatInfo_emojiDisabled, Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.CENTER, 0, 0);

        setFilters(new InputFilter[] { new EmojiExcludeFilter() });
    }

    private class EmojiExcludeFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    mToast.show();
                    return StringUtils.EMPTY;
                }
            }
            return null;
        }
    }
}
