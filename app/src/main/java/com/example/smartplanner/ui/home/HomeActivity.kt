package com.example.smartplanner.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartplanner.R
import com.example.smartplanner.databinding.ActivityHomeBinding
import com.example.smartplanner.ui.settings.SettingsActivity
import com.example.smartplanner.viewmodel.SchedulerViewModel
import com.example.smartplanner.viewmodel.TaskViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var weekAdapter: WeekAdapter

    private val schedulerVm: SchedulerViewModel by viewModels()
    private val taskVm: TaskViewModel by viewModels()

    // Keep a master list so we can filter without losing tasks
    private val allTasks = mutableListOf<Task>()
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.title = "SyncUp"
        supportActionBar?.subtitle = "Plan · Focus · Finish"

        applyWindowInsets()

        setupWeekBar()
        setupRecyclerView()
        attachSwipeGestures()
        setupFab()
        setupBottomNav()

        // Load tasks from API (map missing due dates to 'today' for now)
        taskVm.load()
        taskVm.message.observe(this) { msg ->
            if (!msg.isNullOrBlank()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        taskVm.tasks.observe(this) { list ->
            val mapped = list.map { Task(it.title, it.tag, it.done, LocalDate.now()) }
            allTasks.clear()
            if (mapped.isEmpty()) {
                // keep some samples with spread dates across the week
                val today = LocalDate.now()
                allTasks.addAll(
                    listOf(
                        Task("Finish Wireframes", "Today · High", false, today),
                        Task("Prepare API Endpoints", "Tomorrow · Medium", false, today.plusDays(1)),
                        Task("Team Review – Calendar Flow", "Fri · Low", true, today.with(DayOfWeek.FRIDAY))
                    )
                )
            } else {
                allTasks.addAll(mapped)
            }
            filterFor(selectedDate)
        }
    }

    // Menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); true }
            R.id.action_load -> { schedulerVm.loadEvents(); true }
            R.id.action_create -> { schedulerVm.createSample(); true }
            R.id.action_logout -> { Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** WEEK BAR **/
    private fun setupWeekBar() {
        val startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val days = (0..6).map { i ->
            val date = startOfWeek.plusDays(i.toLong())
            WeekDay(date = date, selected = date == LocalDate.now())
        }.toMutableList()
        selectedDate = LocalDate.now()

        weekAdapter = WeekAdapter(days) { picked ->
            selectedDate = picked.date
            filterFor(selectedDate)
        }

        binding.weekList.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = weekAdapter
        }

        // month label at top
        binding.tvMonth.text = "${selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${selectedDate.year}"
    }

    /** TASK LIST **/
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(mutableListOf())
        binding.taskList.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = taskAdapter
        }
    }

    private fun filterFor(date: LocalDate) {
        val filtered = allTasks.filter { it.dueDate == date }
        taskAdapter.setItems(filtered)
        // update month label when you move across months in the week
        binding.tvMonth.text = "${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
    }

    /** ADD TASK **/
    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
            val etTitle = dialogView.findViewById<android.widget.EditText>(R.id.etTaskTitle)
            val etTag = dialogView.findViewById<android.widget.EditText>(R.id.etTaskTag)

            AlertDialog.Builder(this)
                .setTitle("New Task for ${selectedDate}")
                .setView(dialogView)
                .setPositiveButton("Add") { d, _ ->
                    val title = etTitle.text.toString().trim()
                    val tag = etTag.text.toString().trim().ifBlank { "No tag" }
                    if (title.isBlank()) {
                        Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show()
                    } else {
                        val newTask = Task(title, tag, false, selectedDate)
                        allTasks.add(newTask)
                        filterFor(selectedDate) // refresh list for that day
                        taskVm.add(title, tag) { /* API fallback already handled separately */ }
                    }
                    d.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    /** SWIPE ACTIONS **/
    private fun attachSwipeGestures() {
        val helper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val pos = viewHolder.bindingAdapterPosition
                    if (direction == ItemTouchHelper.LEFT) {
                        // toggle done
                        val current = taskAdapter.getItem(pos)
                        val indexInAll = allTasks.indexOfFirst { it === current }
                        if (indexInAll != -1) {
                            allTasks[indexInAll] = current.copy(done = !current.done)
                            filterFor(selectedDate)
                        }
                        Toast.makeText(
                            this@HomeActivity,
                            if (!current.done) "Marked done" else "Marked active",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // delete with Undo
                        val removed = taskAdapter.getItem(pos)
                        allTasks.remove(removed)
                        filterFor(selectedDate)
                        Snackbar.make(binding.root, "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                allTasks.add(removed)
                                filterFor(selectedDate)
                            }.show()
                    }
                }
            }
        )
        helper.attachToRecyclerView(binding.taskList)
    }

    /** MISC **/
    private fun setupBottomNav() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_task -> { Toast.makeText(this, "Task page (WIP)", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_collab -> { Toast.makeText(this, "Collaboration (WIP)", Toast.LENGTH_SHORT).show(); true }
                R.id.nav_profile -> { Toast.makeText(this, "Profile (WIP)", Toast.LENGTH_SHORT).show(); true }
                else -> false
            }
        }
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.topAppBar) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(v.paddingLeft, sysBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }
}
