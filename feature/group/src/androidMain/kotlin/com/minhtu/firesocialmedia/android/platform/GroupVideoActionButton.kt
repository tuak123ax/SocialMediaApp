package com.minhtu.firesocialmedia.group.platform

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

// ── modernActionButton design tokens ────────────────────────────────────────
internal object ActionButtonTokens {
    // Dimensions (dp)
    const val CIRCLE_SIZE_DP          = 52
    const val ICON_PADDING_DP         = 12
    const val STROKE_WIDTH_DP         = 1
    const val BUTTON_BOTTOM_MARGIN_DP = 20
    const val LABEL_TOP_MARGIN_DP     = 6

    // Typography
    const val LABEL_TEXT_SIZE_SP      = 11f

    // Press animation
    const val PRESS_SCALE_DOWN        = 0.85f
    const val PRESS_SCALE_UP          = 1f
    const val PRESS_ANIM_DURATION_MS  = 80L

    // Elevation
    const val CIRCLE_ELEVATION        = 6f

    // Text shadow
    const val SHADOW_RADIUS           = 4f
    const val SHADOW_DX               = 0f
    const val SHADOW_DY               = 1f

    // Colors – default (white frosted-glass)
    const val DEFAULT_ICON_ALPHA      = 255
    const val DEFAULT_BG_ALPHA        = 80
    const val DEFAULT_STROKE_ALPHA    = 60
    const val DEFAULT_COLOR_R         = 255
    const val DEFAULT_COLOR_G         = 255
    const val DEFAULT_COLOR_B         = 255

    // Colors – active (soft pink #FF6987)
    const val ACTIVE_ICON_ALPHA       = 255
    const val ACTIVE_BG_ALPHA         = 60
    const val ACTIVE_STROKE_ALPHA     = 120
    const val ACTIVE_COLOR_R          = 255
    const val ACTIVE_COLOR_G          = 105
    const val ACTIVE_COLOR_B          = 135

    // Shadow color
    const val SHADOW_ALPHA            = 160
    const val SHADOW_COLOR_R          = 0
    const val SHADOW_COLOR_G          = 0
    const val SHADOW_COLOR_B          = 0
}

/**
 * A modern circular-background action button with an icon + label underneath,
 * styled like a TikTok / Reels action button.
 */
internal fun modernActionButton(
    context: Context,
    iconResId: Int,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false
): LinearLayout {
    val t = ActionButtonTokens

    val iconTint = if (isActive)
        android.graphics.Color.argb(t.ACTIVE_ICON_ALPHA, t.ACTIVE_COLOR_R, t.ACTIVE_COLOR_G, t.ACTIVE_COLOR_B)
    else
        android.graphics.Color.argb(t.DEFAULT_ICON_ALPHA, t.DEFAULT_COLOR_R, t.DEFAULT_COLOR_G, t.DEFAULT_COLOR_B)

    val circleBgColor = if (isActive)
        android.graphics.Color.argb(t.ACTIVE_BG_ALPHA, t.ACTIVE_COLOR_R, t.ACTIVE_COLOR_G, t.ACTIVE_COLOR_B)
    else
        android.graphics.Color.argb(t.DEFAULT_BG_ALPHA, t.DEFAULT_COLOR_R, t.DEFAULT_COLOR_G, t.DEFAULT_COLOR_B)

    val circleStrokeColor = if (isActive)
        android.graphics.Color.argb(t.ACTIVE_STROKE_ALPHA, t.ACTIVE_COLOR_R, t.ACTIVE_COLOR_G, t.ACTIVE_COLOR_B)
    else
        android.graphics.Color.argb(t.DEFAULT_STROKE_ALPHA, t.DEFAULT_COLOR_R, t.DEFAULT_COLOR_G, t.DEFAULT_COLOR_B)

    val shadowColor = android.graphics.Color.argb(t.SHADOW_ALPHA, t.SHADOW_COLOR_R, t.SHADOW_COLOR_G, t.SHADOW_COLOR_B)

    return LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = t.BUTTON_BOTTOM_MARGIN_DP.dpToPx(context)
        }

        // Circular frosted-glass background for the icon
        val circle = FrameLayout(context).apply {
            val size = t.CIRCLE_SIZE_DP.dpToPx(context)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(circleBgColor)
                setStroke(t.STROKE_WIDTH_DP.dpToPx(context), circleStrokeColor)
            }
            elevation = t.CIRCLE_ELEVATION

            // Icon
            val icon = android.widget.ImageView(context).apply {
                setImageResource(iconResId)
                colorFilter = android.graphics.PorterDuffColorFilter(iconTint, android.graphics.PorterDuff.Mode.SRC_IN)
                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                val pad = t.ICON_PADDING_DP.dpToPx(context)
                setPadding(pad, pad, pad, pad)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            addView(icon)

            // Ripple-like press feedback
            setOnClickListener {
                animate().scaleX(t.PRESS_SCALE_DOWN).scaleY(t.PRESS_SCALE_DOWN)
                    .setDuration(t.PRESS_ANIM_DURATION_MS)
                    .withEndAction {
                        animate().scaleX(t.PRESS_SCALE_UP).scaleY(t.PRESS_SCALE_UP)
                            .setDuration(t.PRESS_ANIM_DURATION_MS).start()
                        onClick()
                    }.start()
            }
        }

        // Label
        val labelView = TextView(context).apply {
            text = label
            textSize = t.LABEL_TEXT_SIZE_SP
            setTextColor(iconTint)
            gravity = Gravity.CENTER
            setShadowLayer(t.SHADOW_RADIUS, t.SHADOW_DX, t.SHADOW_DY, shadowColor)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = t.LABEL_TOP_MARGIN_DP.dpToPx(context) }
        }

        addView(circle)
        addView(labelView)
    }
}

