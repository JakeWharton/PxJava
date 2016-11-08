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
package px.internal.source

import px.Pollable
import java.util.ArrayDeque

class Create<T>(val func: (Emitter<T>) -> Unit) : Pollable<T>() {
  override fun start(): Machine<T> {
    val impl = Implementation<T>()
    func.invoke(impl)
    return impl
  }

  class Implementation<T>() : Machine<T>, Emitter<T> {
    object Complete
    class Error(val t: Throwable)

    var closed = false
    var closeAction: (() -> Unit)? = null
    val items = ArrayDeque<Any>()

    override fun poll(): T? {
      if (closed) throw IllegalStateException("closed")
      items.pollFirst()?.let {
        @Suppress("UNCHECKED_CAST")
        when (it) {
          is Complete -> closed = true
          is Error -> throw it.t
          else -> return it as T
        }
      }
      return null
    }

    override fun isComplete() = closed

    override fun close() {
      closeAction?.invoke()
      closed = true
    }

    override fun item(item: T) {
      items += item
    }

    override fun complete() {
      items += Complete
    }

    override fun error(t: Throwable) {
      items += Error(t)
    }

    override fun setCloser(action: () -> Unit) {
      if (closeAction != null) throw IllegalStateException("Close action already set")
      closeAction = action
    }
  }
}