/* UnsafeString String↔char[] conversion library
   Copyright 2019 by Michał Nazarewicz <mina86@mina86.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.mina86.unsafe;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.ObjLongConsumer;

public final class UnsafeStringBenchmark {
	public static void main(final String[] args) {
		boolean result = true;
		result = new UnsafeStringBenchmark(
			UnsafeStringImpl.makeSafe()
		).run("safe") && result;
		result = new UnsafeStringBenchmark(
			UnsafeStringImpl.makeUnsafeMaybe()
		).run("unsafe") && result;
		if (!result) {
			System.exit(1);
		}
	}

	private final UnsafeStringImpl impl;

	private UnsafeStringBenchmark(final UnsafeStringImpl impl) {
		this.impl = impl;
	}

	private boolean run(final String name) {
		System.err.printf("Testing %s implementation: ", name);
		if (impl == null) {
			System.err.printf("unsupported by the run-time%n");
			return false;
		}

		/* Run tests */
		final int errors = runTests();
		if (errors != 0) {
			System.err.printf(" done with %d FAILURES%n", errors);
			return false;
		}
		System.err.println(" done, all ok");

		/* Run benchmarks (but only if tests passed). */
		runBenchmarks(name +  "::fromChars", char[]::clone, (chars, i) -> {
			do {
				impl.fromChars(chars);
			} while (--i != 0);
		});
		runBenchmarks(name + "::toChars", String::new, (str, i) -> {
			do {
				impl.toChars(str);
			} while (--i != 0);
		});
		return true;
	}

	private int check(final boolean result) {
		System.err.print(result ? '.' : '!');
		return result ? 0 : 1;
	}

	private static final char[][] TEST_CHARS = new char[][]{
		new char[]{},
		new char[]{' '},
		new char[]{'f', 'o', 'o'},
		new char[]{'ż', 'ó', 'ł', 'w'},
		newCharArray(       10),
		newCharArray(       33),
		newCharArray(      100),
		newCharArray(   10_000),
		newCharArray(1_000_000),
	};

	private int runTests() {
		int errors = 0;
		for (char[] chars : TEST_CHARS) {
			final String want = new String(chars);
			final String got = impl.fromChars(chars.clone());
			errors += check(want.equals(got));
			errors += check(want.hashCode() == got.hashCode());
			errors += check(Arrays.equals(impl.toChars(got), chars));
		}
		return errors;
	}

	private <T> void runBenchmarks(final String name,
				       final Function<char[], T> prepare,
				       final ObjLongConsumer<T> benchmark) {
		for (char[] chars : TEST_CHARS) {
			System.err.printf(" + %17s/%-7d: ", name, chars.length);
			System.err.flush();

			final T arg = prepare.apply(chars);
			benchmark.accept(arg, 1000);  /* Warm-up */
			runBenchmark(arg, benchmark);
		}
	}

	private <T> void runBenchmark(final T arg,
				      final ObjLongConsumer<T> benchmark) {
		final long oneSecond = 1_000_000_000;
		final BigInteger bigSecond = BigInteger.valueOf(oneSecond);

		long iterations = 1000;
		long elapsed = 0;
		for (;;) {
			final String itStr = String.format("%10d", iterations);
			System.err.print(itStr);

			final long start = System.nanoTime();
			benchmark.accept(arg, iterations);
			elapsed = System.nanoTime() - start;

			for (int i = itStr.length(); i > 0; --i) {
				System.err.print('\b');
			}

			if (elapsed < 1_000) { /* protect against elapsed == 0 */
				iterations *= 1000;
			} else if (elapsed < oneSecond) {
				final long it = BigInteger
					.valueOf(iterations)
					.multiply(bigSecond)
					.divide(BigInteger.valueOf(elapsed))
					.longValue();
				iterations = Math.max(3 * iterations, it);
			} else {
				break;
			}
		}

		System.err.printf("%10d ops in %9d ns: %g ns/op%n",
		                  iterations, elapsed,
		                  (double) elapsed / (double) iterations);
	}

	private static char[] newCharArray(final int length) {
		final char[] chars = new char[length];
		for (int i = 0; i < length; ++i) {
			chars[i] = (char)(i & 127);
		}
		return chars;
	}
}
