package com.mylove.app.data

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.time.LocalDate
import java.time.MonthDay

private const val KEY_ME_NAME = "me_name"
private const val KEY_PARTNER_NAME = "partner_name"
private const val KEY_START_DATE = "start_date"
private const val KEY_ME_BIRTHDAY = "me_birthday"
private const val KEY_PARTNER_BIRTHDAY = "partner_birthday"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_FOOD = "pet_food"
private const val KEY_JOY = "pet_joy"
private const val KEY_ENERGY = "pet_energy"
private const val KEY_GROWTH = "pet_growth"
private const val KEY_COINS = "coins"
private const val KEY_FEED_DATE = "feed_date"
private const val KEY_FEED_USED = "feed_used"
private const val KEY_MISS_ME = "miss_me"
private const val KEY_MISS_TA = "miss_ta"
private const val KEY_MY_MOOD = "my_mood"
private const val KEY_DAILY_ANSWER = "daily_answer"
private const val KEY_LOVE_NOTE = "love_note"
private const val KEY_LAST_OPEN = "last_open"
private const val KEY_CONFIGURED = "configured"
private const val KEY_SYNC_SERVER = "sync_server"
private const val KEY_SYNC_TOKEN = "sync_token"
private const val KEY_SYNC_COUPLE = "sync_couple"
private const val KEY_SYNC_CODE = "sync_code"
private const val KEY_SYNC_ROLE = "sync_role"
private const val KEY_PALETTE = "palette"
private const val KEY_PET_TYPE = "pet_type"
private const val KEY_NOTIFY = "notify_enabled"
private const val KEY_MY_QUIZ = "my_quiz"
private const val KEY_PERIOD_STARTS = "period_starts"
private const val KEY_CYCLE = "cycle_length"
private const val KEY_PERIOD_LEN = "period_length"
private const val KEY_GUIDE_SEEN = "guide_seen"
private const val KEY_ACTION_DATE = "action_date"
private const val KEY_TOUCH = "touch_count"
private const val KEY_PLAY = "play_count"
private const val KEY_TASK_REWARD = "task_reward_date"

/**
 * 极简本地状态：所有字段直接写入 SharedPreferences，够原型使用。
 */
class LoveStore(private val prefs: SharedPreferences) {

    private var _meName by mutableStateOf(prefs.getString(KEY_ME_NAME, "")!!)
    var meName: String
        get() = _meName
        set(value) {
            _meName = value
            prefs.edit().putString(KEY_ME_NAME, value).apply()
        }

    private var _partnerName by mutableStateOf(prefs.getString(KEY_PARTNER_NAME, "")!!)
    var partnerName: String
        get() = _partnerName
        set(value) {
            _partnerName = value
            prefs.edit().putString(KEY_PARTNER_NAME, value).apply()
        }

    private var _startDate by mutableStateOf(
        LocalDate.ofEpochDay(prefs.getLong(KEY_START_DATE, LocalDate.now().toEpochDay()))
    )
    var startDate: LocalDate
        get() = _startDate
        set(value) {
            _startDate = value
            prefs.edit().putLong(KEY_START_DATE, value.toEpochDay()).apply()
        }

    private var _meBirthday by mutableStateOf(
        prefs.getString(KEY_ME_BIRTHDAY, "")!!.takeIf { it.isNotBlank() }?.let { raw ->
            runCatching { MonthDay.parse(raw) }.getOrNull()
        }
    )
    var meBirthday: MonthDay?
        get() = _meBirthday
        set(value) {
            _meBirthday = value
            prefs.edit().putString(KEY_ME_BIRTHDAY, value?.toString() ?: "").apply()
        }

    private var _partnerBirthday by mutableStateOf(
        prefs.getString(KEY_PARTNER_BIRTHDAY, "")!!.takeIf { it.isNotBlank() }?.let { raw ->
            runCatching { MonthDay.parse(raw) }.getOrNull()
        }
    )
    var partnerBirthday: MonthDay?
        get() = _partnerBirthday
        set(value) {
            _partnerBirthday = value
            prefs.edit().putString(KEY_PARTNER_BIRTHDAY, value?.toString() ?: "").apply()
        }

