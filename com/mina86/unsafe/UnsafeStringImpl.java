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

import java.lang.reflect.Field;

abstract class UnsafeStringImpl {
	abstract String fromChars(final char[] chars);
	abstract char[] toChars(final String string);

	static UnsafeStringImpl makeUnsafeMaybe() {
		try {
			final Field field = String.class.getDeclaredField("value");
			field.setAccessible(true);
			/* Test if it works.  Besides checking if we have access
			   to the field, this also tests whether runtime uses
			   strings which share storage with substrings.
			   UnsafeImpl does not support such strings. */
			final String string = new String();
			final char[] chars = new char[]{'F', 'o', 'o'};
			field.set(string, chars);
			if (string.equals("Foo") && field.get(string) == chars) {
				return new UnsafeImpl(field);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	static UnsafeStringImpl makeSafe() {
		return new SafeImpl();
	}

	static UnsafeStringImpl choose() {
		final UnsafeStringImpl impl = makeUnsafeMaybe();
		return impl == null ? makeSafe() : impl;
	}

	private static final class SafeImpl extends UnsafeStringImpl {
		String fromChars(final char[] chars) {
			return new String(chars);
		}

		char[] toChars(final String string) {
			return string.toCharArray();
		}
	}

	private static final class UnsafeImpl extends UnsafeStringImpl {
		private final Field field;

		UnsafeImpl(final Field field) {
			this.field = field;
		}

		String fromChars(final char[] chars) {
			try {
				final String string = new String();
				field.set(string, chars);
				return string;
			} catch (Exception ex) {
				/* We should never be here */
				return new String(chars);
			}
		}

		char[] toChars(final String string) {
			try {
				return (char[]) field.get(string);
			} catch (Exception ex) {
				/* We should never be here */
				return string.toCharArray();
			}
		}
	}
}
