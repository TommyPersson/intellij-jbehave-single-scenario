package se.fortnox.intellij.jbehave.utils;

public class ScenarioUtils {
	public static final String SCENARIO_PREFIX = "Scenario:";

	public static String findScenarioTitleInText(String text, int start) {
		int scenarioStart = text.lastIndexOf(SCENARIO_PREFIX, start);
		if (scenarioStart == -1) {
			return null;
		}
		int end = text.indexOf("\n", scenarioStart);
		if (end == -1) {
			return null;
		}
		return text.substring(scenarioStart + SCENARIO_PREFIX.length(), end).trim();
	}

	public static String createScenarioFilterFromTitle(String scenarioTitle) {
		return scenarioTitle.replaceAll("[[^\\p{ASCII}][\\-()%]]+", "*");
	}

	public static String formatTrimmedTitle(String scenarioTitle) {
		if (scenarioTitle.length() > 80) {
			return scenarioTitle.substring(0, 80) + " ...";
		}

		return scenarioTitle;
	}
}
