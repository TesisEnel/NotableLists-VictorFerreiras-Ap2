package ucne.edu.notablelists.domain.utils.usecase

import ucne.edu.notablelists.presentation.Notes.edit.ChecklistItem
import javax.inject.Inject

class ParseChecklistUseCase @Inject constructor() {
    operator fun invoke(checklistString: String?): List<ChecklistItem> {
        if (checklistString.isNullOrBlank()) return emptyList()
        return checklistString.split("\n").mapNotNull {
            val parts = it.split("|", limit = 2)
            if (parts.size == 2) {
                ChecklistItem(parts[1], parts[0] == "1")
            } else null
        }
    }
}