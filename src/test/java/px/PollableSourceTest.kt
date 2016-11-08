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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import px.Pollable.Emitter
import java.util.concurrent.atomic.AtomicReference

class PollableSourceTest {
  @Test fun empty() {
    val empty = Pollable.empty<Any>()
    empty.start().use {
      assertTrue(it.isComplete())
    }
  }

  @Test fun never() {
    val never = Pollable.never<Any>()
    val machine = never.start()
    machine.use {
      assertFalse(it.isComplete())
      assertNull(it.poll())
    }
  }

  @Test fun just() {
    val just = Pollable.just("Hello")
    just.start().use {
      assertFalse(it.isComplete())
      assertEquals("Hello", it.poll())
      assertTrue(it.isComplete())
    }
  }

  @Test fun from() {
    val from = Pollable.from("Hello", "World")
    from.start().use {
      assertFalse(it.isComplete())
      assertEquals("Hello", it.poll())
      assertFalse(it.isComplete())
      assertEquals("World", it.poll())
      assertTrue(it.isComplete())
    }
  }

  @Test fun error() {
    val exception = RuntimeException("Oops!")
    val error = Pollable.error<Any>(exception)
    error.start().use {
      assertFalse(it.isComplete())
      try {
        it.poll()
        fail()
      } catch (t: Throwable) {
        assertSame(exception, t)
      }
    }
  }

  @Test fun defer() {
    val box = AtomicReference<String>("Hello")
    val defer = Pollable.defer { Pollable.just(box.get()) }
    box.set("World")
    defer.start().use {
      assertFalse(it.isComplete())
      assertEquals("World", it.poll())
      assertTrue(it.isComplete())
    }
  }

  @Test fun create() {
    val emitter = AtomicReference<Emitter<String>>()
    val create = Pollable.create<String> { emitter.set(it) }
    create.start().use {
      assertFalse(it.isComplete())
      assertNull(it.poll())
      emitter.get().item("Hello")
      assertEquals("Hello", it.poll())
      assertFalse(it.isComplete())
      emitter.get().complete()
      assertNull(it.poll())
      assertTrue(it.isComplete())
    }
  }
}
