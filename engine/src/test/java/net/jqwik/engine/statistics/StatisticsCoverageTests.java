package net.jqwik.engine.statistics;

import java.util.*;
import java.util.function.*;

import org.assertj.core.api.*;
import org.assertj.core.data.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.statistics.*;
import net.jqwik.api.statistics.Statistics;
import net.jqwik.engine.*;

import static net.jqwik.api.statistics.StatisticsReport.StatisticsReportMode.*;

@Group
class StatisticsCoverageTests {

	@Property(tries = 10)
	@ExpectFailure("coverage check should have failed")
	void moreThanOneCoverageWorks(@ForAll int anInt) {
		Statistics.collect(anInt > 0);

		Statistics.coverage(statisticsCoverage -> statisticsCoverage.check(true).count(c -> true));
		Statistics.coverage(statisticsCoverage -> statisticsCoverage.check(false).count(c -> false));
	}

	@Group
	class Count {
		@Property(tries = 10)
		@ExpectFailure("coverage check should have failed")
		void coverageFailsForViolatedCountCondition(@ForAll int anInt) {
			Statistics.collect(anInt > 0);

			Statistics.coverage(coverage -> {
				coverage.check(true).count(c -> false);
			});
		}

		@Property(tries = 10)
		@ExpectFailure("coverage check should have failed")
		void coverageWorksForLabelledCollectors(@ForAll int anInt) {
			Statistics.label("ints").collect(anInt > 0);

			Statistics.coverageOf("ints", coverage -> {
				coverage.check(true).count(c -> false);
			});
		}

		@Property(tries = 10)
		void countCheckPredicate(@ForAll int anInt) {
			Statistics.collect(anInt > 0);

			Statistics.coverage(coverage -> {
				coverage.check(true).count(c -> c > 0);
			});
		}

		@Property(tries = 10)
		void countCheckPredicateWithCountAll(@ForAll int anInt) {
			Statistics.collect(anInt > 0);

			Statistics.coverage(coverage -> {
				coverage.check(true).count((c, a) -> c <= a);
			});
		}

		int countPositive = 0;

		@Property(tries = 10)
		void countCheckAssertion(@ForAll int anInt) {
			boolean isPositive = anInt > 0;
			if (isPositive) {
				countPositive++;
			}
			Statistics.collect(isPositive);

			Statistics.coverage(coverage -> {
				coverage.check(true).count(c -> {
					Assertions.assertThat(c).isEqualTo(countPositive);
				});
			});
		}

		@Property(tries = 10)
		void countCheckAssertionWithCountAll(@ForAll int anInt) {
			Statistics.collect(anInt > 0);

			Statistics.coverage(coverage -> {
				coverage.check(true).count((c, a) -> {
					Assertions.assertThat(c).isLessThanOrEqualTo(a);
				});
			});
		}
	}

	@Group
	class Percentage {
		@Property(tries = 10)
		@ExpectFailure("coverage check should have failed")
		void coverageFailsForViolatedPercentageCondition(@ForAll int anInt) {
			Statistics.collect(anInt > 0);

			Statistics.coverage(coverage -> {
				coverage.check(true).percentage(c -> false);
			});
		}

		@Property(tries = 10)
		@ExpectFailure("coverage check should have failed")
		void coverageWorksForLabelledCollectors(@ForAll int anInt) {
			Statistics.label("ints").collect(anInt > 0);

			Statistics.coverageOf("ints", coverage -> {
				coverage.check(true).percentage(c -> {
					Assertions.fail("");
				});
			});
		}

		@Property(tries = 10)
		void percentageCheckPredicate(@ForAll int anInt) {
			Statistics.collect(anInt > 0);

			Statistics.coverage(coverage -> {
				coverage.check(true).percentage(p -> p > 0.0);
			});
		}

		@Property(tries = 10)
		void percentageCheckAssertion(@ForAll int anInt) {
			Statistics.collect(anInt > 0);

			Statistics.coverage(coverage -> {
				coverage.check(true).percentage(p -> {
					Assertions.assertThat(p).isGreaterThan(0.0);
					Assertions.assertThat(p).isLessThan(100.0);
				});
			});
		}
	}

	@Group
	class AdHocQueries {

		@Property(generation = GenerationMode.EXHAUSTIVE)
		@StatisticsReport(OFF)
		void checkCountForQuery(@ForAll @IntRange(min = 1, max = 100) int anInt) {
			Statistics.collect(anInt);

			Statistics.coverage(coverage -> {
				Predicate<List<Integer>> query = params -> params.get(0) <= 10;
				coverage.checkQuery(query).count(c -> c == 10);
			});
		}

		@Property(generation = GenerationMode.EXHAUSTIVE)
		@StatisticsReport(OFF)
		void checkPercentageForQuery(@ForAll @IntRange(min = 1, max = 100) int anInt) {
			Statistics.collect(anInt);

			Statistics.coverage(coverage -> {
				Predicate<List<Integer>> query = params -> params.get(0) <= 50;
				coverage.checkQuery(query).percentage(p -> {
					Assertions.assertThat(p).isCloseTo(50.0, Offset.offset(0.1));
				});
			});
		}

		@Property(generation = GenerationMode.EXHAUSTIVE)
		@StatisticsReport(OFF)
		void checkCountsAreAddedUp(@ForAll @IntRange(min = 1, max = 100) int anInt) {
			Statistics.label("twice").collect(anInt);
			Statistics.label("twice").collect(anInt);

			Statistics.coverageOf("twice", coverage -> {
				Predicate<List<Integer>> query = params -> params.get(0) <= 10;
				coverage.checkQuery(query).count(c -> c == 20);
			});
		}

		@Property(tries = 10)
		@StatisticsReport(OFF)
		@ExpectFailure(throwable = ClassCastException.class)
		void queryWithWrongTypeFails(@ForAll int anInt) {
			Statistics.collect(anInt);

			Statistics.coverage(coverage -> {
				Predicate<List<String>> query = params -> !params.get(0).isEmpty();
				coverage.checkQuery(query).count(c -> c == 10);
			});
		}

	}
}