package me.coley.recaf.util.threading;

import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executor service that counts the current
 * amount of running tasks.
 *
 * @author xDark
 */
public final class CountingExecutor implements ExecutorService {
	private final AtomicBoolean shutdown = new AtomicBoolean();
	private final Phaser phaser;
	private final ExecutorService service;

	public CountingExecutor(ExecutorService service) {
		this.service = service;
		phaser = new Phaser(1);
	}

	@Override
	public void shutdown() {
		if (shutdown.compareAndSet(false, true)) {
			phaser.arriveAndDeregister();
		}
	}

	@Override
	public List<Runnable> shutdownNow() {
		throw new UnsupportedOperationException("Use shutdown method instead");
	}

	@Override
	public boolean isShutdown() {
		return shutdown.get();
	}

	@Override
	public boolean isTerminated() {
		return phaser.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		try {
			return phaser.awaitAdvanceInterruptibly(0, timeout, unit) == 0;
		} catch (TimeoutException ex) {
			return false;
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		phaser.register();
		return service.submit(wrap(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		phaser.register();
		return service.submit(wrap(task), result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		phaser.register();
		return service.submit(wrap(task));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		phaser.bulkRegister(tasks.size());
		return service.invokeAll(Collections2.transform(tasks, this::wrap));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
		phaser.bulkRegister(tasks.size());
		return service.invokeAll(Collections2.transform(tasks, this::wrap), timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void execute(Runnable command) {
		phaser.register();
		service.execute(wrap(command));
	}

	private Runnable wrap(Runnable r) {
		return () -> {
			try {
				r.run();
			} finally {
				phaser.arriveAndDeregister();
			}
		};
	}

	private <T> Callable<T> wrap(Callable<T> c) {
		return () -> {
			try {
				return c.call();
			} finally {
				phaser.arriveAndDeregister();
			}
		};
	}
}
