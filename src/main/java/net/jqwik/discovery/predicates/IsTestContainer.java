package net.jqwik.discovery.predicates;

import net.jqwik.api.Group;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

public class IsTestContainer implements Predicate<Class<?>> {

	private static final IsExampleMethod isExampleMethod = new IsExampleMethod();
	private static final IsPropertyMethod isPropertyMethod = new IsPropertyMethod();
	private static final Predicate<Method> isAnyTestMethod = isExampleMethod.or(isPropertyMethod);

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();

	@Override
	public boolean test(Class<?> candidate) {
		if (!isPotentialTestContainer.test(candidate)) {
			return false;
		}
		return hasTests(candidate) || isGroup(candidate);
	}

	private boolean hasTests(Class<?> candidate) {
		return !ReflectionSupport.findMethods(candidate, isAnyTestMethod, HierarchyTraversalMode.TOP_DOWN).isEmpty();
	}

	private boolean isGroup(Class<?> candidate) {
		return isAnnotated(candidate, Group.class);
	}

}
