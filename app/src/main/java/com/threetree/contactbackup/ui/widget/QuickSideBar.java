package com.afmobi.wakacloud.ui.widget;

/**
 * Created by hj on 2017/5/16.
 */

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.List;

public class QuickSideBar extends View {
    private ListView list;
    private TextView mDialogText;
    private int orientation;
    private Context context;
    private int color_text_focus;
    private int color_text_normal;//提示的字母字体颜色
    private float offY;//距离上边界高度
    private SectionIndexer sectionIndexter;

    private char[] l = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
            'I', 'J', 'K', 'L', 'M', 'N', 'O',
            'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', '#'};


    public void setCharArr(List<String> tempSections) {
        l = new char[tempSections.size() + 1];
        String _str = null;
        for (int i = 0; i < tempSections.size(); i++) {
            _str = tempSections.get(i);
            if (!TextUtils.isEmpty(_str)) {
                l[i] = _str.charAt(0);
            }
        }
        l[tempSections.size()] = '#';
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public QuickSideBar(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public QuickSideBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public QuickSideBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    private void init() {
        Configuration cf = this.getResources().getConfiguration(); //获取设置的配置信息
        orientation = cf.orientation;
        textSize = getContext().getResources().getDimension(com.afmobi.tudcsdk.R.dimen.sideBarTextSize);
        offY = getContext().getResources().getDimension(com.afmobi.tudcsdk.R.dimen.sideBarOffY);
        color_text_focus = getContext().getResources().getColor(com.afmobi.tudcsdk.R.color.color_FFFFFF);
        color_text_normal = getContext().getResources().getColor(com.afmobi.tudcsdk.R.color.color_b3b3b3);
    }


    public void setListView(ListView _list) {
        list = _list;
    }


    public SectionIndexer getSectionIndexter() {
        return sectionIndexter;
    }

    public void setSectionIndexter(SectionIndexer sectionIndexter) {
        this.sectionIndexter = sectionIndexter;
    }

    public void setTextView(TextView mDialogText) {
        this.mDialogText = mDialogText;
    }

    public boolean onTouchEvent(MotionEvent event) {

        super.onTouchEvent(event);
        int i = (int) event.getY();
        int idx = i / (getMeasuredHeight() / l.length);
        if (idx >= l.length) {
            idx = l.length - 1;
        } else if (idx < 0) {
            idx = 0;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN
                || event.getAction() == MotionEvent.ACTION_MOVE) {
            mDialogText.setVisibility(View.VISIBLE);
            mDialogText.setText(String.valueOf(l[idx]));
            mDialogText.setTextSize(34);
            if (sectionIndexter == null) {
                sectionIndexter = (SectionIndexer) list.getAdapter();
            }
            if (selectIndex != idx) {
                selectIndex = idx;
                invalidate();
            }

            int position = sectionIndexter.getPositionForSection(l[idx]);
            if (position == -1) {
                return true;
            }
            list.setSelection(position);
        } else {
            mDialogText.setVisibility(View.INVISIBLE);

        }

        return true;
    }

    Paint paint = new Paint();
    DisplayMetrics dm = new DisplayMetrics();

    public void setNowIndex(int inx) {
        selectIndex = inx;
        invalidate();
    }

    private int selectIndex;
    float textSize;
    private PaintFlagsDrawFilter mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    /**
     * 绘制侧边栏
     *
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;//屏幕分辨率的高
        int width = dm.widthPixels;//屏幕分辨率的宽

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int min = Math.min(width, height);
            int max = Math.max(width, height);
            float s = (float) min / max;
            paint.setTextSize(textSize * s - 2);
        } else {
            paint.setTextSize(textSize);
        }
        float widthCenter = getMeasuredWidth() / 2;

        if (l.length > 0) {
            float p_height = (float) (getMeasuredHeight() - offY) / l.length;
            paint.setColor(0xaab3b3b3);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(widthCenter,
                    selectIndex * p_height + offY, widthCenter, paint);

            paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float fontHeight = fontMetrics.bottom - fontMetrics.top;
            for (int i = 0; i < l.length; i++) {
                if (i == selectIndex) {
                    paint.setColor(color_text_focus);
                } else {
                    paint.setColor(color_text_normal);
                }
                canvas.drawText(String.valueOf(l[i]), widthCenter,
                        (i) * p_height + offY +
                                fontHeight / 2 - fontMetrics.bottom, paint);
            }
        }

        super.onDraw(canvas);
    }

}
