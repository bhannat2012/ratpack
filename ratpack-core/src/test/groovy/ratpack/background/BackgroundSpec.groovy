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

package ratpack.background

import ratpack.error.internal.PrintingServerErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.test.internal.RatpackGroovyDslSpec

class BackgroundSpec extends RatpackGroovyDslSpec {

  def "can perform groovy background operations"() {
    when:
    def steps = []
    app {
      handlers {
        get {
          steps << "start"
          background {
            sleep 300
            steps << "operation"
            2
          }.then { Integer result ->
            steps << "then"
            response.send result.toString()
          }
          steps << "end"
        }
      }
    }

    then:
    text == "2"
    steps == ["start", "end", "operation", "then"]
  }

  def "by default errors during background operations are forwarded to server error handler"() {
    when:
    app {
      modules {
        bind ServerErrorHandler, PrintingServerErrorHandler
      }
      handlers {
        get {
          background {
            sleep 300
            throw new Exception("!")
          }.then {
            /* never called */
          }
        }
      }
    }

    then:
    text.startsWith(new Exception("!").toString())
    response.statusCode == 500
  }

  def "can use custom error handler"() {
    when:
    app {
      handlers {
        get {
          background {
            sleep 300
            throw new Exception("!")
          }.onError {
            response.status(210).send("error: $it.message")
          }.then {
            // never called
          }
        }
      }
    }

    then:
    text == "error: !"
    response.statusCode == 210
  }

  def "errors in custom error handlers are forwarded to the server error handler"() {
    when:
    app {
      modules {
        bind ServerErrorHandler, PrintingServerErrorHandler
      }
      handlers {
        get {
          background {
            throw new Exception("!")
          }.onError {
            throw new Exception("!!", it)
          }.then {
            /* never called */
          }
        }
      }
    }

    then:
    text.startsWith(new Exception("!!", new Exception("!")).toString())
    response.statusCode == 500
  }

  def "errors in success handlers are forwarded to the server error handler"() {
    when:
    app {
      modules {
        bind ServerErrorHandler, PrintingServerErrorHandler
      }
      handlers {
        get {
          background {
            1
          }.onError {
            throw new Exception("!")
          }.then {
            throw new Exception("success")
          }
        }
      }
    }

    then:
    text.startsWith(new Exception("success").toString())
    response.statusCode == 500
  }

  def "closure arg type mismatch errors on success handler are handled well"() {
    when:
    app {
      modules {
        bind ServerErrorHandler, PrintingServerErrorHandler
      }
      handlers {
        get {
          background {
            1
          }.then { List<String> result ->
            response.send("unexpected")
          }
        }
      }
    }

    then:
    text.startsWith "groovy.lang.MissingMethodException"
    response.statusCode == 500
  }

  def "closure arg type mismatch errors on error handler are handled well"() {
    when:
    app {
      modules {
        bind ServerErrorHandler, PrintingServerErrorHandler
      }
      handlers {
        get {
          background {
            throw new Exception("!")
          }.onError { String string ->
            response.send("unexpected")
          }.then {
            response.send("unexpected - value")
          }
        }
      }
    }

    then:
    text.startsWith "groovy.lang.MissingMethodException"
    response.statusCode == 500
  }

  def "delegate in closure actions is no the arg"() {
    when:
    app {
      handlers {
        handler {
          background {
            [foo: "bar"]
          }.then {
            response.send it.toString()
          }
        }
      }
    }

    then:
    getText() == [foo: "bar"].toString()
  }
}
