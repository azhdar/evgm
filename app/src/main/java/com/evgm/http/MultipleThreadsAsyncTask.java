package com.evgm.http;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;

public abstract class MultipleThreadsAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

	public MultipleThreadsAsyncTask() {
		if (Looper.myLooper() == null)
			Looper.prepare();
	}

	public AsyncTask<Params, Progress, Result> executeOnMultiBackgroundThread(Params... params) {
		if (Build.VERSION.SDK_INT >= 11)
			return executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);// execute this on the executor with multiple background thread
		else
			return execute(params);// before API 11, execute on multiple background thread, after 11 the new implementation provided by Android executes in a single background thread
	}

}
