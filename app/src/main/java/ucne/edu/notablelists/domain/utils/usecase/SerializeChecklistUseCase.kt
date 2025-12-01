package ucne.edu.notablelists.domain.utils.usecase

import ucne.edu.notablelists.presentation.Notes.edit.ChecklistItem
import javax.inject.Inject

class SerializeChecklistUseCase @Inject constructor() {
    operator fun invoke(items: List<ChecklistItem>): String? {
        if (items.isEmpty()) return null
        return items.joinToString("\n") {
            "${if (it.isDone) "1" else "0"}|${it.text}"
        }
    }
}