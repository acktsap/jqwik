package net.jqwik.newArbitraries;

import static org.assertj.core.api.Assertions.*;

import java.util.*;

import net.jqwik.api.*;

@Group
class NArbitraryTests {

	private Random random = new Random();

	@Example
	void generateInteger() {
		NArbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
		NShrinkableGenerator<Integer> generator = arbitrary.generator(10);

		assertThat(generator.next(random).value()).isEqualTo(1);
		assertThat(generator.next(random).value()).isEqualTo(2);
		assertThat(generator.next(random).value()).isEqualTo(3);
		assertThat(generator.next(random).value()).isEqualTo(4);
		assertThat(generator.next(random).value()).isEqualTo(5);
		assertThat(generator.next(random).value()).isEqualTo(1);
	}

	@Example
	void shrinkInteger() {
		NArbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
		NShrinkableGenerator<Integer> generator = arbitrary.generator(10);

		NShrinkable<Integer> value5 = generateNth(generator, 5);
		assertThat(value5.value()).isEqualTo(5);
		Set<NShrinkable<Integer>> shrunkValues = value5.nextShrinkingCandidates();
		assertThat(shrunkValues).hasSize(1);

		NShrinkable<Integer> shrunkValue = shrunkValues.iterator().next();
		assertThat(shrunkValue.value()).isEqualTo(4);
		assertThat(shrunkValue.distance()).isEqualTo(3);
	}

	@Example
	void generateList() {
		NArbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
		NShrinkableGenerator<List<Integer>> generator = arbitrary.generator(10);

		assertThat(generator.next(random).value()).isEmpty();
		assertThat(generator.next(random).value()).containsExactly(1);
		assertThat(generator.next(random).value()).containsExactly(1, 2);
		assertThat(generator.next(random).value()).containsExactly(1, 2, 3);
		assertThat(generator.next(random).value()).containsExactly(1, 2, 3, 4);
		assertThat(generator.next(random).value()).containsExactly(1, 2, 3, 4, 5);
		assertThat(generator.next(random).value()).isEmpty();
		assertThat(generator.next(random).value()).containsExactly(1);
	}

	@Example
	void shrinkList() {
		NArbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
		NShrinkableGenerator<List<Integer>> generator = arbitrary.generator(10);

		NShrinkable<List<Integer>> value5 = generateNth(generator, 6);
		assertThat(value5.value()).containsExactly(1, 2, 3, 4, 5);

		Set<NShrinkable<List<Integer>>> shrunkValues = value5.nextShrinkingCandidates();
		assertThat(shrunkValues).hasSize(2);
		shrunkValues.forEach(shrunkValue -> {
			assertThat(shrunkValue.value()).hasSize(4);
			assertThat(shrunkValue.distance()).isEqualTo(4);
		});
	}

	@Group
	class Filtering {
		@Example
		void filterInteger() {
			NArbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			NArbitrary<Integer> filtered = arbitrary.filter(anInt -> anInt % 2 != 0);
			NShrinkableGenerator<Integer> generator = filtered.generator(10);

			assertThat(generator.next(random).value()).isEqualTo(1);
			assertThat(generator.next(random).value()).isEqualTo(3);
			assertThat(generator.next(random).value()).isEqualTo(5);
			assertThat(generator.next(random).value()).isEqualTo(1);
		}

		@Example
		void shrinkFilteredInteger() {
			NArbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			NArbitrary<Integer> filtered = arbitrary.filter(anInt -> anInt % 2 != 0);
			NShrinkableGenerator<Integer> generator = filtered.generator(10);

			NShrinkable<Integer> value5 = generateNth(generator, 3);
			assertThat(value5.value()).isEqualTo(5);
			Set<NShrinkable<Integer>> shrunkValues = value5.nextShrinkingCandidates();
			assertThat(shrunkValues).hasSize(1);

			NShrinkable<Integer> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.value()).isEqualTo(3);
			assertThat(shrunkValue.distance()).isEqualTo(2);
		}

		@Example
		void filterList() {
			NArbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
			NArbitrary<List<Integer>> filtered = arbitrary.filter(aList -> aList.size() % 2 != 0);
			NShrinkableGenerator<List<Integer>> generator = filtered.generator(10);

			assertThat(generator.next(random).value()).containsExactly(1);
			assertThat(generator.next(random).value()).containsExactly(1, 2, 3);
			assertThat(generator.next(random).value()).containsExactly(1, 2, 3, 4, 5);
			assertThat(generator.next(random).value()).containsExactly(1);
		}

