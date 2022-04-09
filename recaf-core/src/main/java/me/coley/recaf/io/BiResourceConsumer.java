package me.coley.recaf.io;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public interface BiResourceConsumer<E> {

	void accept(E data, ByteContainer container) throws IOException;

	/**
	 * Creates new {@link BiConsumer} that can be run on a
	 * different thread.
	 *
	 * @param c
	 * 		Consumer to wrap.
	 * @param executor
	 * 		Executor to run task in.
	 *
	 * @return Wrapped consumer.
	 */
	static <E> BiResourceConsumer<E> async(BiResourceConsumer<? super E> c, Executor executor) {
		return (t, u) -> executor.execute(() -> {
			try {
				c.accept(t, u);
			} catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		});
	}

}
