package me.ycdev.android.demo.assist.voice;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.service.voice.VoiceInteractionService;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import me.ycdev.android.arch.utils.AppLogger;
import me.ycdev.android.demo.assist.MainActivity;
import me.ycdev.android.demo.assist.R;

@RequiresApi(api = Build.VERSION_CODES.M)
public class VoiceInteractionActivity extends AppCompatActivity implements View.OnClickListener {
    static final String TAG = "TestInteractionActivity";

    static final String REQUEST_ABORT = "abort";
    static final String REQUEST_COMPLETE = "complete";
    static final String REQUEST_COMMAND = "command";
    static final String REQUEST_PICK = "pick";
    static final String REQUEST_CONFIRM = "confirm";

    VoiceInteractor mInteractor;
    VoiceInteractor.Request mCurrentRequest = null;
    TextView mLog;
    Button mAirplaneButton;
    Button mAbortButton;
    Button mCompleteButton;
    Button mCommandButton;
    Button mPickButton;
    Button mJumpOutButton;
    Button mCancelButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (!isVoiceInteraction()) {
            Log.w(TAG, "Not running as a voice interaction!");
            finish();
            return;
        }

        if (!VoiceInteractionService.isActiveService(this,
                new ComponentName(this, MainInteractionService.class))) {
            Log.w(TAG, "Not current voice interactor!");
            finish();
            return;
        }

