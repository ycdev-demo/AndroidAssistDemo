package me.ycdev.android.demo.assist.voice;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.VoiceInteractor;
import android.app.assist.AssistContent;
import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.demo.assist.R;

@RequiresApi(api = Build.VERSION_CODES.M)
class MainInteractionSession extends VoiceInteractionSession
        implements View.OnClickListener {
    private static final String TAG = "MainInteractionSession";

    private static final int STATE_IDLE = 0;
    private static final int STATE_LAUNCHING = 1;
    private static final int STATE_CONFIRM = 2;
    private static final int STATE_PICK_OPTION = 3;
    private static final int STATE_COMMAND = 4;
    private static final int STATE_ABORT_VOICE = 5;
    private static final int STATE_COMPLETE_VOICE = 6;
    private static final int STATE_DONE = 7;

    private Intent mStartIntent;
    private View mContentView;
    private AssistVisualizer mAssistVisualizer;
    private View mTopContent;
    private View mBottomContent;
    private TextView mText;
    private Button mTreeButton;
    private Button mTextButton;
    private Button mStartButton;
    private CheckBox mOptionsCheck;
    private View mOptionsContainer;
    private CheckBox mDisallowAssist;
    private CheckBox mDisallowScreenshot;
    private TextView mOptionsText;
    private ImageView mScreenshot;
    private ImageView mFullScreenshot;
    private Button mConfirmButton;
    private Button mCompleteButton;
    private Button mAbortButton;

    private AssistStructure mAssistStructure;

    private int mState = STATE_IDLE;
    private VoiceInteractor.PickOptionRequest.Option[] mPendingOptions;
    private CharSequence mPendingPrompt;
    private Request mPendingRequest;
    private int mCurrentTask = -1;
    private int mShowFlags;

    MainInteractionSession(Context context) {
        super(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.d(TAG, "onCreate");

        ActivityManager am = getContext().getSystemService(ActivityManager.class);
        am.setWatchHeapLimit(40 * 1024 * 1024);
    }

    @Override
    public void onShow(Bundle args, int showFlags) {
        super.onShow(args, showFlags);
        AppLogger.i(TAG, "onShow: flags=0x%s, args=%s", Integer.toHexString(showFlags), args);

        // new session start
        mShowFlags = showFlags;
        mStartIntent = args != null ? (Intent) args.getParcelable("intent") : null;
        AppLogger.d(TAG, "args intent: %s", mStartIntent);
        if (mStartIntent == null) {
            mStartIntent = new Intent(getContext(), VoiceInteractionActivity.class);
        }

        // reset previous data
        if (mAssistVisualizer != null) {
            mAssistVisualizer.clearAssistData();
        }
        onHandleScreenshot(null);
        mState = STATE_IDLE;
        updateState();
        refreshOptions();
    }

    @Override
    public void onHide() {
        super.onHide();
        AppLogger.d(TAG, "onHide");
        if (mAssistVisualizer != null) {
            mAssistVisualizer.clearAssistData();
        }
        mState = STATE_DONE;
        updateState();
    }

    @SuppressLint("SetTextI18n")
    private void refreshOptions() {
        if (mOptionsContainer != null) {
            if (mOptionsCheck.isChecked()) {
                mOptionsContainer.setVisibility(View.VISIBLE);
                int flags = getDisabledShowContext();
                mDisallowAssist.setChecked((flags & SHOW_WITH_ASSIST) != 0);
                mDisallowScreenshot.setChecked((flags & SHOW_WITH_SCREENSHOT) != 0);
                int disabled = getUserDisabledShowContext();
                mOptionsText.setText("Disabled: 0x" + Integer.toHexString(disabled));
            } else {
                mOptionsContainer.setVisibility(View.GONE);
            }
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateContentView() {
        AppLogger.d(TAG, "onCreateContentView");
        mContentView = getLayoutInflater().inflate(R.layout.voice_interaction_session, null);
        mAssistVisualizer = (AssistVisualizer) mContentView.findViewById(R.id.assist_visualizer);
        if (mAssistStructure != null) {
            mAssistVisualizer.setAssistStructure(mAssistStructure);
        }
        mTopContent = mContentView.findViewById(R.id.top_content);
        mBottomContent = mContentView.findViewById(R.id.bottom_content);
        mText = (TextView) mContentView.findViewById(R.id.text);
        mTreeButton = (Button) mContentView.findViewById(R.id.do_tree);
        mTreeButton.setOnClickListener(this);
        mTextButton = (Button) mContentView.findViewById(R.id.do_text);
        mTextButton.setOnClickListener(this);
        mStartButton = (Button) mContentView.findViewById(R.id.start);
        mStartButton.setOnClickListener(this);
        mScreenshot = (ImageView) mContentView.findViewById(R.id.screenshot);
        mScreenshot.setOnClickListener(this);
        mFullScreenshot = (ImageView) mContentView.findViewById(R.id.full_screenshot);
        mOptionsCheck = (CheckBox) mContentView.findViewById(R.id.show_options);
        mOptionsCheck.setOnClickListener(this);
        mOptionsContainer = mContentView.findViewById(R.id.options);
        mDisallowAssist = (CheckBox) mContentView.findViewById(R.id.disallow_structure);
        mDisallowAssist.setOnClickListener(this);
        mDisallowScreenshot = (CheckBox) mContentView.findViewById(R.id.disallow_screenshot);
        mDisallowScreenshot.setOnClickListener(this);
        mOptionsText = (TextView) mContentView.findViewById(R.id.options_text);
        mConfirmButton = (Button) mContentView.findViewById(R.id.confirm);
        mConfirmButton.setOnClickListener(this);
        mCompleteButton = (Button) mContentView.findViewById(R.id.complete);
        mCompleteButton.setOnClickListener(this);
        mAbortButton = (Button) mContentView.findViewById(R.id.abort);
        mAbortButton.setOnClickListener(this);
        refreshOptions();
        return mContentView;
    }

    @Override
    public void onHandleAssist(Bundle data, AssistStructure structure, AssistContent content) {
        AppLogger.d(TAG, "onHandleAssist, data: %s", data);
        mAssistStructure = structure;
        if (mAssistStructure != null) {
            if (mAssistVisualizer != null) {
                mAssistVisualizer.setAssistStructure(mAssistStructure);
            }
        } else {
            if (mAssistVisualizer != null) {
                mAssistVisualizer.clearAssistData();
            }
        }

        if (content != null) {
            AppLogger.d(TAG, "assist intent: %s", content.getIntent());
            AppLogger.d(TAG, "assist content: %s", content.getStructuredData());
            AppLogger.d(TAG, "assist clip data: %s", content.getClipData());
        }
        if (data != null) {
            Uri referrer = data.getParcelable(Intent.EXTRA_REFERRER);
            if (referrer != null) {
                AppLogger.d(TAG, "Referrer: " + referrer);
            }
        }
    }

    @Override
    public void onHandleAssistSecondary(final Bundle data, final AssistStructure structure,
            final AssistContent content, int index, int count) {
        AppLogger.d(TAG, "onHandleAssistSecondary, %d/%d, delay a few seconds",  index, count);
        mContentView.postDelayed(new Runnable() {
            public void run() {
                onHandleAssist(data, structure, content);
            }
        }, 2000 * index);
    }

    @Override
    public void onHandleScreenshot(Bitmap screenshot) {
        AppLogger.d(TAG, "onHandleScreenshot: %s", screenshot);
        if (screenshot != null) {
            mScreenshot.setImageBitmap(screenshot);
            mScreenshot.setAdjustViewBounds(true);
            mScreenshot.setMaxWidth(screenshot.getWidth() / 3);
            mScreenshot.setMaxHeight(screenshot.getHeight() / 3);
            mFullScreenshot.setImageBitmap(screenshot);
        } else {
            mScreenshot.setImageDrawable(null);
            mFullScreenshot.setImageDrawable(null);
        }
    }

    private void updateState() {
        if (mState == STATE_IDLE) {
            mTopContent.setVisibility(View.VISIBLE);
            mBottomContent.setVisibility(View.GONE);
            mAssistVisualizer.setVisibility(View.VISIBLE);
        } else if (mState == STATE_DONE) {
            mTopContent.setVisibility(View.GONE);
            mBottomContent.setVisibility(View.GONE);
            mAssistVisualizer.setVisibility(View.GONE);
        } else {
            mTopContent.setVisibility(View.GONE);
            mBottomContent.setVisibility(View.VISIBLE);
            mAssistVisualizer.setVisibility(View.GONE);
        }
        mStartButton.setEnabled(mState == STATE_IDLE);
        mConfirmButton.setEnabled(mState == STATE_CONFIRM || mState == STATE_PICK_OPTION
                || mState == STATE_COMMAND);
        mAbortButton.setEnabled(mState == STATE_ABORT_VOICE);
        mCompleteButton.setEnabled(mState == STATE_COMPLETE_VOICE);
    }

    public void onClick(View v) {
        if (v == mTreeButton) {
            if (mAssistVisualizer != null) {
                mAssistVisualizer.logTree();
            }
        } else if (v == mTextButton) {
            if (mAssistVisualizer != null) {
                mAssistVisualizer.logText();
            }
        } else if (v == mOptionsCheck) {
            refreshOptions();
        } else if (v == mDisallowAssist) {
            int flags = getDisabledShowContext();
            if (mDisallowAssist.isChecked()) {
                flags |= SHOW_WITH_ASSIST;
            } else {
                flags &= ~SHOW_WITH_ASSIST;
            }
            setDisabledShowContext(flags);
        } else if (v == mDisallowScreenshot) {
            int flags = getDisabledShowContext();
            if (mDisallowScreenshot.isChecked()) {
                flags |= SHOW_WITH_SCREENSHOT;
            } else {
                flags &= ~SHOW_WITH_SCREENSHOT;
            }
            setDisabledShowContext(flags);
        } else if (v == mStartButton) {
            mState = STATE_LAUNCHING;
            updateState();
            AppLogger.d(TAG, "start voice activity: %s", mStartIntent);
            startVoiceActivity(mStartIntent);
        } else if (v == mConfirmButton) {
            if (mPendingRequest instanceof ConfirmationRequest) {
                ((ConfirmationRequest) mPendingRequest).sendConfirmationResult(true, null);
                mPendingRequest = null;
                mState = STATE_LAUNCHING;
            } else if (mPendingRequest instanceof PickOptionRequest) {
                PickOptionRequest pick = (PickOptionRequest) mPendingRequest;
                int numReturn = mPendingOptions.length / 2;
                if (numReturn <= 0) {
                    numReturn = 1;
                }
                VoiceInteractor.PickOptionRequest.Option[] picked
                        = new VoiceInteractor.PickOptionRequest.Option[numReturn];
                for (int i = 0; i < picked.length; i++) {
                    picked[i] = mPendingOptions[i * 2];
                }
                mPendingOptions = picked;
                if (picked.length <= 1) {
                    pick.sendPickOptionResult(picked, null);
                    mPendingRequest = null;
                    mState = STATE_LAUNCHING;
                } else {
                    pick.sendIntermediatePickOptionResult(picked, null);
                    updatePickText();
                }
            } else if (mPendingRequest instanceof CommandRequest) {
                Bundle result = new Bundle();
                result.putString("key", "a result!");
                ((CommandRequest) mPendingRequest).sendResult(result);
                mPendingRequest = null;
                mState = STATE_LAUNCHING;
            }
        } else if (v == mAbortButton && mPendingRequest instanceof AbortVoiceRequest) {
            ((AbortVoiceRequest) mPendingRequest).sendAbortResult(null);
            mPendingRequest = null;
        } else if (v == mCompleteButton && mPendingRequest instanceof CompleteVoiceRequest) {
            ((CompleteVoiceRequest) mPendingRequest).sendCompleteResult(null);
            mPendingRequest = null;
        } else if (v == mScreenshot) {
            if (mFullScreenshot.getVisibility() != View.VISIBLE) {
                mFullScreenshot.setVisibility(View.VISIBLE);
            } else {
                mFullScreenshot.setVisibility(View.INVISIBLE);
            }
        }
        updateState();
    }

    @Override
    public void onComputeInsets(Insets outInsets) {
        AppLogger.d(TAG, "onComputeInsets");
        super.onComputeInsets(outInsets);
        if (mState != STATE_IDLE) {
            outInsets.contentInsets.top = mBottomContent.getTop();
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_CONTENT;
        } else if ((mShowFlags & SHOW_SOURCE_ACTIVITY) != 0) {
            outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_CONTENT;
        }
    }

    @Override
    public void onTaskStarted(Intent intent, int taskId) {
        AppLogger.d(TAG, "onTaskStarted, intent=%s, taskId=%d", intent, taskId);
        super.onTaskStarted(intent, taskId);
        mCurrentTask = taskId;
    }

    @Override
    public void onTaskFinished(Intent intent, int taskId) {
        AppLogger.d(TAG, "onTaskFinished, intent=%s, taskId=%d", intent, taskId);
        super.onTaskFinished(intent, taskId);
        if (mCurrentTask == taskId) {
            mCurrentTask = -1;
        }
    }

    @Override
    public void onLockscreenShown() {
        AppLogger.d(TAG, "onLockscreenShown");
        if (mCurrentTask < 0) {
            hide();
        }
    }

    @Override
    public boolean[] onGetSupportedCommands(String[] commands) {
        AppLogger.d(TAG, "onGetSupportedCommands: " + Arrays.toString(commands));
        boolean[] res = new boolean[commands.length];
        for (int i = 0; i < commands.length; i++) {
            if ("com.android.test.voiceinteraction.COMMAND".equals(commands[i])) {
                res[i] = true;
            }
        }
        return res;
    }

    private void setPrompt(VoiceInteractor.Prompt prompt) {
        if (prompt == null) {
            mText.setText("(null)");
            mPendingPrompt = "";
        } else {
            mText.setText(prompt.getVisualPrompt());
            mPendingPrompt = prompt.getVisualPrompt();
        }
    }

    @Override
    public void onRequestConfirmation(ConfirmationRequest request) {
        AppLogger.i(TAG, "onConfirm: prompt=" + request.getVoicePrompt() + " extras="
                + request.getExtras());
        setPrompt(request.getVoicePrompt());
        mConfirmButton.setText("Confirm");
        mPendingRequest = request;
        mState = STATE_CONFIRM;
        updateState();
    }

    @Override
    public void onRequestPickOption(PickOptionRequest request) {
        AppLogger.i(TAG, "onPickOption: prompt=" + request.getVoicePrompt() + " options="
                + request.getOptions() + " extras=" + request.getExtras());
        mConfirmButton.setText("Pick Option");
        mPendingRequest = request;
        setPrompt(request.getVoicePrompt());
        mPendingOptions = request.getOptions();
        mState = STATE_PICK_OPTION;
        updatePickText();
        updateState();
    }

    private void updatePickText() {
        StringBuilder sb = new StringBuilder();
        sb.append(mPendingPrompt);
        sb.append(": ");
        for (int i = 0; i < mPendingOptions.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(mPendingOptions[i].getLabel());
        }
        mText.setText(sb.toString());
    }

    @Override
    public void onRequestCompleteVoice(CompleteVoiceRequest request) {
        AppLogger.i(TAG, "onCompleteVoice: message=" + request.getVoicePrompt() + " extras="
                + request.getExtras());
        setPrompt(request.getVoicePrompt());
        mPendingRequest = request;
        mState = STATE_COMPLETE_VOICE;
        updateState();
    }

    @Override
    public void onRequestAbortVoice(AbortVoiceRequest request) {
        AppLogger.d(TAG, "onRequestAbortVoice: message=" + request.getVoicePrompt() + " extras="
                + request.getExtras());
        setPrompt(request.getVoicePrompt());
        mPendingRequest = request;
        mState = STATE_ABORT_VOICE;
        updateState();
    }

    @Override
    public void onRequestCommand(CommandRequest request) {
        Bundle extras = request.getExtras();
        if (extras != null) {
            extras.getString("arg");
        }
        AppLogger.d(TAG, "onRequestCommand: command: %s, extras: %s",
                request.getCommand(), extras);
        mText.setText("Command: " + request.getCommand() + ", " + extras);
        mConfirmButton.setText("Finish Command");
        mPendingRequest = request;
        mState = STATE_COMMAND;
        updateState();
    }

    @Override
    public void onCancelRequest(Request request) {
        AppLogger.d(TAG, "onCancelRequest, callingPackage: %s, isActive: %s",
                request.getCallingPackage(), request.isActive());
        if (mPendingRequest == request) {
            mPendingRequest = null;
            mState = STATE_LAUNCHING;
            updateState();
        }
        request.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLogger.d(TAG, "onDestroy");
    }

    @Override
    public void onAssistStructureFailure(Throwable failure) {
        super.onAssistStructureFailure(failure);
        AppLogger.w(TAG, "onAssistStructureFailure", failure);
    }
}