/**
 * Imperatively update the visual state of a like button (returned by [modernActionButton])
 * without rebuilding the whole view hierarchy.
 *
 * Layout assumed: LinearLayout → [0] FrameLayout (circle) → [0] ImageView (icon)
 *                              → [1] TextView (label)
 */
internal fun updateLikeButtonState(likeButton: LinearLayout, isActive: Boolean, context: Context) {
    val t = ActionButtonTokens

    val iconTint = if (isActive)
        android.graphics.Color.argb(t.ACTIVE_ICON_ALPHA, t.ACTIVE_COLOR_R, t.ACTIVE_COLOR_G, t.ACTIVE_COLOR_B)
    else
        android.graphics.Color.argb(t.DEFAULT_ICON_ALPHA, t.DEFAULT_COLOR_R, t.DEFAULT_COLOR_G, t.DEFAULT_COLOR_B)

    val circleBgColor = if (isActive)
        android.graphics.Color.argb(t.ACTIVE_BG_ALPHA, t.ACTIVE_COLOR_R, t.ACTIVE_COLOR_G, t.ACTIVE_COLOR_B)
    else
        android.graphics.Color.argb(t.DEFAULT_BG_ALPHA, t.DEFAULT_COLOR_R, t.DEFAULT_COLOR_G, t.DEFAULT_COLOR_B)

    val circleStrokeColor = if (isActive)
        android.graphics.Color.argb(t.ACTIVE_STROKE_ALPHA, t.ACTIVE_COLOR_R, t.ACTIVE_COLOR_G, t.ACTIVE_COLOR_B)
    else
        android.graphics.Color.argb(t.DEFAULT_STROKE_ALPHA, t.DEFAULT_COLOR_R, t.DEFAULT_COLOR_G, t.DEFAULT_COLOR_B)

    val shadowColor = android.graphics.Color.argb(
        t.SHADOW_ALPHA, t.SHADOW_COLOR_R, t.SHADOW_COLOR_G, t.SHADOW_COLOR_B
    )

    // circle (FrameLayout) is child 0
    val circle = likeButton.getChildAt(0) as? FrameLayout ?: return
    // icon (ImageView) is child 0 of circle
    val icon = circle.getChildAt(0) as? android.widget.ImageView ?: return
    // label (TextView) is child 1 of the outer LinearLayout
    val label = likeButton.getChildAt(1) as? TextView ?: return

    // Update circle background
    (circle.background as? GradientDrawable)?.apply {
        setColor(circleBgColor)
        setStroke(t.STROKE_WIDTH_DP.dpToPx(context), circleStrokeColor)
    }

    // Update icon tint
    icon.colorFilter = android.graphics.PorterDuffColorFilter(iconTint, android.graphics.PorterDuff.Mode.SRC_IN)

    // Update label color & shadow
    label.setTextColor(iconTint)
    label.setShadowLayer(t.SHADOW_RADIUS, t.SHADOW_DX, t.SHADOW_DY, shadowColor)
}

internal fun Int.dpToPx(context: Context): Int {
    return (this * context.resources.displayMetrics.density).toInt()
}