        setContentView(R.layout.activity_voice_interaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLog = (TextView) findViewById(R.id.log);
        mAirplaneButton = (Button) findViewById(R.id.airplane);
        mAirplaneButton.setOnClickListener(this);
        mAbortButton = (Button) findViewById(R.id.abort);
        mAbortButton.setOnClickListener(this);
        mCompleteButton = (Button) findViewById(R.id.complete);
        mCompleteButton.setOnClickListener(this);
        mCommandButton = (Button) findViewById(R.id.command);
        mCommandButton.setOnClickListener(this);
        mPickButton = (Button) findViewById(R.id.pick);
        mPickButton.setOnClickListener(this);
        mJumpOutButton = (Button) findViewById(R.id.jump);
        mJumpOutButton.setOnClickListener(this);
        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(this);

        mInteractor = getVoiceInteractor();

        VoiceInteractor.Request[] active = mInteractor.getActiveRequests();
        for (int i = 0; i < active.length; i++) {
            Log.i(TAG, "Active #" + i + " / " + active[i].getName() + ": " + active[i]);
        }

        mCurrentRequest = mInteractor.getActiveRequest(REQUEST_CONFIRM);
        if (mCurrentRequest == null) {
            mCurrentRequest = new VoiceInteractor.ConfirmationRequest(
                    new VoiceInteractor.Prompt("This is a confirmation"), null) {
                @Override
                public void onCancel() {
                    Log.i(TAG, "Canceled!");
                    getActivity().finish();
                }

                @Override
                public void onConfirmationResult(boolean confirmed, Bundle result) {
                    Log.i(TAG, "Confirmation result: confirmed=" + confirmed + " result=" + result);
                    getActivity().finish();
                }
            };
            mInteractor.submitRequest(mCurrentRequest, REQUEST_CONFIRM);
            String[] cmds = new String[]{
                    "com.android.test.voiceinteraction.COMMAND",
                    "com.example.foo.bar"
            };
            boolean sup[] = mInteractor.supportsCommands(cmds);
            for (int i = 0; i < cmds.length; i++) {
                mLog.append(cmds[i] + ": " + (sup[i] ? "SUPPORTED" : "NOT SUPPORTED") + "\n");
            }
        } else {
            Log.i(TAG, "Restarting with active confirmation: " + mCurrentRequest);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == mAirplaneButton) {
            Intent intent = new Intent(Settings.ACTION_VOICE_CONTROL_AIRPLANE_MODE);
            intent.addCategory(Intent.CATEGORY_VOICE);
            intent.putExtra(Settings.EXTRA_AIRPLANE_MODE_ENABLED, true);
            startActivity(intent);
        } else if (v == mAbortButton) {
            VoiceInteractor.AbortVoiceRequest req = new TestAbortVoiceRequest();
            mInteractor.submitRequest(req, REQUEST_ABORT);
        } else if (v == mCompleteButton) {
            VoiceInteractor.CompleteVoiceRequest req = new TestCompleteVoiceRequest();
            mInteractor.submitRequest(req, REQUEST_COMPLETE);
        } else if (v == mCommandButton) {
            Bundle args = new Bundle();
            args.putString("test_key1", "this is a string");
            VoiceInteractor.CommandRequest req = new TestCommandRequest(args);
            mInteractor.submitRequest(req, REQUEST_COMMAND);
        } else if (v == mPickButton) {
            VoiceInteractor.PickOptionRequest.Option[] options =
                    new VoiceInteractor.PickOptionRequest.Option[5];
            options[0] = new VoiceInteractor.PickOptionRequest.Option("One", 0);
            options[1] = new VoiceInteractor.PickOptionRequest.Option("Two", 1);
            options[2] = new VoiceInteractor.PickOptionRequest.Option("Three", 2);
            options[3] = new VoiceInteractor.PickOptionRequest.Option("Four", 3);
            options[4] = new VoiceInteractor.PickOptionRequest.Option("Five", 4);
            VoiceInteractor.PickOptionRequest req = new TestPickOption(options);
            mInteractor.submitRequest(req, REQUEST_PICK);
        } else if (v == mJumpOutButton) {
            Log.i(TAG, "Jump out");
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(new ComponentName(this, MainActivity.class));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (v == mCancelButton && mCurrentRequest != null) {
            Log.i(TAG, "Cancel request");
            mCurrentRequest.cancel();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private static void appendLog(Activity activity, String msg) {
        ((VoiceInteractionActivity)activity).mLog.append(msg + "\n");
    }

    private static class TestAbortVoiceRequest extends VoiceInteractor.AbortVoiceRequest {
        TestAbortVoiceRequest() {
            super(new VoiceInteractor.Prompt("Abort voice"), null);
        }

        @Override
        public void onCancel() {
            String msg = "Abort#onCancel()";
            AppLogger.d(TAG, msg);
            appendLog(getActivity(), msg);
        }

        @Override
        public void onAbortResult(Bundle result) {
            String msg = "Abort#onAbortResult(), result=" + result;
            AppLogger.d(TAG, msg);
            appendLog(getActivity(), msg);
            getActivity().finish();
        }
    }

    private static class TestCompleteVoiceRequest extends VoiceInteractor.CompleteVoiceRequest {
        TestCompleteVoiceRequest() {
            super(new VoiceInteractor.Prompt("Complete voice"), null);
        }

        @Override
        public void onCancel() {
            String msg = "Complete#onCancel";
            AppLogger.d(TAG, msg);
            appendLog(getActivity(), msg);
        }

        @Override
        public void onCompleteResult(Bundle result) {
            String msg = "Complete#onCompleteResult(), result=" + result;
            AppLogger.d(TAG, msg);
            appendLog(getActivity(), msg);
            getActivity().finish();
        }
    }

    private static class TestCommandRequest extends VoiceInteractor.CommandRequest {
        TestCommandRequest(Bundle args) {
            super(VoiceCommands.TEST, args);
        }

        @Override
        public void onCancel() {
            String msg = "Command#onCancel";
            AppLogger.d(TAG, msg);
            appendLog(getActivity(), msg);
        }

        @Override
        public void onCommandResult(boolean finished, Bundle result) {
            // TODO
            String msg = "Command#onCommandResult, finished=" + finished + " result=" + result;
            Log.i(TAG, msg);
            StringBuilder sb = new StringBuilder();
            if (finished) {
                sb.append("Command final result: ");
            } else {
                sb.append("Command intermediate result: ");
            }
            if (result != null) {
                result.getString("key");
            }
            sb.append(result);
            sb.append("\n");
            ((VoiceInteractionActivity) getActivity()).mLog.append(sb.toString());
        }

        static Bundle makeBundle(String arg) {
            Bundle b = new Bundle();
            b.putString("key", arg);
            return b;
        }
    }

    private static class TestPickOption extends VoiceInteractor.PickOptionRequest {
        public TestPickOption(Option[] options) {
            super(new VoiceInteractor.Prompt("Need to pick something"), options, null);
        }

        @Override
        public void onCancel() {
            Log.i(TAG, "Canceled!");
            ((VoiceInteractionActivity) getActivity()).mLog.append("Canceled pick\n");
        }

        @Override
        public void onPickOptionResult(boolean finished, Option[] selections, Bundle result) {
            Log.i(TAG, "Pick result: finished=" + finished + " selections=" + selections
                    + " result=" + result);
            StringBuilder sb = new StringBuilder();
            if (finished) {
                sb.append("Pick final result: ");
            } else {
                sb.append("Pick intermediate result: ");
            }
            for (int i = 0; i < selections.length; i++) {
                if (i >= 1) {
                    sb.append(", ");
                }
                sb.append(selections[i].getLabel());
            }
            sb.append("\n");
            ((VoiceInteractionActivity) getActivity()).mLog.append(sb.toString());
        }
    }
}
