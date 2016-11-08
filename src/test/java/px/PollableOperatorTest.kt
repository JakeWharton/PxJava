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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PollableOperatorTest {
  @Test fun map() {
    Pollable.just("Hello")
        .map { "World" }
        .start()
        .use {
          assertEquals("World", it.poll())
          assertTrue(it.isComplete())
        }
  }

  @Test fun flatMap() {
    Pollable.just("Hello")
        .flatMap { Pollable.from(*it.toCharArray().toTypedArray()) }
        .start()
        .use {
          assertEquals('H', it.poll())
          assertEquals('e', it.poll())
          assertEquals('l', it.poll())
          assertEquals('l', it.poll())
          assertEquals('o', it.poll())
          assertTrue(it.isComplete())
        }
  }

  @Test fun filter() {
    Pollable.from(1, 2, 3, 4, 5, 6)
        .filter { it % 2 == 0 }
        .start()
        .use {
          assertNull(it.poll())
          assertEquals(2, it.poll())
          assertNull(it.poll())
          assertEquals(4, it.poll())
          assertNull(it.poll())
          assertEquals(6, it.poll())
          assertTrue(it.isComplete())
        }
  }
}