    private var _themeMode by mutableIntStateOf(prefs.getInt(KEY_THEME_MODE, 0))
    var themeMode: Int
        get() = _themeMode
        set(value) {
            _themeMode = value
            prefs.edit().putInt(KEY_THEME_MODE, value).apply()
        }

    private var _paletteId by mutableStateOf(prefs.getString(KEY_PALETTE, "warm")!!)
    var paletteId: String
        get() = _paletteId
        set(value) {
            _paletteId = value
            prefs.edit().putString(KEY_PALETTE, value).apply()
        }

    private var _petType by mutableStateOf(prefs.getString(KEY_PET_TYPE, "fox")!!)
    var petType: String
        get() = _petType
        set(value) {
            _petType = value
            prefs.edit().putString(KEY_PET_TYPE, value).apply()
        }

    val petSpec: PetSpec get() = petById(petType)

    private var _notificationsEnabled by mutableStateOf(prefs.getBoolean(KEY_NOTIFY, false))
    var notificationsEnabled: Boolean
        get() = _notificationsEnabled
        set(value) {
            _notificationsEnabled = value
            prefs.edit().putBoolean(KEY_NOTIFY, value).apply()
        }

    private var _configured by mutableStateOf(prefs.getBoolean(KEY_CONFIGURED, false))
    val isConfigured: Boolean get() = _configured

    fun completeSetup(me: String, partner: String, start: LocalDate) {
        meName = me.trim()
        partnerName = partner.trim()
        startDate = start
        _configured = true
        prefs.edit().putBoolean(KEY_CONFIGURED, true).apply()
    }

    private var _previousOpen by mutableStateOf(0L)
    val previousOpen: Long get() = _previousOpen

    private var _food by mutableIntStateOf(prefs.getInt(KEY_FOOD, 50))
    var food: Int
        get() = _food
        private set(value) {
            _food = value.coerceIn(0, 100)
            prefs.edit().putInt(KEY_FOOD, _food).apply()
        }

    private var _joy by mutableIntStateOf(prefs.getInt(KEY_JOY, 50))
    var joy: Int
        get() = _joy
        private set(value) {
            _joy = value.coerceIn(0, 100)
            prefs.edit().putInt(KEY_JOY, _joy).apply()
        }

    private var _energy by mutableIntStateOf(prefs.getInt(KEY_ENERGY, 50))
    var energy: Int
        get() = _energy
        private set(value) {
            _energy = value.coerceIn(0, 100)
            prefs.edit().putInt(KEY_ENERGY, _energy).apply()
        }

    private var _growth by mutableIntStateOf(prefs.getInt(KEY_GROWTH, 0))
    var growth: Int
        get() = _growth
        private set(value) {
            _growth = value.coerceIn(0, 100)
            prefs.edit().putInt(KEY_GROWTH, _growth).apply()
        }

    private var _coins by mutableIntStateOf(prefs.getInt(KEY_COINS, 0))
    var coins: Int
        get() = _coins
        private set(value) {
            _coins = value.coerceIn(0, 9999)
            prefs.edit().putInt(KEY_COINS, _coins).apply()
        }

    private var _feedUsed by mutableIntStateOf(prefs.getInt(KEY_FEED_USED, 0))
    var feedUsed: Int
        get() = _feedUsed
        private set(value) {
            _feedUsed = value
            prefs.edit()
                .putInt(KEY_FEED_USED, value)
                .putString(KEY_FEED_DATE, LocalDate.now().toString())
                .apply()
        }

    private var _missMe by mutableIntStateOf(prefs.getInt(KEY_MISS_ME, 0))
    var missMe: Int
        get() = _missMe
        private set(value) {
            _missMe = value
            prefs.edit().putInt(KEY_MISS_ME, value).apply()
        }

    private var _missTa by mutableIntStateOf(prefs.getInt(KEY_MISS_TA, 0))
    var missTa: Int
        get() = _missTa
        private set(value) {
            _missTa = value
            prefs.edit().putInt(KEY_MISS_TA, value).apply()
        }

