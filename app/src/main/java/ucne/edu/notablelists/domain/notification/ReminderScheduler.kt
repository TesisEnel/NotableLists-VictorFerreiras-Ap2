package ucne.edu.notablelists.domain.notification

import java.time.LocalDateTime

interface ReminderScheduler {
    fun schedule(noteId: String, title: String, time: LocalDateTime)
    fun cancel(noteId: String)
}