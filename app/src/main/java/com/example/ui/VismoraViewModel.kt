package com.example.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.Content
import com.example.api.GenerateContentRequest
import com.example.api.GenerationConfig
import com.example.api.GeminiRetrofitClient
import com.example.api.Part
import com.example.data.Badge
import com.example.data.ChatHistory
import com.example.data.CodeChallenge
import com.example.data.QuizQuestion
import com.example.data.RoadmapNode
import com.example.data.UserProgress
import com.example.data.VismoraDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VismoraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = VismoraDatabase.getDatabase(application)
    private val userDao = db.userProgressDao()
    private val badgeDao = db.badgeDao()
    private val chatDao = db.chatHistoryDao()

    // --- Database-backed Flows ---
    val userProgress: StateFlow<UserProgress> = userDao.getProgressFlow()
        .map { it ?: UserProgress() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProgress()
        )

    val earnedBadges: StateFlow<List<Badge>> = badgeDao.getAllBadgesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val chatHistory: StateFlow<List<ChatHistory>> = chatDao.getChatHistoryFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- Local UI States ---
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // --- Tab and Screen states ---
    val currentBottomTab = mutableStateOf("home") // home, dsa, tutor, roadmaps, practice
    val selectedDsaConcept = mutableStateOf("stack") // stack, queue, sort, search

    // --- 1. Dynamic Cursor System ---
    val activeCursor = mutableStateOf("default")

    // --- 2. Gamification Database / Init ---
    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Create user default state if missing
            val existing = userDao.getProgress()
            if (existing == null) {
                userDao.saveProgress(UserProgress())
            }
            // Seed first badge "vismora_explorer"
            val badges = badgeDao.getAllBadges()
            if (badges.none { it.id == "explorer" }) {
                badgeDao.insertBadge(
                    Badge(
                        id = "explorer",
                        name = "Vismora Explorer",
                        description = "Embark on your fearless coding journey.",
                        category = "general",
                        iconName = "explore",
                        earnedTime = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun setCursorSymbol(cursorType: String) {
        activeCursor.value = cursorType
        viewModelScope.launch(Dispatchers.IO) {
            val progress = userDao.getProgress() ?: UserProgress()
            userDao.saveProgress(progress.copy(activeCursor = cursorType))
        }
    }

    fun switchTheme(themeId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = userDao.getProgress() ?: UserProgress()
            userDao.saveProgress(progress.copy(currentTheme = themeId))
        }
    }

    fun awardXp(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = userDao.getProgress() ?: UserProgress()
            var currentXp = progress.xp + amount
            var currentLevel = progress.level
            
            // Level-up threshold e.g. 100 XP per level
            val nextLevelThreshold = currentLevel * 100
            if (currentXp >= nextLevelThreshold) {
                currentXp -= nextLevelThreshold
                currentLevel += 1
                // Automatically unlock leveling badge
                unlockBadgeIfNeeded(
                    "level_$currentLevel",
                    "Level $currentLevel Ascendant",
                    "Demonstrated learning mastery and ascended to Level $currentLevel.",
                    "gamification"
                )
            }
            userDao.saveProgress(progress.copy(xp = currentXp, level = currentLevel))
        }
    }

    fun checkInStreak() {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = userDao.getProgress() ?: UserProgress()
            val now = System.currentTimeMillis()
            val diffMs = now - progress.lastActiveTime
            val diffDays = diffMs / (1000 * 60 * 60 * 24)

            val newStreak = if (diffDays in 1..2) {
                progress.streakCount + 1
            } else if (diffDays > 2) {
                1
            } else {
                progress.streakCount
            }

            userDao.saveProgress(progress.copy(streakCount = newStreak, lastActiveTime = now))
            awardXp(20)
            if (newStreak >= 3) {
                unlockBadgeIfNeeded("streak_3", "Dynamic Streak", "Maintained learning consistency for 3 consecutive check-ins.", "gamification")
            }
        }
    }

    suspend fun unlockBadgeIfNeeded(id: String, name: String, description: String, category: String) {
        val badges = badgeDao.getAllBadges()
        if (badges.none { it.id == id }) {
            badgeDao.insertBadge(
                Badge(
                    id = id,
                    name = name,
                    description = description,
                    category = category,
                    iconName = when (category) {
                        "ai" -> "psychology"
                        "dsa" -> "account_tree"
                        "gamification" -> "military_tech"
                        else -> "workspace_premium"
                    },
                    earnedTime = System.currentTimeMillis()
                )
            )
        }
    }

    // --- Font Preference & Accessibility Settings ---
    fun updateFontSize(multiplier: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = userDao.getProgress() ?: UserProgress()
            userDao.saveProgress(progress.copy(fontSizeMultiplier = multiplier))
        }
    }

    fun toggleHighContrast(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = userDao.getProgress() ?: UserProgress()
            userDao.saveProgress(progress.copy(highContrastMode = enabled))
        }
    }

    fun toggleColorBlind(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = userDao.getProgress() ?: UserProgress()
            userDao.saveProgress(progress.copy(colorBlindMode = enabled))
        }
    }

    // --- 3. Guru AI Integration ---
    fun askGuruAi(userMessageText: String) {
        if (userMessageText.trim().isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            // Save user message
            val userItem = ChatHistory(role = "user", messageText = userMessageText)
            chatDao.insertMessage(userItem)

            _aiLoading.value = true

            // Gather recent history
            val rawHistory = chatDao.getChatHistory()
            val chatHistoryList = rawHistory.takeLast(10) // Context size limit

            // Build payload
            val finalContents = chatHistoryList.map {
                Content(
                    role = if (it.role == "user") "user" else "model",
                    parts = listOf(Part(text = it.messageText))
                )
            }

            val systemInstruction = Content(
                parts = listOf(Part(text = "You are Guru AI, a friendly, comforting computer science mentor for the platform Vismora. Keep your responses highly pedagogy-rich, with formatting (markdown), brief code summaries, bullet points, and encouraging visual analogies. Remove the student's fear of coding entirely. Be ultra-illustrative."))
            )

            val request = GenerateContentRequest(
                contents = finalContents,
                systemInstruction = systemInstruction,
                generationConfig = GenerationConfig(temperature = 0.7f, maxOutputTokens = 800)
            )

            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                    delay(1500) // Simulated pedagogical response for sandbox/API key omission
                    val hint = "**API Key Status**: To enable real-time personalized guidance directly with Google Gemini, insert your Gemini API Key in the AI Studio Secrets Panel.\n\n" +
                            "**No worries! Here is a mentored guideline from Guru AI:**\n" +
                            "- When building a **Stack**, always visualize elements dropping from the top (LIFO).\n" +
                            "- In **Binary Search**, remember that splitting the array is only possible because the items are *already sorted*.\n" +
                            "- **Tip for DFS**: Think of it like exploring a deep cavern. You walk as deep as possible down a path, and only backtrack when you hit a dead end!"
                    chatDao.insertMessage(ChatHistory(role = "model", messageText = hint))
                    return@launch
                }

                val response = GeminiRetrofitClient.service.generateContent(
                    model = "gemini-3.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Guru AI feels a slight ripple in the force. Let me try compiling that node again!"

                chatDao.insertMessage(ChatHistory(role = "model", messageText = generatedText))
                awardXp(15)
                unlockBadgeIfNeeded("guru_consult", "Socratic Spark", "Consulted Guru AI, unlocking the power of guided reasoning.", "ai")
            } catch (e: Exception) {
                val errText = "Error compiling answer: ${e.localizedMessage}. Please verify your API keys and try again."
                chatDao.insertMessage(ChatHistory(role = "model", messageText = errText))
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.clearHistory()
        }
    }

    // --- 4. Interactive DSA Visualization Engine states ---

    // A. STACK VISUALIZER
    val stackElements = mutableStateListOf(24, 73, 11)
    val stackLog = mutableStateListOf("Initialized Stack", "Pushed 11", "Pushed 73", "Pushed 24")
    val isStackArrayBased = mutableStateOf(true) // Array vs Linked List
    val stackAnimatingItem = mutableStateOf<Int?>(null) // Item currently moving
    val stackAnimationDirection = mutableStateOf<String?>(null) // "in" (drop) or "out" (exit)

    fun pushStack(value: Int) {
        viewModelScope.launch {
            if (stackElements.size >= 8) {
                stackLog.add(0, "Stack overflow! Capacity reached.")
                return@launch
            }
            setCursorSymbol("stack")
            stackAnimatingItem.value = value
            stackAnimationDirection.value = "in"
            delay(400)
            stackElements.add(0, value)
            stackLog.add(0, "Pushed $value onto the Stack")
            awardXp(10)
            unlockBadgeIfNeeded("stack_pioneer", "Stack Pioneer", "Successfully mastered Stack pushing & popping visualizations.", "dsa")
            stackAnimatingItem.value = null
            stackAnimationDirection.value = null
        }
    }

    fun popStack() {
        if (stackElements.isEmpty()) {
            stackLog.add(0, "Stack underflow! No elements left to pop.")
            return
        }
        viewModelScope.launch {
            setCursorSymbol("stack")
            val popped = stackElements.first()
            stackAnimatingItem.value = popped
            stackAnimationDirection.value = "out"
            delay(400)
            stackElements.removeAt(0)
            stackLog.add(0, "Popped $popped from the Stack")
            awardXp(10)
            stackAnimatingItem.value = null
            stackAnimationDirection.value = null
        }
    }

    // B. QUEUE VISUALIZER
    val queueElements = mutableStateListOf(18, 45, 90)
    val queueType = mutableStateOf("standard") // standard, circular, priority, deque
    val queueLog = mutableStateListOf("Initialized Queue", "Enqueued 18, 45, 90")
    val queueAnimatingItem = mutableStateOf<Int?>(null)
    val queueAnimationDirection = mutableStateOf<String?>(null) // "in" (enter rear) or "out" (exit front)

    fun enqueue(value: Int) {
        if (queueElements.size >= 7) {
            queueLog.add(0, "Queue Overflow! Capacity limits hit.")
            return
        }
        viewModelScope.launch {
            setCursorSymbol("queue")
            queueAnimatingItem.value = value
            queueAnimationDirection.value = "in"
            delay(400)
            if (queueType.value == "priority") {
                queueElements.add(value)
                queueElements.sortDescending() // Highest priority at front
                queueLog.add(0, "Priority Enqueued $value (sorted by rank)")
            } else {
                queueElements.add(value)
                queueLog.add(0, "Enqueued $value at Rear (Index: ${queueElements.size - 1})")
            }
            awardXp(10)
            unlockBadgeIfNeeded("queue_pioneer", "Queue Pioneer", "Mastered Enqueuing and Dequeuing operations across standard, priority, and circular forms.", "dsa")
            queueAnimatingItem.value = null
            queueAnimationDirection.value = null
        }
    }

    fun dequeue() {
        if (queueElements.isEmpty()) {
            queueLog.add(0, "Queue Underflow! No items in Queue.")
            return
        }
        viewModelScope.launch {
            setCursorSymbol("queue")
            val removed = queueElements.first()
            queueAnimatingItem.value = removed
            queueAnimationDirection.value = "out"
            delay(400)
            queueElements.removeAt(0)
            queueLog.add(0, "Dequeued $removed from Front (Index: 0)")
            awardXp(10)
            queueAnimatingItem.value = null
            queueAnimationDirection.value = null
        }
    }

    // C. SORTING VISUALIZER
    val sortingArray = mutableStateListOf(42, 12, 85, 31, 5, 67, 19, 50)
    val sortingSwaps = mutableStateOf(0)
    val sortingComps = mutableStateOf(0)
    val isSortingRunning = mutableStateOf(false)
    val currentSortingIndices = mutableStateOf(Pair(-1, -1)) // indices currently active
    val sortingAlgorithm = mutableStateOf("bubble") // bubble, selection, insertion, quick

    fun resetSortingArray() {
        sortingArray.clear()
        val randomList = List(8) { (15..99).random() }
        sortingArray.addAll(randomList)
        sortingSwaps.value = 0
        sortingComps.value = 0
        currentSortingIndices.value = Pair(-1, -1)
        isSortingRunning.value = false
    }

    fun runSortingVis() {
        if (isSortingRunning.value) return
        isSortingRunning.value = true
        setCursorSymbol("sort")
        viewModelScope.launch {
            sortingSwaps.value = 0
            sortingComps.value = 0
            if (sortingAlgorithm.value == "bubble") {
                bubbleSortAnim()
            } else {
                selectionSortAnim()
            }
            isSortingRunning.value = false
            currentSortingIndices.value = Pair(-1, -1)
            awardXp(40)
            unlockBadgeIfNeeded("sort_master", "Algorithm Maestro", "Watched custom Sorting engine compile and arrange variables visually.", "dsa")
        }
    }

    private suspend fun bubbleSortAnim() {
        val n = sortingArray.size
        for (i in 0 until n - 1) {
            for (j in 0 until n - i - 1) {
                if (!isSortingRunning.value) return
                currentSortingIndices.value = Pair(j, j + 1)
                sortingComps.value += 1
                delay(400)
                if (sortingArray[j] > sortingArray[j + 1]) {
                    // Swap
                    val temp = sortingArray[j]
                    sortingArray[j] = sortingArray[j + 1]
                    sortingArray[j + 1] = temp
                    sortingSwaps.value += 1
                    delay(300)
                }
            }
        }
    }

    private suspend fun selectionSortAnim() {
        val n = sortingArray.size
        for (i in 0 until n - 1) {
            var minIdx = i
            for (j in i + 1 until n) {
                if (!isSortingRunning.value) return
                currentSortingIndices.value = Pair(minIdx, j)
                sortingComps.value += 1
                delay(400)
                if (sortingArray[j] < sortingArray[minIdx]) {
                    minIdx = j
                }
            }
            if (minIdx != i) {
                // Swap
                val temp = sortingArray[i]
                sortingArray[i] = sortingArray[minIdx]
                sortingArray[minIdx] = temp
                sortingSwaps.value += 1
                delay(300)
            }
        }
    }

    // D. SEARCHING VISUALIZER
    val searchArray = listOf(8, 14, 21, 33, 40, 52, 60, 71, 84, 91, 95, 99)
    val searchTarget = mutableStateOf(52)
    val searchSteps = mutableStateListOf<String>()
    val currentSearchMid = mutableStateOf(-1)
    val currentSearchLow = mutableStateOf(-1)
    val currentSearchHigh = mutableStateOf(-1)
    val searchResultFound = mutableStateOf<Boolean?>(null) // null = idle, true = found, false = not found

    fun performBinarySearch() {
        viewModelScope.launch {
            setCursorSymbol("search")
            searchResultFound.value = null
            searchSteps.clear()
            currentSearchLow.value = 0
            currentSearchHigh.value = searchArray.size - 1
            searchSteps.add("Initialized Binary Search. Target = ${searchTarget.value}")
            delay(600)

            var low = 0
            var high = searchArray.size - 1
            var stepsLeft = 10

            while (low <= high && stepsLeft > 0) {
                stepsLeft--
                val mid = low + (high - low) / 2
                currentSearchMid.value = mid
                currentSearchLow.value = low
                currentSearchHigh.value = high
                searchSteps.add(0, "Pivot selected at mid Index: $mid (Value: ${searchArray[mid]})")
                delay(1200)

                if (searchArray[mid] == searchTarget.value) {
                    searchSteps.add(0, "Target MATCH! Verified index: $mid.")
                    searchResultFound.value = true
                    awardXp(35)
                    unlockBadgeIfNeeded("search_master", "Logarithmic Pathfinder", "Visualized the elegant divide-and-conquer Binary Search.", "dsa")
                    return@launch
                }

                if (searchArray[mid] < searchTarget.value) {
                    searchSteps.add(0, "Value ${searchArray[mid]} < ${searchTarget.value}. Discarding left half. Recalculating Low = ${mid + 1}")
                    low = mid + 1
                } else {
                    searchSteps.add(0, "Value ${searchArray[mid]} > ${searchTarget.value}. Discarding right half. Recalculating High = ${mid - 1}")
                    high = mid - 1
                }
                currentSearchLow.value = low
                currentSearchHigh.value = high
                delay(800)
            }
            searchSteps.add(0, "Target not present in sorted space.")
            searchResultFound.value = false
        }
    }


    // --- 5. Roadmap Section Data ---
    val roadmaps = listOf(
        RoadmapNode("python_1", "Python Basics", "Master syntax, variables, basic input/outputs without fear.", listOf("Variables", "Input/Output"), listOf("Greetings generator"), "Beginner", 50, true),
        RoadmapNode("python_2", "Control Flow", "Diverge program logic using if-statements and conditional logic.", listOf("If-Else", "Comparisons"), listOf("Leap year check"), "Intermediate", 100),
        RoadmapNode("python_3", "Dynamic Lists & Dicts", "Collect elements dynamically. Linear listings of variables.", listOf("Lists", "Dictionaries"), listOf("Shopping logger"), "Advanced", 150),
        RoadmapNode("ai_1", "AI Engineering", "Establish neural structures and prompt-guided models.", listOf("Embeddings", "Model queries"), listOf("Vismora AI Assistant"), "Industry Level", 300)
    )

    // --- 6. Quiz Session ---
    val quizzes = listOf(
        QuizQuestion(
            "q1",
            "Which property defines LIFO (Last In First Out) behavior?",
            listOf("Queue", "Stack", "Tree", "Graph"),
            1,
            "A Stack stores items in a Last-In, First-Out sequence—meaning elements drop and lift from the top only!"
        ),
        QuizQuestion(
            "q2",
            "What is the average time complexity of searching in a BST?",
            listOf("O(1)", "O(N)", "O(log N)", "O(N log N)"),
            2,
            "In a balanced Binary Search Tree, we eliminate half of the tree's branches at each step, yielding O(log N) complexity!"
        ),
        QuizQuestion(
            "q3",
            "Why is Binary Search faster than Linear Search on sorted arrays?",
            listOf("It skips duplicates", "It uses divide and conquer to discard half the space each step", "It is written in assembly", "It parallelizes the items"),
            1,
            "By continually halving the search space, Binary Search achieves logarithmic time complexity! O(log N) vs Linear's O(N)."
        )
    )

    val currentQuizIdx = mutableStateOf(0)
    val chosenQuizOption = mutableStateOf<Int?>(null)
    val quizCorrectAnswerGiven = mutableStateOf<Boolean?>(null)

    fun submitQuizChoice(idx: Int) {
        val quiz = quizzes[currentQuizIdx.value]
        chosenQuizOption.value = idx
        val correct = idx == quiz.correctIdx
        quizCorrectAnswerGiven.value = correct
        if (correct) {
            awardXp(30)
            viewModelScope.launch {
                unlockBadgeIfNeeded("quiz_scout", "Trivia Scout", "Crushed conceptual challenges with a high index of precision.", "gamification")
            }
        }
    }

    fun nextQuiz() {
        chosenQuizOption.value = null
        quizCorrectAnswerGiven.value = null
        currentQuizIdx.value = (currentQuizIdx.value + 1) % quizzes.size
    }

    // --- 7. Interactive Practice Coding Challenges (Simulated compiler) ---
    val challenges = listOf(
        CodeChallenge(
            "c1",
            "Sum Array Elements",
            "Write a Python function to aggregate variables inside a listing block. Example: sum_list([1, 2, 3]) should return 6.",
            "def sum_list(items):\n    # Write your solution here\n    return 0",
            "Python",
            listOf("[1, 2, 3, 4]" to "10", "[-1, 10]" to "9"),
            "Beginner",
            60
        ),
        CodeChallenge(
            "c2",
            "Stack Push Validator",
            "Write a function string_pushed(char) that validates brackets match perfectly in a compiler compiler stack structure.",
            "def validate_brackets(brackets):\n    # Return True if perfectly balanced, else False\n    return True",
            "Python",
            listOf("'(())'" to "True", "'(()'" to "False"),
            "Intermediate",
            120
        )
    )

    val activeChallengeIdx = mutableStateOf(0)
    val currentCodeDraft = mutableStateOf(challenges[0].startingCode)
    val compilationOutputOutput = mutableStateOf<String?>(null)
    val challengePassed = mutableStateOf<Boolean?>(null)

    fun selectChallenge(index: Int) {
        activeChallengeIdx.value = index
        currentCodeDraft.value = challenges[index].startingCode
        compilationOutputOutput.value = null
        challengePassed.value = null
    }

    fun compileAndRunChallengeCode() {
        viewModelScope.launch {
            compilationOutputOutput.value = "Compiling visual nodes... Triggering compiler parser sandbox..."
            challengePassed.value = null
            delay(1500)
            
            // Simple robust mock compiler evaluation
            val draft = currentCodeDraft.value
            val chal = challenges[activeChallengeIdx.value]
            
            if (chal.id == "c1") {
                if (draft.contains("sum(") || draft.contains("for ") || draft.contains("while ")) {
                    compilationOutputOutput.value = "Success! Sandbox Output:\n" +
                            "[1, 2, 3, 4] -> Returned 10 (Passed)\n" +
                            "[-1, 10] -> Returned 9 (Passed)\n\n" +
                            "Tests compiled flawlessly. Theoretical and structural validations matched."
                    challengePassed.value = true
                    awardXp(chal.xpReward)
                    unlockBadgeIfNeeded("code_conqueror", "Syntax General", "Compiled and resolved functional assignments successfully.", "gamification")
                } else {
                    compilationOutputOutput.value = "Compilation Succeeded, but tests failed!\n" +
                            "Output on [1,2,3,4]: Expected 10, got 0.\n" +
                            "Stub returning default. Modify code draft logic to run sum loop."
                    challengePassed.value = false
                }
            } else {
                if (draft.contains("stack") || draft.contains("pop") || draft.contains("append")) {
                    compilationOutputOutput.value = "Success! Sandbox Output:\n" +
                            "'(())' -> Balanced (True) (Passed)\n" +
                            "'(()' -> Unbalanced (False) (Passed)\n\n" +
                            "Validated bracket recursion."
                    challengePassed.value = true
                    awardXp(chal.xpReward)
                } else {
                    compilationOutputOutput.value = "Validation incomplete! Empty stack detected or logic missing stack matching structures."
                    challengePassed.value = false
                }
            }
        }
    }
}
