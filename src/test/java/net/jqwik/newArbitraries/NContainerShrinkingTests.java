package net.jqwik.newArbitraries;

import net.jqwik.api.*;
import net.jqwik.properties.shrinking.*;
import org.assertj.core.api.*;

import java.util.*;

class NContainerShrinkingTests {

	@Example
	void dontShrinkEmptyList() {
		NContainerShrinkable<List<Integer>, Integer, List<NShrinkable<Integer>>> list = emptyShrinkableIntegerList();
		NShrinkResult<NShrinkable<List<Integer>>> shrinkResult = list.shrink(MockFalsifier.falsifyAll(), null);
		Assertions.assertThat(shrinkResult.value().value()).isEmpty();
	}

	private NContainerShrinkable<List<Integer>, Integer, List<NShrinkable<Integer>>> emptyShrinkableIntegerList() {
		return new NContainerShrinkable<>(new ArrayList<>(), ArrayList::new, new NListShrinker<>());
	}

	@Example
	void shrinkListSizeOnly() {
		NShrinkable<List<Integer>> list = NArbitraryTestHelper.shrinkableListOfIntegers(0, 0, 0, 0);

		NShrinkResult<NShrinkable<List<Integer>>> shrinkResult = list.shrink(listToShrink -> {
			if (listToShrink.size() < 2) return true;
			return false;

		}, null);

		Assertions.assertThat(shrinkResult.value().value()).containsExactly(0, 0);
		Assertions.assertThat(shrinkResult.value().distance()).isEqualTo(2);
	}

	@Example
	void shrinkElementsOnly() {
		NShrinkable<List<Integer>> list = NArbitraryTestHelper.shrinkableListOfIntegers(1, 2, 3, 4);

		NShrinkResult<NShrinkable<List<Integer>>> shrinkResult = list.shrink(listToShrink -> {
			if (listToShrink.size() != 4) return true;
			return !listToShrink.stream().allMatch(anInt -> anInt > 0);

		}, null);

		Assertions.assertThat(shrinkResult.value().value()).containsExactly(1, 1, 1, 1);
		Assertions.assertThat(shrinkResult.value().distance()).isEqualTo(8);
	}

	@Example
	void shrinkNumberOfElementsThenIndividualElements() {
		NShrinkable<List<Integer>> list = NArbitraryTestHelper.shrinkableListOfIntegers(1, 2, 3, 4, 5);

		NShrinkResult<NShrinkable<List<Integer>>> shrinkResult = list.shrink(listToShrink -> {
			if (listToShrink.size() < 3) return true;
			return false;
		}, null);

		Assertions.assertThat(shrinkResult.value().value()).containsExactly(0, 0, 0);
		Assertions.assertThat(shrinkResult.value().distance()).isEqualTo(3);

	}
}