    private var _myMood by mutableStateOf(prefs.getString(KEY_MY_MOOD, null))
    var myMood: String?
        get() = _myMood
        set(value) {
            _myMood = value
            prefs.edit().putString(KEY_MY_MOOD, value).apply()
        }

    private var _dailyAnswer by mutableStateOf(prefs.getString(KEY_DAILY_ANSWER, "")!!)
    var dailyAnswer: String
        get() = _dailyAnswer
        set(value) {
            _dailyAnswer = value
            prefs.edit().putString(KEY_DAILY_ANSWER, value).apply()
        }

    private var _loveNote by mutableStateOf(prefs.getString(KEY_LOVE_NOTE, "")!!)
    var loveNote: String
        get() = _loveNote
        set(value) {
            _loveNote = value
            prefs.edit().putString(KEY_LOVE_NOTE, value).apply()
        }

    private var _lastOpenMe by mutableStateOf(prefs.getLong(KEY_LAST_OPEN, System.currentTimeMillis()))
    var lastOpenMe: Long
        get() = _lastOpenMe
        private set(value) {
            _lastOpenMe = value
            prefs.edit().putLong(KEY_LAST_OPEN, value).apply()
        }

    val petLevel: Int get() = (growth / 20).coerceIn(0, 5) + 1
    val petForm: String get() = petSpec.form
    val feedLimit: Int get() = 3
    val feedLeft: Int get() = (feedLimit - feedUsed).coerceAtLeast(0)

    init {
        val today = LocalDate.now().toString()
        if (prefs.getString(KEY_FEED_DATE, "") != today) {
            _feedUsed = 0
            prefs.edit()
                .putInt(KEY_FEED_USED, 0)
                .putString(KEY_FEED_DATE, today)
                .apply()
        }
        _previousOpen = prefs.getLong(KEY_LAST_OPEN, 0L)
        lastOpenMe = System.currentTimeMillis()
        resetDailyActions(today)
    }

    fun feed() {
        if (feedLeft <= 0 || coins < 10) return
        feedUsed = feedUsed + 1
        coins = coins - 10
        food = food + 20
        growth = growth + 4
        checkTaskReward()
    }

    fun petAction() {
        joy = joy + 8
        energy = energy - 2
        _touchToday += 1
        persistActions()
        checkTaskReward()
    }

    fun play() {
        if (coins < 5) return
        coins = coins - 5
        joy = joy + 12
        energy = energy - 8
        growth = growth + 3
        _playToday += 1
        persistActions()
        checkTaskReward()
    }

    fun sleep() {
        energy = energy + 18
        food = food - 4
    }

    fun miss() {
        missMe = missMe + 1
    }

    fun resetPet() {
        food = 50
        joy = 50
        energy = 50
        growth = 10
        coins = 50
        feedUsed = 0
    }

    // ---- 生理期 ----

    private var _periodStartsRaw by mutableStateOf(prefs.getString(KEY_PERIOD_STARTS, "")!!)

    val periodStarts: List<LocalDate>
        get() = _periodStartsRaw.split(",").mapNotNull { raw ->
            runCatching { LocalDate.ofEpochDay(raw.trim().toLong()) }.getOrNull()
        }

    fun recordPeriodStart(date: LocalDate = LocalDate.now()) {
        val cleaned = (periodStarts + date).distinct().sorted().takeLast(24)
        _periodStartsRaw = cleaned.joinToString(",") { it.toEpochDay().toString() }
        prefs.edit().putString(KEY_PERIOD_STARTS, _periodStartsRaw).apply()
    }

    fun clearPeriods() {
        _periodStartsRaw = ""
        prefs.edit().putString(KEY_PERIOD_STARTS, "").apply()
    }

    private var _cycleLength by mutableIntStateOf(prefs.getInt(KEY_CYCLE, 28))
    var cycleLength: Int
        get() = _cycleLength
        set(value) {
            _cycleLength = value.coerceIn(20, 45)
            prefs.edit().putInt(KEY_CYCLE, _cycleLength).apply()
        }

