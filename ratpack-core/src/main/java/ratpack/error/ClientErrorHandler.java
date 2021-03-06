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

package ratpack.error;

import ratpack.handling.Context;

/**
 * The client error handler deals with errors that are due to the client doing something wrong.
 * <p>
 * Examples:
 * <ul>
 *   <li>Unsupported media type (415)
 *   <li>Unsupported method (405)
 * </ul>
 */
public interface ClientErrorHandler {

  /**
   * Handle a client error.
   *
   * @param context The context
   * @param statusCode The 4xx status code that explains the problem
   */
  void error(Context context, int statusCode) throws Exception;

}
