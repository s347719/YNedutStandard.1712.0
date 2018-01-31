package com.yineng.ynmessager.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yineng.ynmessager.util.BubbleUtils;
import com.yineng.ynmessager.util.GeometryUtil;

public class BubbleView extends View {

	// 1、消除气泡触摸的误差---定义状态栏的高度
	private int statusBarHeight;

	// 2、画两个圆之间的连线---定义消息气泡是否消失
	private boolean isDisappear;

	// 2、画两个圆之间的连线---定义消息气泡是否超出了临界范围
	private boolean isOutOfRange;

	// 2.1.1 计算两个圆心之间的距离---定义拖拽圆心坐标
	private PointF dragCenter;

	// 2.1.1 计算两个圆心之间的距离---定义固定圆心坐标
	private PointF stickCenter;

	// 2.1.2.1 控制两点之间的距离的范围---定义临界值
	private float farest;

	// 2.1.2.3 根据百分比和固定的圆的半径的范围---定义固定圆的半径的范围(最大)
	private float stickCircleRadius;
	private float stickCircleTemRadius;
	// 2.1.2.3 根据百分比和固定的圆的半径的范围---定义固定圆的半径的范围（最小）
	private float stickCircleMinRadius;

	// 2.1.4 求出四个坐标？---定义拖拽圆的半径
	private float dragCircleRadius = 0;

	// 2、画两个圆之间的连线---定义气泡的大小
	private Rect bubbleSize;

	// 3、绘制固定的圆---定义圆的画笔
	private Paint paintRed;

	// 5、绘制消息文本
	private String bubbleText;

	// 5、绘制消息文本---定义文本画笔
	private Paint paintText;

	public BubbleView(Context arg0, AttributeSet arg1) {
		super(arg0, arg1);
		initParams();
	}

	public BubbleView(Context arg0) {
		super(arg0);
		initParams();
	}

	public void setBubbleText(String bubbleText) {
		this.bubbleText = bubbleText;
	}

	private void initParams() {
		// 1、消除气泡触摸的误差---初始化状态栏的高度
		this.statusBarHeight = BubbleUtils.getStatusBarHeight(getContext());
		// 2.1.2.1 控制两点之间的距离的范围---初始化临界值
		this.farest = BubbleUtils.dipToDimension(80, getContext());
		// 2.1.2.3 根据百分比和固定的圆的半径的范围---初始化固定圆的半径的范围
		this.stickCircleRadius = BubbleUtils.dipToDimension(10, getContext());
		// 2.1.2.3 根据百分比和固定的圆的半径的范围---定义固定圆的半径的范围（最小）
		this.stickCircleMinRadius = BubbleUtils.dipToDimension(3, getContext());
		// 2.1.4 求出四个坐标？---初始化拖拽圆的半径
		this.dragCircleRadius = BubbleUtils.dipToDimension(10, getContext());

		// 2、画两个圆之间的连线---定义气泡的大小---可以自定义
		this.bubbleSize = new Rect(0, 0, 50, 50);

		// 3、绘制固定的圆---定义圆的画笔－－－初始化
		this.paintRed = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.paintRed.setColor(Color.RED);

		// 5、绘制消息文本---定义文本画笔
		this.paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		this.paintText.setTextAlign(Align.CENTER);
		this.paintText.setColor(Color.WHITE);
		this.paintText.setTextSize(dragCircleRadius * 1.2f);

	}

	// 2.1.1 计算两个圆心之间的距离---初始化圆心坐标
	public void setCenterPoint(float x, float y) {
		this.dragCenter = new PointF(x, y);
		this.stickCenter = new PointF(x, y);
		invalidate();
	}

	// 1.1 绘制气泡
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// 我们要干五件事情
		// 1、消除气泡触摸的误差
		canvas.translate(0, -statusBarHeight);

		if (!this.isDisappear) {

			if (!this.isOutOfRange) {
				// 2、画两个圆之间的连线
				// 图形类----ShapeDrawable－－－默认情况下：矩形
				// 矩形: new ShapeDrawable(new RectShape())
				// 圆形: new ShapeDrawable(new OvalShape())
				// 椭圆等等......
				ShapeDrawable shapeDrawable = drawShapeDrawable();
				shapeDrawable.setBounds(bubbleSize);
				shapeDrawable.draw(canvas);

				// 3、绘制固定的圆
				canvas.drawCircle(stickCenter.x, stickCenter.y,
						stickCircleTemRadius, paintRed);
			}
			// 4、绘制拖拽的圆
			canvas.drawCircle(dragCenter.x, dragCenter.y, dragCircleRadius,
					paintRed);

			// 5、绘制消息文本
			canvas.drawText(bubbleText, dragCenter.x, dragCenter.y
					+ dragCircleRadius / 2f, paintText);
		}

