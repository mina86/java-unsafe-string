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

public enum UnsafeString {
	;

	private static final UnsafeStringImpl impl =
		UnsafeStringImpl.choose();

	public static String fromChars(final char[] chars) {
		return impl.fromChars(chars);
	}

	public static char[] toChars(final String string) {
		return impl.toChars(string);
	}
}
