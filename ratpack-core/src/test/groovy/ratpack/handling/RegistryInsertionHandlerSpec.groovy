/*
 * Copyright 2013 the original author or authors.
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

package ratpack.handling

import groovy.transform.TupleConstructor
import ratpack.error.ServerErrorHandler
import ratpack.error.internal.PrintingServerErrorHandler
import ratpack.registry.NotInRegistryException
import ratpack.test.internal.RatpackGroovyDslSpec

class RegistryInsertionHandlerSpec extends RatpackGroovyDslSpec {

  def setup() {
    modules {
      bind ServerErrorHandler, new PrintingServerErrorHandler()
    }
  }

  interface Thing {
    String getValue()
  }

  @TupleConstructor
  static class ThingImpl implements Thing {
    @SuppressWarnings("GrFinalVariableAccess")
    final String value
  }

  def "can register for downstream with next"() {
    when:
    app {
      handlers {
        prefix("foo") {
          handler {
            next(Thing, new ThingImpl("foo"))
          }
          get {
            render get(Thing).value
          }
          prefix("bar") {
            get {
              render get(Thing).value + ":bar"
            }
          }
        }
        get {
          get(Thing)
        }
      }
    }

    then:
    getText("foo") == "foo"
    getText("foo/bar") == "foo:bar"
    getText().startsWith "$NotInRegistryException.name: No object for type '$Thing.name'"
    response.statusCode == 500
  }
}
