package io.sece.vlc.rcvr;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.concurrent.CompletableFuture;


public class ConfirmationDialog extends DialogFragment {
    private static final String ARG_MESSAGE = "message";
    private static final String ARG_CANCELABLE = "cancelable";

    public CompletableFuture<Boolean> completed;

    public static ConfirmationDialog newInstance(String message, boolean cancelable) {
        ConfirmationDialog d = new ConfirmationDialog();
        d.completed = new CompletableFuture<>();

        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_CANCELABLE, cancelable);
        d.setArguments(args);
        return d;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();

        b.setMessage(args.getString(ARG_MESSAGE));
        b.setPositiveButton(android.R.string.ok, (dialog, which) -> completed.complete(true));
        if (args.getBoolean(ARG_CANCELABLE))
            b.setNegativeButton(android.R.string.cancel, (dialog, which) -> completed.complete(false));
        return b.create();
    }
}
