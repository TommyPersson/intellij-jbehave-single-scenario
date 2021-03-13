package se.fortnox.intellij.jbehave

class RunStoryAction(
    storyFileAndProject: StoryFileAndProject?
) : RunStoryActionBase(storyFileAndProject, RunMode.Run) {
    @Suppress("unused")
    constructor() : this(null)
}
