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
package px.internal.op

import px.Pollable
import java.util.ArrayDeque

class FlatMap<U, D>(val upstream: Pollable<U>, val func: (U) -> Pollable<D>) : Pollable<D>() {
  override fun start() = Operator(upstream.start(), func)

  class Operator<U, D>(val upstream: Machine<U>, val func: (U) -> Pollable<D>) : Machine<D> {
    val children = ArrayDeque<Machine<D>>()

    override fun poll(): D? {
      while (true) {
        upstream.poll()?.let {
          children += func.invoke(it).start()
        } ?: break
      }

      val iterator = children.iterator()
      while (iterator.hasNext()) {
        iterator.next().let { child ->
          if (child.isComplete()) {
            iterator.remove()
          } else {
            child.poll()?.let {
              if (child.isComplete()) {
                iterator.remove()
              }
              return it
            }
          }
        }
      }
      return null
    }

    override fun isComplete() = children.all { it.isComplete() } && upstream.isComplete()

    override fun close() {
      upstream.close()
      children.forEach { it.close() }
    }
  }
}