		@Example
		void shrinkFilteredList() {
			NArbitrary<List<Integer>> arbitrary = new ListArbitraryForTests(5);
			NArbitrary<List<Integer>> filtered = arbitrary.filter(aList -> aList.size() % 2 != 0);
			NShrinkableGenerator<List<Integer>> generator = filtered.generator(10);

			NShrinkable<List<Integer>> value5 = generateNth(generator, 3);
			assertThat(value5.value()).containsExactly(1, 2, 3, 4, 5);

			Set<NShrinkable<List<Integer>>> shrunkValues = value5.nextShrinkingCandidates();
			assertThat(shrunkValues).hasSize(3); // [1,2,3] [2,3,4] [3,4,5]
			shrunkValues.forEach(shrunkValue -> {
				assertThat(shrunkValue.value()).hasSize(3);
				assertThat(shrunkValue.distance()).isEqualTo(3);
			});
		}

	}

	@Group
	class Mapping {

		@Example
		void mapIntegerToString() {
			NArbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			NArbitrary<String> mapped = arbitrary.map(anInt -> "value=" + anInt);
			NShrinkableGenerator<String> generator = mapped.generator(10);

			assertThat(generator.next(random).value()).isEqualTo("value=1");
			assertThat(generator.next(random).value()).isEqualTo("value=2");
			assertThat(generator.next(random).value()).isEqualTo("value=3");
			assertThat(generator.next(random).value()).isEqualTo("value=4");
			assertThat(generator.next(random).value()).isEqualTo("value=5");
			assertThat(generator.next(random).value()).isEqualTo("value=1");
		}

		@Example
		void shrinkIntegerMappedToString() {
			NArbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			NArbitrary<String> mapped = arbitrary.map(anInt -> "value=" + anInt);
			NShrinkableGenerator<String> generator = mapped.generator(10);

			NShrinkable<String> value5 = generateNth(generator, 5);
			assertThat(value5.value()).isEqualTo("value=5");
			Set<NShrinkable<String>> shrunkValues = value5.nextShrinkingCandidates();
			assertThat(shrunkValues).hasSize(1);

			NShrinkable<String> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.value()).isEqualTo("value=4");
			assertThat(shrunkValue.distance()).isEqualTo(3);
		}

		@Example
		void shrinkFilteredIntegerMappedToString() {
			NArbitrary<Integer> arbitrary = new ArbitraryWheelForTests<>(1, 2, 3, 4, 5);
			NArbitrary<Integer> filtered = arbitrary.filter(anInt -> anInt % 2 != 0);
			NArbitrary<String> mapped = filtered.map(anInt -> "value=" + anInt);
			NShrinkableGenerator<String> generator = mapped.generator(10);

			NShrinkable<String> value5 = generateNth(generator, 3);
			assertThat(value5.value()).isEqualTo("value=5");
			Set<NShrinkable<String>> shrunkValues = value5.nextShrinkingCandidates();
			assertThat(shrunkValues).hasSize(1);

			NShrinkable<String> shrunkValue = shrunkValues.iterator().next();
			assertThat(shrunkValue.value()).isEqualTo("value=3");
			assertThat(shrunkValue.distance()).isEqualTo(2);
		}
	}

	@Group
	class Combination {

		@Example
		void generateCombination() {
			NArbitrary<Integer> a1 = new ArbitraryWheelForTests<>(1, 2, 3);
			NArbitrary<Integer> a2 = new ArbitraryWheelForTests<>(4, 5, 6);
			NArbitrary<String> combined = NCombinators.combine(a1, a2).as((i1, i2) -> i1 + ":" + i2);
			NShrinkableGenerator<String> generator = combined.generator(10);

			assertThat(generator.next(random).value()).isEqualTo("1:4");
			assertThat(generator.next(random).value()).isEqualTo("2:5");
			assertThat(generator.next(random).value()).isEqualTo("3:6");
			assertThat(generator.next(random).value()).isEqualTo("1:4");
		}

		@Example
		void shrinkCombination() {
			NArbitrary<Integer> a1 = new ArbitraryWheelForTests<>(1, 2, 3);
			NArbitrary<Integer> a2 = new ArbitraryWheelForTests<>(4, 5, 6);
			NArbitrary<String> combined = NCombinators.combine(a1, a2).as((i1, i2) -> i1 + ":" + i2);
			NShrinkableGenerator<String> generator = combined.generator(10);

			NShrinkable<String> value3to6 = generateNth(generator, 3);
			assertThat(value3to6.value()).isEqualTo("3:6");

			Set<NShrinkable<String>> shrunkValues = value3to6.nextShrinkingCandidates();
			assertThat(shrunkValues).hasSize(2); // 2:6 3:5
			shrunkValues.forEach(shrunkValue -> {
				assertThat(shrunkValue.value()).isIn("2:6", "3:5");
				assertThat(shrunkValue.distance()).isEqualTo(3); // sum of single distances
			});
		}

		@Example
		void shrinkListCombinedWithInteger() {
			NArbitrary<List<Integer>> lists = new ListArbitraryForTests(2);
			NArbitrary<Integer> integers = new ArbitraryWheelForTests<>(0, 1, 2);
			NArbitrary<String> combined = NCombinators.combine(lists, integers).as((l, i) -> l.toString() + ":" + i);

			NShrinkableGenerator<String> generator = combined.generator(10);

			NShrinkable<String> combinedString = generateNth(generator, 3);
			assertThat(combinedString.value()).isEqualTo("[1, 2]:2");

			Set<NShrinkable<String>> shrunkValues = combinedString.nextShrinkingCandidates();
			assertThat(shrunkValues).hasSize(3);
			shrunkValues.forEach(shrunkValue -> {
				assertThat(shrunkValue.value()).isIn("[1]:2", "[2]:2", "[1, 2]:1");
				assertThat(shrunkValue.distance()).isEqualTo(3); // sum of single distances
			});
		}

	}

	private <T> NShrinkable<T> generateNth(NShrinkableGenerator<T> generator, int n) {
		NShrinkable<T> generated = null;
		for (int i = 0; i < n; i++) {
			generated = generator.next(random);
		}
		return generated;
	}

}