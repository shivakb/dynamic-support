/*
 * Copyright 2018-2021 Pranav Pandey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pranavpandey.android.dynamic.support.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.transition.Fade;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.app.DynamicLocaleDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.pranavpandey.android.dynamic.locale.DynamicLocale;
import com.pranavpandey.android.dynamic.locale.DynamicLocaleUtils;
import com.pranavpandey.android.dynamic.support.R;
import com.pranavpandey.android.dynamic.support.intent.DynamicIntent;
import com.pranavpandey.android.dynamic.support.listener.DynamicListener;
import com.pranavpandey.android.dynamic.support.listener.DynamicTransitionListener;
import com.pranavpandey.android.dynamic.support.model.DynamicAppTheme;
import com.pranavpandey.android.dynamic.support.motion.DynamicMotion;
import com.pranavpandey.android.dynamic.support.theme.DynamicTheme;
import com.pranavpandey.android.dynamic.support.theme.dialog.DynamicThemeDialog;
import com.pranavpandey.android.dynamic.support.theme.inflater.DynamicLayoutInflater;
import com.pranavpandey.android.dynamic.support.theme.listener.ThemeListener;
import com.pranavpandey.android.dynamic.theme.AppTheme;
import com.pranavpandey.android.dynamic.theme.Theme;
import com.pranavpandey.android.dynamic.theme.utils.DynamicThemeUtils;
import com.pranavpandey.android.dynamic.utils.DynamicColorUtils;
import com.pranavpandey.android.dynamic.utils.DynamicSdkUtils;
import com.pranavpandey.android.dynamic.utils.DynamicViewUtils;
import com.pranavpandey.android.dynamic.utils.DynamicWindowUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Base activity to perform all the system UI related tasks like setting the status and
 * navigation bar colors, theme, etc. It heavily depends on the {@link DynamicTheme} that can be
 * customised by implementing the corresponding methods.
 *
 * <p>Extend this activity and implement the various methods according to the need.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class DynamicSystemActivity extends AppCompatActivity implements
        DynamicLocale, DynamicListener, DynamicTransitionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Dynamic theme key to maintain its state.
     */
    protected static final String ADS_STATE_DYNAMIC_THEME = "ads_state_dynamic_theme";

    /**
     * Background color key to maintain its state.
     */
    protected static final String ADS_STATE_BACKGROUND_COLOR = "ads_state_background_color";

    /**
     * Status bar color key to maintain its state.
     */
    protected static final String ADS_STATE_STATUS_BAR_COLOR = "ads_state_status_bar_color";

    /**
     * Navigation bar color key to maintain its state.
     */
    protected static final String ADS_STATE_NAVIGATION_BAR_COLOR =
            "ads_state_navigation_bar_color";

    /**
     * Shared element map key to maintain its state.
     */
    protected static final String ADS_STATE_SHARED_ELEMENT_MAP = "ads_state_shared_element_map";

    /**
     * Transition result code key to maintain its state.
     */
    protected static final String ADS_STATE_TRANSITION_RESULT_CODE =
            "ads_state_transition_result_code";

    /**
     * Transition position key to maintain its state.
     */
    protected static final String ADS_STATE_TRANSITION_POSITION = "ads_state_transition_position";

    /**
     * Paused state key to maintain its state.
     */
    protected static final String ADS_STATE_PAUSED = "ads_state_paused";

    /**
     * Default tint color for the system UI elements like snackbars, etc.
     */
    protected static final @ColorInt int ADS_DEFAULT_SYSTEM_UI_COLOR =
            Color.parseColor("#F5F5F5");

    /**
     * Default background color for the system UI elements like status and navigation bar.
     */
    protected static final @ColorInt int ADS_DEFAULT_SYSTEM_BG_COLOR =
            Color.parseColor("#000000");

    /**
     * Default overlay color for the system UI elements like status and navigation bar.
     */
    protected static final @ColorInt int ADS_DEFAULT_SYSTEM_OVERLAY_COLOR =
            Color.parseColor("#1A000000");

    /**
     * App compat delegate used by this activity.
     */
    protected AppCompatDelegate mDynamicDelegate;

    /**
     * Dynamic context used by this activity.
     */
    protected Context mContext = this;

    /**
     * Current locale used by this activity.
     */
    protected Locale mCurrentLocale;

    /**
     * Saved instance state for this activity.
     */
    private Bundle mSavedInstanceState;

    /**
     * Dynamic app theme received from the intent.
     */
    private DynamicAppTheme mDynamicIntentTheme;

    /**
     * Background color used by the activity window.
     */
    protected @ColorInt int mBackgroundColor;

    /**
     * Current status bar color.
     */
    protected @ColorInt int mStatusBarColor;

    /**
     * Current navigation bar color.
     */
    protected @ColorInt int mNavigationBarColor;

    /**
     * Applied navigation bar color.
     */
    protected @ColorInt int mAppliedNavigationBarColor;

    /**
     * {@code true} if navigation bar theme is applied.
     */
    protected boolean mNavigationBarTheme;

    /**
     * Hash map to store the shared elements map.
     */
    private Map<String, Integer> mSharedElementMap;

    /**
     * Result code for the shared element transition.
     */
    private int mTransitionResultCode;

    /**
     * Transition position of the shared element.
     */
    private int mTransitionPosition;

    /**
     * Shared element transition.
     */
    private Transition mSharedElementTransition;

    /**
     * Callback for the shared element transition.
     */
    private SharedElementCallback mSharedElementCallback;

    /**
     * {@code true} if activity is finishing after running the support transition.
     */
    private boolean mFinishAfterTransition;

    /**
     * Listener to listen the transition events.
     */
    private DynamicTransitionListener mDynamicTransitionListener;

    /**
     * {@code true} this activity is paused before.
     *
     * @see #onPause();
     */
    private boolean mPaused;

    /**
     * Fix for app compat 1.2.0.
     * Use custom delegate to fix activity configurations (locale, font scale, etc.) when using
     * along with the application configuration.
     */
    @Override
    public @NonNull AppCompatDelegate getDelegate() {
        if (mDynamicDelegate == null) {
            this.mDynamicDelegate = new DynamicLocaleDelegate(
                    super.getDelegate(), this);
        }

        return mDynamicDelegate;
    }

    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        return mContext = super.createConfigurationContext(overrideConfiguration);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        updateThemeFromIntent(getIntent());
        setDynamicTheme();
        onSetSharedElementTransition();
        super.onCreate(savedInstanceState);

        mSavedInstanceState = savedInstanceState;

        mBackgroundColor = DynamicTheme.getInstance().get().getBackgroundColor();
        mStatusBarColor = DynamicTheme.getInstance().get().getPrimaryColorDark();
        mNavigationBarColor = DynamicTheme.getInstance().get().getPrimaryColorDark();

        if (savedInstanceState != null) {
            mBackgroundColor = savedInstanceState.getInt(
                    ADS_STATE_BACKGROUND_COLOR, mBackgroundColor);
            mPaused = savedInstanceState.getBoolean(ADS_STATE_PAUSED);
        }

        updateTaskDescription(DynamicTheme.getInstance().get().getPrimaryColor());
        setNavigationBarColor(mNavigationBarColor);

        onManageSharedElementTransition();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        onNewIntent(getIntent(), mSavedInstanceState == null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        onNewIntent(intent, true);
    }

    /**
     * Setup content according to the intent.
     *
     * @param intent The received intent.
     * @param newIntent {@code true} if updating from the new intent.
     */
    @CallSuper
    protected void onNewIntent(@Nullable Intent intent, boolean newIntent) {
        setIntent(intent);
        updateThemeFromIntent(intent);

        if (isThemeActivity() && (newIntent || mSavedInstanceState == null)) {
            checkForThemeIntent(intent);
        }
    }

    /**
     * Update content according to the intent.
     *
     * @param intent The received intent.
     * @param newIntent {@code true} if updating from the new intent.
     */
    protected void onUpdateIntent(@Nullable Intent intent, boolean newIntent) { }

    /**
     * Returns whether this activity can handle theme intents.
     *
     * @return {@code true} if this activity can handle theme intents.
     *
     * @see #checkForThemeIntent(Intent)
     * @see #onThemeIntent(String, String)
     */
    protected boolean isThemeActivity() {
        return false;
    }

    /**
     * Try to retrieve the dynamic theme from the intent.
     *
     * @param intent The intent to retrieve the theme.
     */
    private void updateThemeFromIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        mDynamicIntentTheme = DynamicTheme.getInstance().getTheme(getIntent()
                .getStringExtra(DynamicIntent.EXTRA_THEME));
    }

    /**
     * Checks for the valid dynamic theme intent.
     *
     * @param intent The intent to be checked.
     *
     * @see #onThemeIntent(String, String)
     */
    public void checkForThemeIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        if (Intent.ACTION_VIEW.equals(intent.getAction())
                || Intent.ACTION_SEND.equals(intent.getAction())) {
            if (DynamicThemeUtils.isValidThemeIntent(getContext(), intent)) {
                String name = DynamicThemeUtils.getThemeName(getContext(),
                        intent, getString(R.string.ads_data));

                DynamicThemeDialog.newImportIntentInstance()
                        .setThemeAction(Theme.Action.IMPORT_FILE)
                        .setThemeImportFileListener(
                                new ThemeListener.Import.File<Intent>() {
                                    @Override
                                    public @Nullable Intent getThemeSource() {
                                        return intent;
                                    }

                                    @Override
                                    public void onImportTheme(@Nullable String theme) {
                                        onThemeIntent(theme, name);
                                    }
                                })
                        .setMessage(name)
                        .showDialog(this);
            }
        }
    }

    /**
     * This method will be called on successfully verifying the dynamic theme intent.
     *
     * @param data The theme data retrieved from the intent.
     * @param name The theme or file name retrieved from the intent.
     *
     * @see #checkForThemeIntent(Intent)
     */
    protected void onThemeIntent(@Nullable String data, @Nullable String name) { }

    /**
     * Setup the shared element transition fot this activity.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void onSetSharedElementTransition() {
        if (!DynamicSdkUtils.is21()) {
            return;
        }

        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        mSharedElementCallback = new SharedElementCallback() {
            @Override
            public void onSharedElementEnd(
                    @NonNull List<String> sharedElementNames,
                    @NonNull List<View> sharedElements,
                    @NonNull List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames,
                        sharedElements, sharedElementSnapshots);

                resetSharedElementTransition();
            }

            @Override
            public void onMapSharedElements(@NonNull List<String> names,
                    @NonNull Map<String, View> sharedElements) {
                if (mSharedElementMap == null) {
                    mSharedElementMap = new HashMap<>();
                    for (Map.Entry<String, View> entry : sharedElements.entrySet()) {
                        mSharedElementMap.put(entry.getKey(), entry.getValue().getId());
                    }
                } else if (!mFinishAfterTransition) {
                    if (mDynamicTransitionListener != null) {
                        for (Map.Entry<String, Integer> entry : mSharedElementMap.entrySet()) {
                            if (!names.contains(entry.getKey())) {
                                names.add(entry.getKey());
                            }

                            if (sharedElements.size() < names.size()) {
                                sharedElements.put(entry.getKey(), onFindView(
                                        mTransitionResultCode, mTransitionPosition,
                                        entry.getKey(), entry.getValue()));
                            }
                        }
                    }

                    resetSharedElementTransition();
                } else {
                    resetSharedElementTransition();
                }
                super.onMapSharedElements(names, sharedElements);
            }
        };

        if (getWindow().getSharedElementEnterTransition() == null) {
            setExitSharedElementCallback(getSharedElementCallback());
        } else {
            setEnterSharedElementCallback(getSharedElementCallback());

            if (getSharedElementTransition() != null) {
                getWindow().setSharedElementEnterTransition(getSharedElementTransition());
                getWindow().setSharedElementExitTransition(getSharedElementTransition());
            }
        }
    }

    /**
     * Setup the shared element transition callbacks to manage it's position on
     * configuration change.
     */
    @SuppressWarnings("unchecked")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void onManageSharedElementTransition() {
        if (!DynamicSdkUtils.is21()) {
            return;
        }

        if (mSavedInstanceState != null) {
            if (mSavedInstanceState.getSerializable(ADS_STATE_SHARED_ELEMENT_MAP) != null) {
                mSharedElementMap = (HashMap<String, Integer>)
                        mSavedInstanceState.getSerializable(ADS_STATE_SHARED_ELEMENT_MAP);
                mTransitionResultCode = mSavedInstanceState.getInt(
                        ADS_STATE_TRANSITION_RESULT_CODE);
                mTransitionPosition = mSavedInstanceState.getInt(
                        ADS_STATE_TRANSITION_POSITION);
            }
        }

        onApplyTransitions(false);
    }

    @Override
    public void onApplyTransitions(boolean exit) {
        if (!DynamicSdkUtils.is21()) {
            return;
        }

        if (exit) {
            if (getWindow().getSharedElementEnterTransition() == null) {
                getWindow().setExitTransition((Transition) onAdjustExitReenterTransition(
                        getDynamicExitTransition(), true));
                getWindow().setReenterTransition((Transition) onAdjustExitReenterTransition(
                        getDynamicReenterTransition(), false));
            }
        } else {
            if (getWindow().getSharedElementEnterTransition() != null) {
                getWindow().setEnterTransition((Transition) onAdjustEnterReturnTransition(
                        getDynamicEnterTransition(), true));
                getWindow().setReturnTransition((Transition) onAdjustEnterReturnTransition(
                        getDynamicReturnTransition(), false));
                supportPostponeEnterTransition();

                if (getWindow().getEnterTransition() != null) {
                    getWindow().getEnterTransition().addListener(
                            new Transition.TransitionListener() {
                                @Override
                                public void onTransitionStart(Transition transition) { }

                                @Override
                                public void onTransitionEnd(Transition transition) {
                                    transition.removeListener(this);
                                    resetSharedElementTransition();
                                    setWindowBackground(Color.TRANSPARENT);
                                }

                                @Override
                                public void onTransitionCancel(Transition transition) {
                                    transition.removeListener(this);
                                    resetSharedElementTransition();
                                    setWindowBackground(Color.TRANSPARENT);
                                }

                                @Override
                                public void onTransitionPause(Transition transition) {
                                    transition.removeListener(this);
                                    resetSharedElementTransition();
                                    setWindowBackground(Color.TRANSPARENT);
                                }

                                @Override
                                public void onTransitionResume(Transition transition) { }
                            });
                }
            } else {
                getWindow().setExitTransition((Transition) onAdjustExitReenterTransition(
                        getDynamicExitTransition(), true));
                getWindow().setReenterTransition((Transition) onAdjustExitReenterTransition(
                        getDynamicReenterTransition(), false));
            }

            if (getSavedInstanceState() != null) {
                setWindowBackground(mBackgroundColor);
            }
        }

        getWindow().setAllowEnterTransitionOverlap(false);
        getWindow().setAllowReturnTransitionOverlap(false);

        final View transitionView = getPostponeTransitionView();
        if (transitionView != null) {
            transitionView.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            if (transitionView != null) {
                                transitionView.getViewTreeObserver()
                                        .removeOnPreDrawListener(this);
                            }
                            supportStartPostponedEnterTransition();

                            return true;
                        }
                    });
        }
    }

    @Override
    public @Nullable Object getDynamicEnterTransition() {
        // Using a transition is necessary to avoid flickering of status bar when using
        // the shared element transition.
        return DynamicMotion.getInstance().withDuration(new Fade());
    }

    @Override
    public @Nullable Object getDynamicReturnTransition() {
        return getDynamicEnterTransition();
    }

    @Override
    public @Nullable Object getDynamicExitTransition() {
        // Using a transition is necessary to avoid flickering of status bar when using
        // the shared element transition.
        return DynamicMotion.getInstance().withDuration(new Fade());
    }

    @Override
    public @Nullable Object getDynamicReenterTransition() {
        return getDynamicExitTransition();
    }

    @Override
    public @Nullable Object onAdjustEnterReturnTransition(
            @Nullable Object transition, boolean enter) {
        if (transition instanceof Transition) {
            if (enter) {
                ((Transition) transition).excludeTarget(getWindow().getDecorView()
                        .findViewById(R.id.action_bar_container), true);
                ((Transition) transition).excludeTarget(
                        android.R.id.statusBarBackground, true);
                ((Transition) transition).excludeTarget(
                        android.R.id.navigationBarBackground, true);
            }
        }

        return transition;
    }

    @Override
    public @Nullable Object onAdjustExitReenterTransition(
            @Nullable Object transition, boolean exit) {
        if (transition != null) {
            ((Transition) transition).excludeTarget(getWindow().getDecorView()
                    .findViewById(R.id.action_bar_container), true);
            ((Transition) transition).excludeTarget(
                    android.R.id.statusBarBackground, true);
            ((Transition) transition).excludeTarget(
                    android.R.id.navigationBarBackground, true);
        }

        return transition;
    }

    @Override
    public @Nullable View getPostponeTransitionView() {
        if (mDynamicTransitionListener != null) {
            return mDynamicTransitionListener.getPostponeTransitionView();
        }

        return getContentView();
    }

    @Override
    public @Nullable View onFindView(int resultCode, int position,
            @NonNull String transition, @IdRes int viewId) {
        View view = mDynamicTransitionListener == null ? findViewById(viewId)
                : mDynamicTransitionListener.onFindView(resultCode, position, transition, viewId);

        if (view != null) {
            view.setTag(null);
        }

        return view;
    }

    /**
     * Resets the shared element transition.
     */
    protected void resetSharedElementTransition() {
        mBackgroundColor = DynamicTheme.getInstance().get().getBackgroundColor();
        mSharedElementMap = null;
        mDynamicTransitionListener = null;
        mFinishAfterTransition = false;
    }

    /**
     * Commit the fragment transaction.
     *
     * @param fragmentTransaction The fragment transaction to be committed.
     */
    public void commitFragmentTransaction(@NonNull FragmentTransaction fragmentTransaction) {
        try {
            fragmentTransaction.commit();
        } catch (Exception e) {
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(ADS_STATE_BACKGROUND_COLOR, mBackgroundColor);
        outState.putInt(ADS_STATE_STATUS_BAR_COLOR, mStatusBarColor);
        outState.putInt(ADS_STATE_NAVIGATION_BAR_COLOR, mNavigationBarColor);
        outState.putInt(ADS_STATE_TRANSITION_RESULT_CODE, mTransitionResultCode);
        outState.putInt(ADS_STATE_TRANSITION_POSITION, mTransitionPosition);
        outState.putSerializable(ADS_STATE_SHARED_ELEMENT_MAP, (Serializable) mSharedElementMap);
        outState.putBoolean(ADS_STATE_PAUSED, mPaused);
    }

    @Override
    public @Nullable String[] getSupportedLocales() {
        if (DynamicTheme.getInstance().getListener() instanceof DynamicLocale) {
            return ((DynamicLocale) DynamicTheme.getInstance().getListener()).getSupportedLocales();
        }

        return null;
    }

    @Override
    public @NonNull Locale getDefaultLocale(@NonNull Context context) {
        return DynamicLocaleUtils.getDefaultLocale(context, getSupportedLocales());
    }

    @Override
    public @Nullable Locale getLocale() {
        if (DynamicTheme.getInstance().getListener() instanceof DynamicLocale) {
            return ((DynamicLocale) DynamicTheme.getInstance().getListener()).getLocale();
        }

        return DynamicLocaleUtils.getCurrentLocale(DynamicTheme.getInstance().getContext());
    }

    @Override
    public @NonNull Context setLocale(@NonNull Context context) {
        mCurrentLocale = DynamicLocaleUtils.getLocale(getLocale(), getDefaultLocale(context));
        return mContext = DynamicLocaleUtils.setLocale(context, mCurrentLocale, getFontScale());
    }

    @Override
    public float getFontScale() {
        if (getDynamicTheme() != null) {
            return getDynamicTheme().getFontScaleRelative();
        } else if (DynamicTheme.getInstance().getListener() instanceof DynamicLocale) {
            return ((DynamicLocale) DynamicTheme.getInstance().getListener()).getFontScale();
        }

        return DynamicTheme.getInstance().get(false).getFontScaleRelative();
    }

    /**
     * This method will be called just before the {@link #onCreate(Bundle)} after applying
     * the theme.
     * <p>Override this method to customise the theme further.
     */
    protected void onCustomiseTheme() { }

    /**
     * Returns a layout inflater factory for this activity.
     * <p>It will be used to replace the app compat widgets with their dynamic counterparts
     * to provide the support for dynamic theme.
     *
     * <p>Override this method to provide a custom layout inflater.
     *
     * @return The layout inflater factory for this activity.
     */
    protected @Nullable LayoutInflater.Factory2 getDynamicLayoutInflater() {
        return new DynamicLayoutInflater();
    }

    /**
     * Returns the parent content view used by this activity.
     *
     * @return The parent content view used by this activity.
     */
    public abstract  @NonNull View getContentView();

    /**
     * Get the current locale used by this activity.
     *
     * @return The current locale used by this activity.
     */
    public @NonNull Locale getCurrentLocale() {
        return mCurrentLocale;
    }

    /**
     * Get the current saved instance state for this activity.
     *
     * @return The current saved instance state for this activity.
     */
    public @Nullable Bundle getSavedInstanceState() {
        return mSavedInstanceState;
    }

    /**
     * Returns the dynamic app theme received from the intent.
     *
     * @return The dynamic app theme received from the intent.
     */
    public @Nullable DynamicAppTheme getDynamicIntentTheme() {
        return mDynamicIntentTheme;
    }

    /**
     * Returns the transition result code for this activity.
     *
     * @return The transition result code for this activity.
     */
    public int getTransitionResultCode() {
        return mTransitionResultCode;
    }

    /**
     * Sets the transition result code for this activity.
     *
     * @param transitionResultCode The transition result code to be set.
     */
    public void setTransitionResultCode(int transitionResultCode) {
        this.mTransitionResultCode = transitionResultCode;
    }

    /**
     * Returns the transition position of the shared element.
     *
     * @return The transition position of the shared element.
     */
    public int getTransitionPosition() {
        return mTransitionPosition;
    }

    /**
     * Sets the transition position of the shared element.
     *
     * @param transitionPosition The transition position to be set.
     */
    public void setTransitionPosition(int transitionPosition) {
        this.mTransitionPosition = transitionPosition;
    }

    /**
     * Returns the shared element transition.
     *
     * @return The shared element transition.
     */
    public @Nullable Transition getSharedElementTransition() {
        return mSharedElementTransition;
    }

    /**
     * Sets the shared element transition.
     *
     * @param sharedElementTransition The shared element transition to be set.
     */
    public void setSharedElementTransition(@Nullable Transition sharedElementTransition) {
        this.mSharedElementTransition = sharedElementTransition;
    }

    /**
     * Returns the callback for the shared element transition.
     *
     * @return The callback for the shared element transition.
     */
    public @Nullable SharedElementCallback getSharedElementCallback() {
        return mSharedElementCallback;
    }

    /**
     * Sets the callback for the shared element transition.
     *
     * @param sharedElementCallback The callback for the shared element transition.
     */
    public void setSharedElementCallback(@Nullable SharedElementCallback sharedElementCallback) {
        this.mSharedElementCallback = sharedElementCallback;
    }

    /**
     * Returns the dynamic transition listener used by this activity.
     *
     * @return The dynamic transition listener used by this activity.
     */
    public @Nullable DynamicTransitionListener getDynamicTransitionListener() {
        return mDynamicTransitionListener;
    }

    /**
     * Sets the dynamic transition listener for this activity.
     *
     * @param dynamicTransitionListener The dynamic transition listener to be set.
     */
    public void setDynamicTransitionListener(
            @Nullable DynamicTransitionListener dynamicTransitionListener) {
        this.mDynamicTransitionListener = dynamicTransitionListener;

        onApplyTransitions(false);
    }

    /**
     * Checks whether this activity is launched from the history.
     *
     * @return {@code true} if this activity is launched from the history.
     */
    public boolean isLaunchedFromHistory() {
        return getIntent() != null && (getIntent().getFlags()
                & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0;
    }

    /**
     * Set the dynamic app theme and style resource for this activity.
     */
    private void setDynamicTheme() {
        DynamicTheme.getInstance().attach(this, getDynamicLayoutInflater())
                .setLocalTheme(getThemeRes(), getDynamicTheme());
        setWindowBackground(getBackgroundColor());

        onCustomiseTheme();
    }

    /**
     * Returns whether the navigation bar theme is applied for this activity.
     *
     * @return {@code true} if navigation bar theme is applied.
     */
    public boolean isNavigationBarTheme() {
        return mNavigationBarTheme;
    }

    /**
     * Sets whether the navigation bar theme should be applied for this activity in landscape mode.
     * <p>It will be applied only on the API 21 and above devices.
     *
     * <p>By default it will use the {@link Theme.ColorType#PRIMARY_DARK} color, use
     * {@link #setNavigationBarColor(int)} to set a custom color.
     *
     * @return {@code true} to apply navigation bar theme for this activity in the landscape mode.
     */
    protected boolean setNavigationBarThemeInLandscape() {
        return getResources().getBoolean(R.bool.ads_navigation_bar_theme_landscape);
    }

    /**
     * Sets whether register a shared preferences listener for this activity.
     *
     * @return {@code true} to register a {@link SharedPreferences.OnSharedPreferenceChangeListener}
     *         to receive preference change callback.
     */
    protected boolean setOnSharedPreferenceChangeListener() {
        return true;
    }

    /**
     * This method will be called after the theme has been changed.
     * <p>Override this method to perform operations after the theme has been changed like
     * re-initialize the {@link DynamicTheme} with new colors, etc.
     */
    protected void onAppThemeChange() {
        getWindow().setWindowAnimations(R.style.Animation_DynamicApp_Window_FadeInOut);
        ActivityCompat.recreate(this);
    }

    /**
     * This method will be called after the navigation bar theme has been changed.
     * <p>Override this method to perform operations after the navigation bar theme has been
     * changed like update it with new colors.
     */
    protected void navigationBarThemeChange() {
        onDynamicChanged(false, true);
    }

    /**
     * Returns the default background color for this activity.
     *
     * @return The default background color for this activity.
     */
    public @ColorInt int getBackgroundColor() {
        return DynamicTheme.getInstance().get().getBackgroundColor();
    }

    /**
     * Sets the window background color.
     *
     * @param color The window background color to be set.
     */
    public void setWindowBackground(@ColorInt int color) {
        this.mBackgroundColor = color;
        getWindow().setBackgroundDrawable(new ColorDrawable(mBackgroundColor));
    }

    /**
     * Set the status bar color.
     * <p>It will be applied only on the API 21 and above devices.
     *
     * @param color Color to be applied on the status bar.
     */
    protected void setWindowStatusBarColor(@ColorInt int color) {
        if (DynamicSdkUtils.is21()) {
            getWindow().setStatusBarColor(color);
        }
    }

    /**
     * Set the status bar color.
     * <p>It will be applied only on the API 21 and above devices.
     *
     * @param color The color to be applied.
     */
    public void setStatusBarColor(@ColorInt int color) {
        if (DynamicSdkUtils.is21()) {
            this.mStatusBarColor = color;
            updateStatusBar();
        }
    }

    /**
     * Set the status bar color resource.
     * <p>It will be applied only on the API 21 and above devices.
     *
     * @param colorRes The color resource to be applied.
     */
    public void setStatusBarColorRes(@ColorRes int colorRes) {
        setStatusBarColor(ContextCompat.getColor(this, colorRes));
    }

    /**
     * Set the translucent status bar flag, useful in case of {@link CollapsingToolbarLayout}.
     * <p>It will be applied only on the API 21 and above devices.
     */
    public void setTranslucentStatusBar() {
        if (DynamicSdkUtils.is21()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * Update status bar color according to the supplied parameters. It will automatically
     * check for the light or dark color and will change the status bar icons or background
     * accordingly so that the icons will always be visible.
     */
    protected void updateStatusBar() {
        boolean isLightColor = !DynamicColorUtils.isColorDark(mStatusBarColor);
        if (DynamicTheme.getInstance().get().isBackgroundAware() && isLightColor) {
            if (!DynamicSdkUtils.is23()) {
                mStatusBarColor = DynamicColorUtils.getContrastColor(
                        mStatusBarColor, ADS_DEFAULT_SYSTEM_UI_COLOR);
            }
        }

        DynamicViewUtils.setLightStatusBar(getWindow().getDecorView(), isLightColor);
    }

    /**
     * Checks whether to enable edge-to-edge content.
     * <p>Override this method to provide your own implementation.
     *
     * @return {@code true} to enable edge-to-edge content.
     */
    public boolean isEdgeToEdgeContent() {
        return !mNavigationBarTheme && DynamicWindowUtils.isGestureNavigation(this);
    }

    /**
     * Returns the view to apply edge-to-edge window insets.
     *
     * @return The view to apply edge-to-edge window insets.
     *
     * @see #isApplyEdgeToEdgeInsets()
     */
    public @Nullable View getEdgeToEdgeView() {
        return null;
    }

    /**
     * Returns whether to apply edge-to-edge window insets.
     *
     * @return {@code true} to apply edge-to-edge window insets.
     *
     * @see #getEdgeToEdgeView()
     */
    public boolean isApplyEdgeToEdgeInsets() {
        return true;
    }

    /**
     * Returns the bottom view to apply edge-to-edge window insets.
     *
     * @return The bottom view to apply edge-to-edge window insets.
     *
     * @see #isApplyEdgeToEdgeInsets()
     */
    public @Nullable View getEdgeToEdgeViewBottom() {
        return getEdgeToEdgeView();
    }

    /**
     * Set the navigation bar color.
     * <p>It will be applied only on the API 21 and above devices.
     *
     * @param color The color to be applied.
     */
    public void setNavigationBarColor(@ColorInt int color) {
        if (DynamicTheme.getInstance().get().isBackgroundAware()
                && !DynamicSdkUtils.is26()) {
            color = DynamicColorUtils.getContrastColor(color, ADS_DEFAULT_SYSTEM_UI_COLOR);
        }

        int orientation = DynamicWindowUtils.getScreenOrientation(this);
        if (DynamicWindowUtils.isNavigationBarThemeSupported(this)
                && (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                || orientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)) {
            if (!setNavigationBarThemeInLandscape()) {
                color = ADS_DEFAULT_SYSTEM_BG_COLOR;
            }
        }

        this.mNavigationBarColor = color;
        if (DynamicSdkUtils.is21()) {
            this.mNavigationBarTheme = setNavigationBarTheme();

            if (isEdgeToEdgeContent()) {
                if ((getWindow().getDecorView().getSystemUiVisibility()
                        & View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                        != View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION) {
                    DynamicViewUtils.setEdgeToEdge(getWindow().getDecorView(), true);
                }

                if (isApplyEdgeToEdgeInsets() && getEdgeToEdgeView() != null) {
                    ViewCompat.setOnApplyWindowInsetsListener(getEdgeToEdgeView(),
                            new OnApplyWindowInsetsListener() {
                                @Override
                                public WindowInsetsCompat onApplyWindowInsets(
                                        View v, WindowInsetsCompat insets) {
                                    if (!(v.getLayoutParams()
                                            instanceof  ViewGroup.MarginLayoutParams)) {
                                        return insets;
                                    }

                                    final ViewGroup.MarginLayoutParams lp =
                                            (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                                    lp.topMargin = insets.getInsets(
                                            WindowInsetsCompat.Type.systemBars()).top;
                                    v.setLayoutParams(lp);

                                    DynamicViewUtils.applyWindowInsetsBottom(
                                            getEdgeToEdgeViewBottom(), true);
                                    return insets;
                                }
                            });
                }

                mAppliedNavigationBarColor = Color.TRANSPARENT;
            } else {
                mAppliedNavigationBarColor = mNavigationBarTheme
                        ? color : ADS_DEFAULT_SYSTEM_BG_COLOR;
            }

            getWindow().setNavigationBarColor(mAppliedNavigationBarColor);
        } else {
            mAppliedNavigationBarColor = mNavigationBarColor;
        }

        updateNavigationBar();
    }

    /**
     * Set the navigation bar color resource.
     * <p>It will be applied only on the API 21 and above devices.
     *
     * @param colorRes The color resource to be applied.
     */
    public void setNavigationBarColorRes(@ColorRes int colorRes) {
        setNavigationBarColor(ContextCompat.getColor(this, colorRes));
    }

    /**
     * Set the translucent navigation bar flag, useful in case of to show the layout behind the
     * navigation bar.
     * <p>It will be applied only on the API 21 and above devices.
     */
    public void setTranslucentNavigationBar() {
        if (DynamicSdkUtils.is21()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * Update navigation bar color according to the supplied parameters. It will automatically
     * check for the light or dark color and will background accordingly so that the buttons will
     * always be visible.
     */
    protected void updateNavigationBar() {
        DynamicViewUtils.setLightNavigationBar(getWindow().getDecorView(),
                !DynamicColorUtils.isColorDark(mAppliedNavigationBarColor));
    }

    /**
     * Get the current status bar color.
     *
     * @return The current status bar color.
     */
    public int getStatusBarColor() {
        return mStatusBarColor;
    }

    /**
     * Get the current (original) navigation bar color.
     * <p>It may be different from the actually applied color. To get the user visible color,
     * use {@link #getAppliedNavigationBarColor()}.
     *
     * @return The current navigation bar color.
     *
     * @see #getAppliedNavigationBarColor()
     */
    public int getNavigationBarColor() {
        return mNavigationBarColor;
    }

    /**
     * Get the applied navigation bar color.
     *
     * @return The applied navigation bar color.
     */
    public int getAppliedNavigationBarColor() {
        return mAppliedNavigationBarColor;
    }

    @Override
    public void startActivityForResult(@SuppressLint("UnknownNullness") Intent intent,
            int requestCode, @Nullable Bundle options) {
        mTransitionResultCode = requestCode;
        onApplyTransitions(true);
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    public void startActivityFromFragment(@NonNull Fragment fragment,
            @SuppressLint("UnknownNullness") Intent intent,
            int requestCode, @Nullable Bundle options) {
        mTransitionResultCode = requestCode;
        onApplyTransitions(true);
        super.startActivityFromFragment(fragment, intent, requestCode, options);
    }

    /**
     * Call {@link #startActivity(Intent, Bundle)} for this activity.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param intent The intent to be used.
     * @param options The intent to be set.
     * @param motion {@code true} if motion is enabled.
     * @param finish {@code true} to finish calling activity.
     * @param afterTransition {@code true} to finish the calling activity after transition.
     */
    public void startMotionActivity(@SuppressLint("UnknownNullness") Intent intent,
            @Nullable Bundle options, boolean motion, boolean finish,
            boolean afterTransition) {
        if (motion) {
            ActivityCompat.startActivity(this, intent, options);
        } else {
            startActivity(intent);
        }

        finishMotionActivity(finish, afterTransition);
    }

    /**
     * Call {@link #startActivity(Intent, Bundle)} for this activity.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param intent The intent to be used.
     * @param options The intent to be set.
     * @param finish {@code true} to finish calling activity.
     * @param afterTransition {@code true} to finish the calling activity after transition.
     *
     * @see #startMotionActivity(Intent, Bundle, boolean, boolean, boolean)
     */
    public void startMotionActivity(@SuppressLint("UnknownNullness") Intent intent,
            @Nullable Bundle options, boolean finish, boolean afterTransition) {
        startMotionActivity(intent, options, DynamicMotion.getInstance().isMotion(),
                finish, afterTransition);
    }

    /**
     * Call {@link #startActivity(Intent, Bundle)} for this activity.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param intent The intent to be used.
     * @param options The intent to be set.
     */
    public void startMotionActivity(@SuppressLint("UnknownNullness") Intent intent,
            @Nullable Bundle options) {
        startMotionActivity(intent, options, false, false);
    }

    /**
     * Call {@link #startActivityForResult(Intent, int, Bundle)} for this activity.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param intent The intent to be used.
     * @param requestCode The request code to be set.
     * @param options The intent to be set.
     * @param motion {@code true} if motion is enabled.
     * @param finish {@code true} to finish calling activity.
     * @param afterTransition {@code true} to finish the calling activity after transition.
     */
    public void startMotionActivityForResult(@SuppressLint("UnknownNullness") Intent intent,
            int requestCode, @Nullable Bundle options, boolean motion,
            boolean finish, boolean afterTransition) {
        if (motion) {
            ActivityCompat.startActivityForResult(this, intent, requestCode, options);
        } else {
            startActivityForResult(intent, requestCode);
        }

        finishMotionActivity(finish, afterTransition);
    }

    /**
     * Call {@link #startActivityForResult(Intent, int, Bundle)} for this activity.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param intent The intent to be used.
     * @param requestCode The request code to be set.
     * @param options The intent to be set.
     * @param finish {@code true} to finish calling activity.
     * @param afterTransition {@code true} to finish the calling activity after transition.
     *
     * @see #startMotionActivityForResult(Intent, int, Bundle, boolean, boolean, boolean)
     */
    public void startMotionActivityForResult(@SuppressLint("UnknownNullness") Intent intent,
            int requestCode, @Nullable Bundle options, boolean finish, boolean afterTransition) {
        startMotionActivityForResult(intent, requestCode, options,
                DynamicMotion.getInstance().isMotion(), finish, afterTransition);
    }

    /**
     * Call {@link #startActivityForResult(Intent, int, Bundle)} for this activity.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param intent The intent to be used.
     * @param requestCode The request code to be set.
     * @param options The intent to be set.
     */
    public void startMotionActivityForResult(@SuppressLint("UnknownNullness") Intent intent,
            int requestCode, @Nullable Bundle options) {
        startMotionActivityForResult(intent, requestCode, options, false, false);
    }

    /**
     * Called by Fragment.startActivityForResult() to implement its behavior.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param fragment The calling fragment.
     * @param intent The intent to be used.
     * @param requestCode The request code to be set.
     * @param options The intent to be set.
     * @param motion {@code true} if motion is enabled.
     * @param finish {@code true} to finish calling activity.
     * @param afterTransition {@code true} to finish the calling activity after transition.
     */
    public void startMotionActivityFromFragment(@NonNull Fragment fragment,
            @SuppressLint("UnknownNullness") Intent intent, int requestCode,
            @Nullable Bundle options, boolean motion, boolean finish, boolean afterTransition) {
        if (motion) {
            startActivityFromFragment(fragment, intent, requestCode, options);
        } else {
            startActivityFromFragment(fragment, intent, requestCode);
        }

        finishMotionActivity(finish, afterTransition);
    }

    /**
     * Called by Fragment.startActivityForResult() to implement its behavior.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param fragment The calling fragment.
     * @param intent The intent to be used.
     * @param requestCode The request code to be set.
     * @param options The intent to be set.
     * @param finish {@code true} to finish calling activity.
     * @param afterTransition {@code true} to finish the calling activity after transition.
     *
     * @see #startMotionActivityFromFragment(Fragment, Intent, int, Bundle, boolean, boolean, boolean)
     */
    public void startMotionActivityFromFragment(@NonNull Fragment fragment,
            @SuppressLint("UnknownNullness") Intent intent, int requestCode,
            @Nullable Bundle options, boolean finish, boolean afterTransition) {
        startMotionActivityFromFragment(fragment, intent, requestCode, options,
                DynamicMotion.getInstance().isMotion(), finish, afterTransition);
    }

    /**
     * Called by Fragment.startActivityForResult() to implement its behavior.
     *
     * <p>It will allow {@link Bundle} options if {@link DynamicMotion#isMotion()} is enabled.
     *
     * @param fragment The calling fragment.
     * @param intent The intent to be used.
     * @param requestCode The request code to be set.
     * @param options The intent to be set.
     */
    public void startMotionActivityFromFragment(@NonNull Fragment fragment,
            @SuppressLint("UnknownNullness") Intent intent, int requestCode,
            @Nullable Bundle options) {
        startMotionActivityFromFragment(fragment, intent, requestCode,
                options, false, false);
    }

    /**
     * Finish the motion activity after playing the exit transition.
     *
     * @param finish {@code true} to finish the activity.
     * @param finishAfterTransition {@code true} to finish the activity after transition.
     */
    private void finishMotionActivity(boolean finish, boolean finishAfterTransition) {
        if (!finish) {
            return;
        }

        if (!DynamicSdkUtils.is21() || !finishAfterTransition) {
            finishActivity();
        } else if (getWindow().getSharedElementExitTransition() != null) {
            getWindow().getSharedElementExitTransition().addListener(
                    new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) { }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            transition.removeListener(this);
                            finish();
                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {
                            transition.removeListener(this);
                            finish();
                        }

                        @Override
                        public void onTransitionPause(Transition transition) {
                            transition.removeListener(this);
                            finish();
                        }

                        @Override
                        public void onTransitionResume(Transition transition) { }
                    });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        onApplyTransitions(false);

        if (setOnSharedPreferenceChangeListener()) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .registerOnSharedPreferenceChangeListener(this);
        }

        if (!DynamicTheme.getInstance().isDynamicListener(this)) {
            setDynamicTheme();
            setNavigationBarColor(mNavigationBarColor);

             /*
              * Hint: Do not delay on main handler as it was introducing the lag in the app.
              * Hint: Update task description color on UI thread to get the proper theme values.
              */
            String theme = DynamicTheme.getInstance()
                    .getLocalTheme(DynamicSystemActivity.this);
            if (theme != null && !theme.equals(DynamicTheme.getInstance().toString())) {
                onDynamicChanged(false, true);
            } else if (mCurrentLocale != null && !mCurrentLocale.equals(
                    DynamicLocaleUtils.getLocale(getLocale(), getDefaultLocale(getContext())))
                    || (DynamicTheme.getInstance().getLocal() != null && getFontScale()
                    != DynamicTheme.getInstance().getLocal().getFontScaleRelative())) {
                onDynamicChanged(true, true);
            }

            if (DynamicSdkUtils.is21()) {
                runOnUiThread(mDynamicRunnable);
            }
        }
    }

    /**
     * Runnable to change the dynamic theme on resume.
     */
    private final Runnable mDynamicRunnable = new Runnable() {
        @Override
        public void run() {
            updateTaskDescription(DynamicTheme.getInstance().get().getPrimaryColor());
        }
    };

    @Override
    public void onPause() {
        mPaused = true;

        if (setOnSharedPreferenceChangeListener()) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .unregisterOnSharedPreferenceChangeListener(this);
        }
        DynamicTheme.getInstance().onLocalDestroy(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        DynamicTheme.getInstance().deleteLocalTheme(this);
        super.onDestroy();
    }

    @Override
    public @NonNull Context getContext() {
        return mContext != null ? mContext : getBaseContext();
    }

    @Override
    public @StyleRes int getThemeRes(@Nullable AppTheme<?> theme) {
        return DynamicTheme.getInstance().getListener().getThemeRes(theme);
    }

    @Override
    public @StyleRes int getThemeRes() {
        return DynamicTheme.getInstance().getListener().getThemeRes();
    }

    @Override
    public @Nullable AppTheme<?> getDynamicTheme() {
        return DynamicTheme.getInstance().getListener().getDynamicTheme();
    }

    @Override
    public @ColorInt int getDefaultColor(@Theme.ColorType int colorType) {
        return DynamicTheme.getInstance().getListener().getDefaultColor(colorType);
    }

    @Override
    public void onDynamicChanged(boolean context, boolean recreate) {
        if (context) {
            setLocale(getBaseContext());
            setLocale(getContext());
        }

        if (recreate) {
            onAppThemeChange();
        }
    }

    @Override
    public void onDynamicConfigurationChanged(boolean locale, boolean fontScale,
            boolean orientation, boolean uiMode, boolean density) {
        onDynamicChanged(locale || fontScale || orientation
                || uiMode || density, locale || uiMode);
    }

    @Override
    public void onAutoThemeChanged() { }

    @Override
    public void onPowerSaveModeChanged(boolean powerSaveMode) { }

    /**
     * Sets whether the navigation bar theme should be applied for this activity.
     * <p>It will be applied only on the API 21 and above devices.
     *
     * <p>By default it will use the {@link Theme.ColorType#PRIMARY_DARK} color, use
     * {@link #setNavigationBarColor(int)} to set a custom color.
     *
     * @return {@code true} to apply navigation bar theme for this activity.
     */
    @Override
    public boolean setNavigationBarTheme() {
        return DynamicTheme.getInstance().getListener().setNavigationBarTheme();
    }

    @Override
    public void onNavigationBarThemeChanged() {
        navigationBarThemeChange();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { }

    /**
     * Update the task description on API 21 and above devices to match it with the theme
     * color.
     *
     * @param color The color to be set.
     */
    protected void updateTaskDescription(@ColorInt int color) {
        if (DynamicSdkUtils.is21()) {
            setTaskDescription(new ActivityManager.TaskDescription(null, null,
                    DynamicColorUtils.removeAlpha(color)));
        }
    }

    @Override
    public void onBackPressed() {
        if (getOnBackPressedDispatcher().hasEnabledCallbacks()) {
            getOnBackPressedDispatcher().onBackPressed();
        } else {
            finishActivity();
        }
    }

    @Override
    public void finish() {
        super.finish();

        onSetFallbackActivityOptions();
    }

    @Override
    public void supportStartPostponedEnterTransition() {
        try {
            super.supportStartPostponedEnterTransition();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void supportFinishAfterTransition() {
        mFinishAfterTransition = true;

        if (mSavedInstanceState != null) {
            resetSharedElementTransition();
        }
        super.supportFinishAfterTransition();
    }

    /**
     * This method will be called on setting the fallback activity options on unsupported
     * API level like overriding a pending transition on API 15 and below.
     */
    protected void onSetFallbackActivityOptions() { }

    /**
     * Checks whether this activity should be finished after the transition.
     *
     * @return Returns {@code true} if this activity should be finished after the transition.
     *
     * @see #supportFinishAfterTransition()
     */
    public boolean isSupportFinishAfterTransition() {
        return DynamicMotion.getInstance().isMotion() && DynamicSdkUtils.is21()
                && (getWindow().getSharedElementEnterTransition() != null
                || getWindow().getSharedElementExitTransition() != null);
    }

    /**
     * Checks whether the activity was paused before.
     *
     * @return {@code true} if the activity was paused before.
     */
    public boolean isPaused() {
        return mPaused;
    }

    /**
     * Finish the activity properly after checking the shared element transition.
     */
    public void finishActivity() {
        if (!isFinishing()) {
            if (isSupportFinishAfterTransition()) {
                supportFinishAfterTransition();
            } else {
                finish();
            }
        }
    }
}
