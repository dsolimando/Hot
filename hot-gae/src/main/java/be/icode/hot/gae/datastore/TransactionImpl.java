package be.icode.hot.gae.datastore;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.google.appengine.api.datastore.Transaction;

public class TransactionImpl implements Transaction {

	@Override
	public void commit() {
	}

	@Override
	public Future<Void> commitAsync() {
		return new FutureTask<Void>(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				return null;
			}
		});
	}

	@Override
	public String getApp() {
		return "";
	}

	@Override
	public String getId() {
		return "";
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public void rollback() {
	}

	@Override
	public Future<Void> rollbackAsync() {
		return new FutureTask<Void>(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				return null;
			}
		});
	}
}
