/*
 * Copyright 2016 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package px

import px.internal.op.Filter
import px.internal.op.FlatMap
import px.internal.op.Map
import px.internal.source.Create
import px.internal.source.Defer
import px.internal.source.Empty
import px.internal.source.FromArray
import px.internal.source.JustError
import px.internal.source.JustItem
import px.internal.source.Never
import java.io.Closeable

abstract class Pollable<T> {
  interface Machine<out T> : Closeable {
    fun poll(): T?
    fun isComplete(): Boolean
  }

  abstract fun start(): Machine<T>

  fun <O> map(func: (T) -> O) : Pollable<O> = Map(this, func)
  fun filter(func: (T) -> Boolean) : Pollable<T> = Filter(this, func)
  fun <O> flatMap(func: (T) -> Pollable<O>): Pollable<O> = FlatMap(this, func)

  companion object {
    @JvmStatic
    fun <T> just(item: T) : Pollable<T> = JustItem(item)
    @JvmStatic
    fun <T> defer(func: () -> Pollable<T>) = Defer(func)
    @JvmStatic
    fun <T> from(vararg items: T) : Pollable<T> = FromArray(items)
    @JvmStatic
    fun <T> create(func: (Emitter<T>) -> Unit) : Pollable<T> = Create(func)
    @JvmStatic
    fun <T> error(t: Throwable) : Pollable<T> = JustError(t)
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> empty() : Pollable<T> = Empty as Pollable<T>
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <T> never() : Pollable<T> = Never as Pollable<T>
  }

  interface Emitter<in T> {
    fun complete()
    fun item(item: T)
    fun error(t: Throwable)
    fun setCloser(action: () -> Unit)
  }
}
