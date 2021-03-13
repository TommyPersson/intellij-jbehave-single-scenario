package se.fortnox.intellij.jbehave

import com.github.kumaraman21.intellijbehave.parser.StoryFile

class DebugStoryAction(storyFile: StoryFile) : RunStoryActionBase(storyFile, RunMode.Debug)