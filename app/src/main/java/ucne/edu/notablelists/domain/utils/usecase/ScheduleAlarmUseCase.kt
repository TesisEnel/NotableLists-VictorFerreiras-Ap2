package ucne.edu.notablelists.domain.utils.usecase

import ucne.edu.notablelists.data.local.AlarmScheduler
import java.time.LocalDateTime
import javax.inject.Inject

class ScheduleAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler
) {
    operator fun invoke(noteId: String, title: String, dateTime: LocalDateTime) {
        alarmScheduler.schedule(noteId, title.ifBlank { "Sin Título" }, dateTime)
    }
}