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

package ratpack.groovy.internal;

import com.google.common.collect.ImmutableList;
import groovy.lang.Closure;
import ratpack.handling.internal.ServiceExtractor;
import ratpack.registry.Registry;
import ratpack.util.Action;

import java.util.Collections;
import java.util.List;

public class ClosureInvoker<T, D> {

  private final Closure<T> closure;
  private final List<Class<?>> parameterTypes;
  private final boolean hasDefaultParam;


  @SuppressWarnings("unchecked")
  public ClosureInvoker(Closure<? extends T> closure) {
    this.closure = (Closure<T>) closure.clone();
    this.hasDefaultParam = closure.getMaximumNumberOfParameters() > 0;
    closure.setDelegate(null);

    this.parameterTypes = retrieveParameterTypes(this.closure);
  }

  public <T> T invoke(Registry registry, D delegate, int resolveStrategy) {
    @SuppressWarnings("unchecked")
    Closure<T> clone = (Closure<T>) closure.clone();
    clone.setDelegate(delegate);
    clone.setResolveStrategy(resolveStrategy);

    if (parameterTypes.isEmpty()) {
      if (hasDefaultParam) {
        return clone.call(delegate);
      } else {
        return clone.call();
      }
    } else {
      Object[] services = ServiceExtractor.extract(parameterTypes, registry);
      return clone.call(services);
    }
  }

  public Action<D> toAction(final Registry registry, final int resolveStrategy) {
    return new Action<D>() {
      public void execute(D delegate) {
        invoke(registry, delegate, resolveStrategy);
      }
    };
  }

  private static List<Class<?>> retrieveParameterTypes(Closure<?> closure) {
    Class[] parameterTypes = closure.getParameterTypes();
    if (parameterTypes.length == 1 && parameterTypes[0].equals(Object.class)) {
      return Collections.emptyList();
    } else {
      for (Class<?> clazz : parameterTypes) {
        if (clazz.isArray()) {
          throw new IllegalStateException("Closure parameters cannot be array types (type: " + clazz.getName() + ", closure: " + closure.getClass().getName() + ")");
        }
      }

      return ImmutableList.<Class<?>>copyOf(parameterTypes);
    }
  }
}
