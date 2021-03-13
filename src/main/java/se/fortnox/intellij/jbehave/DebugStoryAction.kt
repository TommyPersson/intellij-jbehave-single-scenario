package se.fortnox.intellij.jbehave

class DebugStoryAction(
    storyFileAndProject: StoryFileAndProject?
) : RunStoryActionBase(storyFileAndProject, RunMode.Debug) {
    @Suppress("unused")
    constructor() : this(null)
}