    private var _periodLength by mutableIntStateOf(prefs.getInt(KEY_PERIOD_LEN, 5))
    var periodLength: Int
        get() = _periodLength
        set(value) {
            _periodLength = value.coerceIn(2, 10)
            prefs.edit().putInt(KEY_PERIOD_LEN, _periodLength).apply()
        }

    // ---- 默契问答 ----

    private var _myQuizRaw by mutableStateOf(prefs.getString(KEY_MY_QUIZ, "")!!)
    val myQuizAnswers: Map<String, String> get() = parseQuiz(_myQuizRaw)

    fun saveMyQuiz(answers: Map<String, String>) {
        _myQuizRaw = encodeQuiz(answers)
        prefs.edit().putString(KEY_MY_QUIZ, _myQuizRaw).apply()
    }

    private var _partnerQuizRaw by mutableStateOf("")
    val partnerQuizAnswers: Map<String, String> get() = parseQuiz(_partnerQuizRaw)

    // ---- 对方数据（云同步后可见）----

    private var _partnerMood by mutableStateOf<String?>(null)
    val partnerMood: String? get() = _partnerMood

    private var _partnerDaily by mutableStateOf("")
    val partnerDailyAnswer: String get() = _partnerDaily

    // ---- 每日任务 ----

    private var _touchToday by mutableIntStateOf(prefs.getInt(KEY_TOUCH, 0))
    val touchToday: Int get() = _touchToday

    private var _playToday by mutableIntStateOf(prefs.getInt(KEY_PLAY, 0))
    val playToday: Int get() = _playToday

    private var _taskRewardDate by mutableStateOf(prefs.getString(KEY_TASK_REWARD, "")!!)
    val taskRewardDone: Boolean get() = _taskRewardDate == LocalDate.now().toString()

    private var _guideSeen by mutableStateOf(prefs.getBoolean(KEY_GUIDE_SEEN, false))
    var guideSeen: Boolean
        get() = _guideSeen
        set(value) {
            _guideSeen = value
            prefs.edit().putBoolean(KEY_GUIDE_SEEN, value).apply()
        }

    private fun persistActions() {
        prefs.edit()
            .putInt(KEY_TOUCH, _touchToday)
            .putInt(KEY_PLAY, _playToday)
            .apply()
    }

    private fun resetDailyActions(today: String) {
        if (prefs.getString(KEY_ACTION_DATE, "") != today) {
            _touchToday = 0
            _playToday = 0
            prefs.edit()
                .putString(KEY_ACTION_DATE, today)
                .putInt(KEY_TOUCH, 0)
                .putInt(KEY_PLAY, 0)
                .apply()
        }
    }

    private fun checkTaskReward() {
        val today = LocalDate.now().toString()
        if (feedUsed >= 1 && _touchToday >= 3 && _playToday >= 1 && _taskRewardDate != today) {
            _taskRewardDate = today
            prefs.edit().putString(KEY_TASK_REWARD, today).apply()
            coins = coins + 20
        }
    }

    // ---- 云同步 ----

    private var _syncServer by mutableStateOf(
        prefs.getString(KEY_SYNC_SERVER, "http://123.249.80.35")!!
    )
    var syncServer: String
        get() = _syncServer
        set(value) {
            _syncServer = value
            prefs.edit().putString(KEY_SYNC_SERVER, value).apply()
        }

    private var _syncToken by mutableStateOf(prefs.getString(KEY_SYNC_TOKEN, "")!!)
    var syncToken: String
        get() = _syncToken
        private set(value) {
            _syncToken = value
            prefs.edit().putString(KEY_SYNC_TOKEN, value).apply()
        }

    private var _syncCoupleId by mutableStateOf(prefs.getString(KEY_SYNC_COUPLE, "")!!)
    var syncCoupleId: String
        get() = _syncCoupleId
        private set(value) {
            _syncCoupleId = value
            prefs.edit().putString(KEY_SYNC_COUPLE, value).apply()
        }

    private var _syncInviteCode by mutableStateOf(prefs.getString(KEY_SYNC_CODE, "")!!)
    var syncInviteCode: String
        get() = _syncInviteCode
        private set(value) {
            _syncInviteCode = value
            prefs.edit().putString(KEY_SYNC_CODE, value).apply()
        }

