package com.folioreader;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

/**
 * Configuration class for FolioReader.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class Config implements Parcelable {

    private static final String LOG_TAG = Config.class.getSimpleName();
    public static final String INTENT_CONFIG = "config";
    public static final String EXTRA_OVERRIDE_CONFIG = "com.folioreader.extra.OVERRIDE_CONFIG";
    public static final String CONFIG_FONT = "font";
    public static final String CONFIG_FONT_SIZE = "font_size";
    public static final String CONFIG_IS_NIGHT_MODE = "is_night_mode";
    public static final String CONFIG_THEME_COLOR_INT = "theme_color_int";
    public static final String CONFIG_NIGHT_THEME_COLOR_INT = "night_theme_color_int";
    public static final String CONFIG_IS_TTS = "is_tts";
    public static final String CONFIG_ALLOWED_DIRECTION = "allowed_direction";
    public static final String CONFIG_DIRECTION = "horizontal";
    public static final String CONFIG_REMAINING_INDICATOR = "show_remaining_indicator";
    public static final String CONFIG_TEXT_SELECTION = "text_selection";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String BODY_PADDING = "BODY_PADDING";
    public static final String TEXT_SPACE = "TEXT_SPACE";
    public static final String COLUMN_COUNT = "COLUMN_COUNT";
    public static final String ENABLE_HORIZONTAL_COLUMN = "ENABLE_HORIZONTAL_COLUMN";
    //屏幕方向
    public static final String SCREEN_ORIENTATION = "SCREEN_ORIENTATION";
    public static final String LIGHT = "LIGHT";
    public static final String LIGHT_BACKGROUND = "lightBackground";
    public static final String HIGHLIGHT_BACKGROUND = "highlightBackground";
    private static final AllowedDirection DEFAULT_ALLOWED_DIRECTION = AllowedDirection.ONLY_VERTICAL;
    private static final Direction DEFAULT_DIRECTION = Direction.HORIZONTAL;
    private static final int DEFAULT_THEME_COLOR_INT =
            ContextCompat.getColor(AppContext.get(), R.color.default_theme_accent_color);

    private String font = "冬青黑体W3.otf";

    private String backgroundColor = "";
    private int bodyPadding = 2;
    private int textSpace = 1;
    private int fontSize = 2;
    //列数
    private int columnCount = 2;
    //亮度
    private int light = 100;
    private int lightBackground = 1;
    //高亮默认颜色index
    private int highlightBackground = 1;
    //是否开启横屏双页
    private boolean enableHorizontalColumn = true;
    //屏幕方向，0：自动，1：竖屏，2:横屏
    private int screenOrientation = 0;
    private boolean nightMode;
    @ColorInt
    private int themeColor = DEFAULT_THEME_COLOR_INT;
    private int nightThemeColor = themeColor;
    private boolean showTts = true;
    private boolean showTextSelection = true;
    private AllowedDirection allowedDirection = DEFAULT_ALLOWED_DIRECTION;
    private Direction direction = DEFAULT_DIRECTION;
    private boolean showRemainingIndicator = false;

    /**
     * Reading modes available
     */
    public enum AllowedDirection {
        ONLY_VERTICAL, ONLY_HORIZONTAL, VERTICAL_AND_HORIZONTAL
    }

    /**
     * Reading directions
     */
    public enum Direction {
        VERTICAL, HORIZONTAL
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putString(CONFIG_FONT, font);
        bundle.putInt(CONFIG_FONT_SIZE, fontSize);
        bundle.putBoolean(CONFIG_IS_NIGHT_MODE, nightMode);
        bundle.putInt(CONFIG_THEME_COLOR_INT, themeColor);
        bundle.putInt(CONFIG_NIGHT_THEME_COLOR_INT, nightThemeColor);
        bundle.putBoolean(CONFIG_IS_TTS, showTts);
        bundle.putBoolean(CONFIG_TEXT_SELECTION, showTextSelection);
        bundle.putString(CONFIG_ALLOWED_DIRECTION, allowedDirection.toString());
        bundle.putString(CONFIG_DIRECTION, direction.toString());
        bundle.putBoolean(CONFIG_REMAINING_INDICATOR, showRemainingIndicator);
        bundle.putString(BACKGROUND_COLOR, backgroundColor);
        bundle.putInt(BODY_PADDING, bodyPadding);
        bundle.putInt(TEXT_SPACE, textSpace);
        bundle.putInt(COLUMN_COUNT, columnCount);
        bundle.putInt(LIGHT, light);
        bundle.putInt(LIGHT_BACKGROUND, lightBackground);
        bundle.putInt(HIGHLIGHT_BACKGROUND, highlightBackground);
        bundle.putBoolean(ENABLE_HORIZONTAL_COLUMN, enableHorizontalColumn);
        bundle.putInt(SCREEN_ORIENTATION, screenOrientation);
        dest.writeBundle(bundle);
    }

    protected Config(Parcel in) {
        try {
            Bundle bundle = in.readBundle(getClass().getClassLoader());
            if (bundle == null) {
                // set defaults because of an old configuration
                Log.i("Parse Error", "Bundle does not exist, using default configuration.");
                setDefaults();
            } else {
                font = getBundleItem(bundle, CONFIG_FONT, "Roboto");
                backgroundColor = getBundleItem(bundle, BACKGROUND_COLOR,"");
                bodyPadding = getBundleItem(bundle, BODY_PADDING,2);
                textSpace = getBundleItem(bundle, TEXT_SPACE,5);
                columnCount = getBundleItem(bundle, COLUMN_COUNT,2);
                light = getBundleItem(bundle, LIGHT,100);
                lightBackground = getBundleItem(bundle, LIGHT_BACKGROUND,1);
                highlightBackground = getBundleItem(bundle, HIGHLIGHT_BACKGROUND,1);
                enableHorizontalColumn = getBundleItem(bundle, ENABLE_HORIZONTAL_COLUMN,true);
                screenOrientation = getBundleItem(bundle, SCREEN_ORIENTATION,0);
                fontSize = getBundleItem(bundle, CONFIG_FONT_SIZE, 2);
                nightMode = getBundleItem(bundle, CONFIG_IS_NIGHT_MODE, false);
                themeColor = getBundleItem(bundle, CONFIG_THEME_COLOR_INT, DEFAULT_THEME_COLOR_INT);
                nightThemeColor = getBundleItem(bundle, CONFIG_NIGHT_THEME_COLOR_INT, DEFAULT_THEME_COLOR_INT);
                showTts = getBundleItem(bundle, CONFIG_IS_TTS, true);
                showTextSelection = getBundleItem(bundle, CONFIG_TEXT_SELECTION, showTextSelection);
                allowedDirection = getAllowedDirectionFromString(
                        LOG_TAG,
                        getBundleItem(bundle, CONFIG_ALLOWED_DIRECTION, DEFAULT_ALLOWED_DIRECTION.toString())
                );
                direction = getDirectionFromString(
                        LOG_TAG,
                        getBundleItem(bundle, CONFIG_DIRECTION, DEFAULT_DIRECTION.toString())
                );
                showRemainingIndicator = getBundleItem(bundle, CONFIG_REMAINING_INDICATOR, showRemainingIndicator);
            }
        } catch (Exception e) {
            Log.i("Parse Error", "Bundle does not exist, using default configuration.");
            setDefaults();
        }
    }

    private void setDefaults() {
        font = "冬青黑体W3.otf";
        fontSize = 2;
        nightMode = false;
        themeColor = DEFAULT_THEME_COLOR_INT;
        nightThemeColor = themeColor;
        showTts = true;
        allowedDirection = DEFAULT_ALLOWED_DIRECTION;
        direction = DEFAULT_DIRECTION;
        showRemainingIndicator = false;
        showTextSelection = true;
        backgroundColor = null;
        bodyPadding = 2;
        textSpace = 5;
        columnCount = 2;
        light = 100;
        lightBackground = 1;
        highlightBackground = 1;
        enableHorizontalColumn = true;
        screenOrientation = 0;
    }

    @SuppressWarnings("unchecked")
    private <T> T getBundleItem(Bundle bundle, String key, T defaultValue) {
        if (bundle.containsKey(key)) {
            return (T) bundle.get(key);
        }
        return defaultValue;
    }

    public Config() {
    }

    public Config(JSONObject obj) {
        try {
            font = getJsonItem(obj, CONFIG_FONT, "冬青黑体W3.otf");
        } catch(Exception e) {
            Log.i("Parse Error", "Bundle does not exist, using default configuration.");
        }
        fontSize = getJsonItem(obj, CONFIG_FONT_SIZE, 2);
        nightMode = getJsonItem(obj, CONFIG_IS_NIGHT_MODE, false);
        themeColor = getValidColorInt(getJsonItem(obj, CONFIG_THEME_COLOR_INT, DEFAULT_THEME_COLOR_INT));
        nightThemeColor = getValidColorInt(getJsonItem(obj, CONFIG_NIGHT_THEME_COLOR_INT, DEFAULT_THEME_COLOR_INT));
        showTts = getJsonItem(obj, CONFIG_IS_TTS, true);
        showTextSelection = getJsonItem(obj, CONFIG_TEXT_SELECTION, true);
        allowedDirection = getAllowedDirectionFromString(
                LOG_TAG,
                getJsonItem(obj, CONFIG_ALLOWED_DIRECTION, DEFAULT_ALLOWED_DIRECTION.toString())
        );
        direction = getDirectionFromString(
                LOG_TAG,
                getJsonItem(obj, CONFIG_DIRECTION, DEFAULT_DIRECTION.toString())
        );
        showRemainingIndicator = getJsonItem(obj, CONFIG_REMAINING_INDICATOR, false);
        backgroundColor = getJsonItem(obj, BACKGROUND_COLOR, "");
        bodyPadding = getJsonItem(obj, BODY_PADDING, 2);
        textSpace = getJsonItem(obj, TEXT_SPACE, 5);
        columnCount = getJsonItem(obj, COLUMN_COUNT, 2);
        lightBackground = getJsonItem(obj, LIGHT_BACKGROUND, 1);
        light = getJsonItem(obj,LIGHT,100);
        highlightBackground = getJsonItem(obj, HIGHLIGHT_BACKGROUND, 1);
        enableHorizontalColumn = getJsonItem(obj, ENABLE_HORIZONTAL_COLUMN, true);
        screenOrientation = getJsonItem(obj, SCREEN_ORIENTATION, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T getJsonItem(JSONObject object, String key, T defaultValue) {
        if (object.has(key)) {
            return (T) object.opt(key);
        }
        return defaultValue;
    }

    public static Direction getDirectionFromString(final String LOG_TAG, String directionString) {

        switch (directionString) {
            case "VERTICAL":
                return Direction.VERTICAL;
            case "HORIZONTAL":
                return Direction.HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument directionString = `" + directionString
                        + "`, defaulting direction to " + DEFAULT_DIRECTION);
                return DEFAULT_DIRECTION;
        }
    }

    public static AllowedDirection getAllowedDirectionFromString(final String LOG_TAG,
                                                                 String allowedDirectionString) {

        switch (allowedDirectionString) {
            case "ONLY_VERTICAL":
                return AllowedDirection.ONLY_VERTICAL;
            case "ONLY_HORIZONTAL":
                return AllowedDirection.ONLY_HORIZONTAL;
            case "VERTICAL_AND_HORIZONTAL":
                return AllowedDirection.VERTICAL_AND_HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument allowedDirectionString = `"
                        + allowedDirectionString + "`, defaulting allowedDirection to "
                        + DEFAULT_ALLOWED_DIRECTION);
                return DEFAULT_ALLOWED_DIRECTION;
        }
    }

    public String getFont() {
        return font;
    }

    public Config setFont(String font) {
        this.font = font;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Config setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    public Config setNightMode(boolean nightMode) {
        this.nightMode = nightMode;
        return this;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @ColorInt
    public int getCurrentThemeColor() {
        return isNightMode() ? getNightThemeColor() : getThemeColor();
    }

    @ColorInt
    private int getValidColorInt(@ColorInt int colorInt) {
        if (colorInt >= 0) {
            Log.w(LOG_TAG, "-> getValidColorInt -> Invalid argument colorInt = " + colorInt +
                    ", Returning DEFAULT_THEME_COLOR_INT = " + DEFAULT_THEME_COLOR_INT);
            return DEFAULT_THEME_COLOR_INT;
        }
        return colorInt;
    }

    @ColorInt
    public int getThemeColor() {
        return themeColor;
    }

    @ColorInt
    public int getNightThemeColor() {
        return nightThemeColor;
    }

    public Config setThemeColorRes(@ColorRes int colorResId) {
        try {
            this.themeColor = ContextCompat.getColor(AppContext.get(), colorResId);
        } catch (Resources.NotFoundException e) {
            Log.w(LOG_TAG, "-> setThemeColorRes -> " + e);
            Log.w(LOG_TAG, "-> setThemeColorRes -> Defaulting themeColor to " +
                    DEFAULT_THEME_COLOR_INT);
            this.themeColor = DEFAULT_THEME_COLOR_INT;
        }
        return this;
    }

    public Config setThemeColorInt(@ColorInt int colorInt) {
        this.themeColor = getValidColorInt(colorInt);
        return this;
    }

    public Config setNightThemeColorRes(@ColorRes int colorResId) {
        try {
            this.nightThemeColor = ContextCompat.getColor(AppContext.get(), colorResId);
        } catch (Resources.NotFoundException e) {
            Log.w(LOG_TAG, "-> setNightThemeColorRes -> " + e);
            Log.w(LOG_TAG, "-> setNightThemeColorRes -> Defaulting themeColor to " +
                    DEFAULT_THEME_COLOR_INT);
            this.nightThemeColor = DEFAULT_THEME_COLOR_INT;
        }
        return this;
    }

    public Config setNightThemeColorInt(@ColorInt int colorInt) {
        this.nightThemeColor = getValidColorInt(colorInt);
        return this;
    }

    public boolean isShowTts() {
        return showTts;
    }

    public Config setShowTts(boolean showTts) {
        this.showTts = showTts;
        return this;
    }

    public int getBodyPadding() {
        return bodyPadding;
    }

    public void setBodyPadding(int bodyPadding) {
        this.bodyPadding = bodyPadding;
    }

    public int getTextSpace() {
        return textSpace;
    }

    public void setTextSpace(int textSpace) {
        this.textSpace = textSpace;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getLight() {
        return light;
    }

    public void setLight(int light) {
        this.light = light;
    }

    public int getLightBackground() {
        return lightBackground;
    }

    public void setLightBackground(int lightBackground) {
        this.lightBackground = lightBackground;
    }

    public int getHighlightBackground() {
        return highlightBackground;
    }

    public void setHighlightBackground(int highlightBackground) {
        this.highlightBackground = highlightBackground;
    }

    public boolean getEnableHorizontalColumn() {
        return enableHorizontalColumn;
    }

    public void setEnableHorizontalColumn(boolean enableHorizontalColumn) {
        this.enableHorizontalColumn = enableHorizontalColumn;
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }

    public void setScreenOrientation(int screenOrientation) {
        this.screenOrientation = screenOrientation;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public boolean isShowTextSelection() {
        return showTextSelection;
    }

    public Config setShowTextSelection(boolean showTextSelection) {
        this.showTextSelection = showTextSelection;
        Log.i(LOG_TAG, "-> setShowTextSelection -> " + showTextSelection);
        return this;
    }

    public AllowedDirection getAllowedDirection() {
        return allowedDirection;
    }

    /**
     * Set reading direction mode options for users. This method should be called before
     * {@link #setDirection(Direction)} as it has higher preference.
     *
     * @param allowedDirection reading direction mode options for users. Setting to
     *                         {@link AllowedDirection#VERTICAL_AND_HORIZONTAL} users will have
     *                         choice to select the reading direction at runtime.
     */
    public Config setAllowedDirection(AllowedDirection allowedDirection) {

        this.allowedDirection = allowedDirection;

        if (allowedDirection == null) {
            this.allowedDirection = DEFAULT_ALLOWED_DIRECTION;
            direction = DEFAULT_DIRECTION;
            Log.w(LOG_TAG, "-> allowedDirection cannot be null, defaulting " +
                    "allowedDirection to " + DEFAULT_ALLOWED_DIRECTION + " and direction to " +
                    DEFAULT_DIRECTION);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL &&
                direction != Direction.VERTICAL) {
            direction = Direction.VERTICAL;
            Log.w(LOG_TAG, "-> allowedDirection is " + allowedDirection +
                    ", defaulting direction to " + direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL &&
                direction != Direction.HORIZONTAL) {
            direction = Direction.HORIZONTAL;
            Log.w(LOG_TAG, "-> allowedDirection is " + allowedDirection
                    + ", defaulting direction to " + direction);
        }

        return this;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * Set reading direction. This method should be called after
     * {@link #setAllowedDirection(AllowedDirection)} as it has lower preference.
     *
     * @param direction reading direction.
     */
    public Config setDirection(Direction direction) {

        if (allowedDirection == AllowedDirection.VERTICAL_AND_HORIZONTAL && direction == null) {
            this.direction = DEFAULT_DIRECTION;
            Log.w(LOG_TAG, "-> direction cannot be `null` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL &&
                direction != Direction.VERTICAL) {
            this.direction = Direction.VERTICAL;
            Log.w(LOG_TAG, "-> direction cannot be `" + direction + "` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL &&
                direction != Direction.HORIZONTAL) {
            this.direction = Direction.HORIZONTAL;
            Log.w(LOG_TAG, "-> direction cannot be `" + direction + "` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else {
            this.direction = direction;
        }

        return this;
    }

    public boolean isShowRemainingIndicator() {
        return showRemainingIndicator;
    }

    /**
     * Sets the remaining indicator to be visible while reading
     *
     * @param showRemainingIndicator <t>true</t> to show the indicator while reading, otherwise <t>false</t>.
     */
    public void setShowRemainingIndicator(boolean showRemainingIndicator) {
        this.showRemainingIndicator = showRemainingIndicator;
    }

    @Override
    public String toString() {
        return "Config{" +
                "font=" + font +
                ", fontSize=" + fontSize +
                ", nightMode=" + nightMode +
                ", themeColor=" + themeColor +
                ", nightThemeColor=" + nightThemeColor +
                ", showTts=" + showTts +
                ", allowedDirection=" + allowedDirection +
                ", direction=" + direction +
                ", remainingIndicator=" + showRemainingIndicator +
                ", showTextSelection=" + showTextSelection +
                ", backgroundColor=" + backgroundColor +
                ", bodyPadding=" + bodyPadding +
                ", textSpace=" + textSpace +
                ", columnCount=" + columnCount +
                ", light=" + light +
                ", lightBackground=" + lightBackground +
                ", highlightBackground=" + highlightBackground +
                ", screenOrientation=" + screenOrientation +
                ", enableHorizontalColumn=" + enableHorizontalColumn +
                '}';
    }
}