		canvas.restore();

	}

	// 2、画两个圆之间的连线
	private ShapeDrawable drawShapeDrawable() {
		Path path = new Path();

		// 2.1 根据两个圆心距离计算出固定圆的半径
		// 2.1.1 计算两个圆心之间的距离
		float distance = GeometryUtil.getDistanceBetween2Points(dragCenter,
				stickCenter);
		// 2.1.2 计算出固定圆的半径
		stickCircleTemRadius = getCurrentRadius(distance);

		// 2.1.3 已知经过两圆圆心的连线的垂线的正切值。
		// 目的：绘制贝塞尔曲线
		float xDistance = stickCenter.x - dragCenter.x;
		double tan = 0;
		if (xDistance != 0) {
			tan = (stickCenter.y - dragCenter.y) / xDistance;
		}

		// 2.1.4 求出四个坐标？
		// 如何求这四个坐标值？
		// 获取指定圆心和正切值、直线和圆心的交点
		// 有api提供
		PointF[] stickPoint = GeometryUtil.getIntersectionPoints(stickCenter,
				stickCircleTemRadius, tan);
		PointF[] dragPoint = GeometryUtil.getIntersectionPoints(dragCenter,
				stickCircleTemRadius, tan);

		// 2.1.5 绘制贝塞尔曲线
		// 第一步: 获取控制点（0.618－－－黄金分割）
		PointF pointByPercent = GeometryUtil.getPointByPercent(dragCenter,
				stickCenter, 0.618f);
		// 第二步：画第一条贝塞尔曲线
		// 贝塞尔曲线的开始位置
		path.moveTo(stickPoint[0].x, stickPoint[0].y);
		// quadTo:前面两个参数是贝塞尔曲线的控制点坐标（x1, y1）
		// 后面两个参数是贝塞尔曲线的结束位置（x2, y2）
		path.quadTo(pointByPercent.x, pointByPercent.y, dragPoint[0].x,
				dragPoint[0].y);
		// 第三步：画第二条贝塞尔曲线
		// 贝塞尔曲线的开始位置
		path.lineTo(dragPoint[1].x, dragPoint[1].y);
		path.quadTo(pointByPercent.x, pointByPercent.y, stickPoint[1].x,
				stickPoint[1].y);
		path.close();

		// 构建ShapeDrawable
		ShapeDrawable shapeDrawable = new ShapeDrawable(new PathShape(path,
				50f, 50f));
		shapeDrawable.getPaint().setColor(Color.RED);
		return shapeDrawable;
	}

	// 2.1.2 计算出固定圆的半径
	private float getCurrentRadius(float distance) {
		// 2.1.2.1 控制两点之间的距离的范围
		distance = Math.min(distance, farest);

		// 2.1.2.2 根据两点之间的距离动态的计算固定圆的半径的百分比--这个算法是可以改的(0-1)
		// 例如：我现在我的半径是100（20-100）
		float fraction = 0.2f + 0.8f * (distance / farest);

		// 2.1.2.3 根据百分比和固定的圆的半径的范围－－－计算当前固定圆的半径
		float value = GeometryUtil.evaluateValue(fraction,
				this.stickCircleRadius, this.stickCircleMinRadius);
		return value;
	}

	// 1.2 事件处理
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int actionMasked = MotionEventCompat.getActionMasked(event);
		switch (actionMasked) {
		case MotionEvent.ACTION_DOWN:
			isDisappear = false;
			isOutOfRange = false;
			// 1.2.1 当我们触摸的时候，不断的更新触摸中心点
			updateCenterPoint(event.getRawX(), event.getRawY());
			break;
		case MotionEvent.ACTION_MOVE:
			// 1.2.2 处理按下
			// 判断当前拖拽的偏移量是否超过了我的临界点
			PointF pointOne = new PointF(dragCenter.x, dragCenter.y);
			PointF pointTwo = new PointF(stickCenter.x, stickCenter.y);
			if (GeometryUtil.getDistanceBetween2Points(pointOne, pointTwo) > farest) {
				isOutOfRange = true;
				updateCenterPoint(event.getRawX(), event.getRawY());
				return false;
			}
			updateCenterPoint(event.getRawX(), event.getRawY());
			break;
		case MotionEvent.ACTION_UP:
			// 执行气泡爆炸动画
			break;

		default:
			this.isOutOfRange = false;
			break;
		}
		return true;
	}

	// 1.2.1 当我们触摸的时候，不断的更新触摸中心点
	public void updateCenterPoint(float x, float y) {
		this.dragCenter.x = x;
		this.dragCenter.y = y;
		invalidate();
	}

}