    private var _syncRole by mutableStateOf(prefs.getString(KEY_SYNC_ROLE, "a")!!)
    var syncRole: String
        get() = _syncRole
        private set(value) {
            _syncRole = value
            prefs.edit().putString(KEY_SYNC_ROLE, value).apply()
        }

    val isSynced: Boolean get() = syncToken.isNotBlank()

    private val meKey: String get() = if (syncRole == "b") "b" else "a"
    private val partnerKey: String get() = if (meKey == "a") "b" else "a"

    fun connectToSpace(
        server: String,
        token: String,
        coupleId: String,
        inviteCode: String,
        role: String
    ) {
        syncServer = server
        syncToken = token
        syncCoupleId = coupleId
        syncInviteCode = inviteCode
        syncRole = role
    }

    fun disconnectSpace() {
        syncToken = ""
        syncCoupleId = ""
        syncInviteCode = ""
        syncRole = "a"
    }

    fun buildRemoteState(): JSONObject {
        return JSONObject().apply {
            put("start_date", startDate.toString())
            put("names", JSONObject().put(meKey, meName).put(partnerKey, partnerName))
            put(
                "birthdays",
                JSONObject()
                    .put(meKey, meBirthday?.toString() ?: "")
                    .put(partnerKey, partnerBirthday?.toString() ?: "")
            )
            put(
                "pet",
                JSONObject()
                    .put("food", food)
                    .put("joy", joy)
                    .put("energy", energy)
                    .put("growth", growth)
                    .put("coins", coins)
            )
            put("miss", JSONObject().put(meKey, missMe).put(partnerKey, missTa))
            put("moods", JSONObject().put(meKey, myMood ?: JSONObject.NULL))
            put("daily", JSONObject().put(meKey, dailyAnswer))
            put("quiz", JSONObject().put(meKey, JSONObject(myQuizAnswers)))
            put("love_note", loveNote)
        }
    }

    fun applyRemoteState(state: JSONObject) {
        state.optString("start_date").takeIf { it.isNotBlank() }?.let { raw ->
            runCatching { startDate = LocalDate.parse(raw) }
        }

        state.optJSONObject("names")?.let { names ->
            names.optString(meKey).takeIf { it.isNotBlank() }?.let { meName = it }
            names.optString(partnerKey).takeIf { it.isNotBlank() }?.let { partnerName = it }
        }

        state.optJSONObject("birthdays")?.let { birthdays ->
            birthdays.optString(meKey).takeIf { it.isNotBlank() }?.let { raw ->
                runCatching { meBirthday = MonthDay.parse(raw) }
            }
            birthdays.optString(partnerKey).takeIf { it.isNotBlank() }?.let { raw ->
                runCatching { partnerBirthday = MonthDay.parse(raw) }
            }
        }

        state.optJSONObject("pet")?.let { pet ->
            food = pet.optInt("food", food)
            joy = pet.optInt("joy", joy)
            energy = pet.optInt("energy", energy)
            growth = pet.optInt("growth", growth)
            coins = pet.optInt("coins", coins)
        }

        state.optJSONObject("miss")?.let { miss ->
            missMe = miss.optInt(meKey, missMe)
            missTa = miss.optInt(partnerKey, missTa)
        }

        state.optJSONObject("moods")?.let { moods ->
            val value = moods.optString(meKey)
            myMood = value.takeIf { it.isNotBlank() && it != "null" }
            val partnerValue = moods.optString(partnerKey)
            _partnerMood = partnerValue.takeIf { it.isNotBlank() && it != "null" }
        }

        state.optJSONObject("daily")?.let { daily ->
            daily.optString(meKey).takeIf { it.isNotBlank() }?.let { dailyAnswer = it }
            _partnerDaily = daily.optString(partnerKey)
        }

        state.optJSONObject("quiz")?.optJSONObject(partnerKey)?.let { partnerQuiz ->
            _partnerQuizRaw = partnerQuiz.toString()
        }

        state.optString("love_note").takeIf { it.isNotBlank() }?.let { loveNote = it }
    }
}